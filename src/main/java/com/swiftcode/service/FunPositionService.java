package com.swiftcode.service;

import com.google.common.collect.Lists;
import com.swiftcode.config.Constants;
import com.swiftcode.domain.Device;
import com.swiftcode.domain.FunPosition;
import com.swiftcode.domain.SapUser;
import com.swiftcode.repository.DeviceRepository;
import com.swiftcode.repository.FunPositionRepository;
import com.swiftcode.repository.SapUserRepository;
import com.swiftcode.service.dto.DeviceDTO;
import com.swiftcode.service.dto.FunPositionDTO;
import com.swiftcode.service.mapper.FunPositionMapper;
import com.swiftcode.service.util.SapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 功能位置
 *
 * @author chen
 **/
@Slf4j
@Service
public class FunPositionService {
    @Value("${sap-url}")
    private String sapUrl;
    @Value("${sap-secret}")
    private String sapSecret;

    private FunPositionRepository repository;
    private FunPositionMapper mapper;
    private DeviceRepository deviceRepository;
    private SapUserRepository sapUserRepository;

    public FunPositionService(FunPositionRepository repository, FunPositionMapper mapper, DeviceRepository deviceRepository, SapUserRepository sapUserRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.deviceRepository = deviceRepository;
        this.sapUserRepository = sapUserRepository;
    }

    /**
     * 解析功能位置
     *
     * @param resXml resXml
     * @return 功能位置列表
     * @throws DocumentException DocumentException
     */
    private static List<FunPosition> parsePositions(String resXml) throws DocumentException {
        Document document = DocumentHelper.parseText(resXml);
        DefaultXPath xPath = new DefaultXPath("//EtData");
        xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
        return xPath.selectNodes(document).stream()
            .flatMap(itemNode -> ((Element) itemNode).elements().stream())
            .map(node -> {
                List<Element> items = node.elements();
                FunPosition entity = new FunPosition();
                items.forEach(element -> {
                    if (element.getName().equalsIgnoreCase("TPLMA")) {
                        entity.setParentId(element.getText().isEmpty() ? "0" : element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("TPLNR")) {
                        entity.setPositionCode(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("PLTXT")) {
                        entity.setPositionName(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("MATYP")) {
                        entity.setBranchCompanyCode(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("MATYT")) {
                        entity.setBranchCompanyName(element.getText());
                    }
                });
                return entity;
            })
            .collect(Collectors.toList());
    }

    /**
     * 解析设备
     *
     * @param resXml resXml
     * @return 设备列表
     * @throws DocumentException DocumentException
     */
    private static List<Device> parseDevices(String resXml) throws DocumentException {
        Document document = DocumentHelper.parseText(resXml);
        DefaultXPath xPath = new DefaultXPath("//EtData1");
        xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
        return xPath.selectNodes(document).stream()
            .flatMap(itemNode -> ((Element) itemNode).elements().stream())
            .map(node -> {
                List<Element> items = node.elements();
                Device entity = new Device();
                items.forEach(element -> {
                    if (element.getName().equalsIgnoreCase("HEQUI")) {
                        entity.setParentCode(element.getText().isEmpty() ? "0" : element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("EQUNR")) {
                        entity.setDeviceCode(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("EQKTX")) {
                        entity.setDeviceName(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("TPLNR")) {
                        entity.setPositionCode(element.getText());
                    }
                });
                return entity;
            })
            .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public FunPositionDTO addPosition(FunPositionDTO dto) {
        FunPosition position = repository.save(mapper.toEntity(dto));
        return mapper.toDto(position);
    }

    /**
     * 查找功能位置列表，返回树形结构
     *
     * @return 功能位置列表
     */
    public List<FunPositionDTO> findPositions(String userCode) {
        Optional<SapUser> optional = sapUserRepository.findByUserCode(userCode);
        if (optional.isPresent()) {

            List<Device> devices = deviceRepository.findAll();
            List<DeviceDTO> allDeviceDto = devices.stream()
                .map(device -> new DeviceDTO(device.getId(), device.getParentCode().trim(), device.getPositionCode().trim(), device.getDeviceCode().trim(), device.getDeviceName(), Lists.newArrayListWithCapacity(0))).collect(Collectors.toList());

            SapUser sapUser = optional.get();
            String branchCode = sapUser.getBranchCode().trim();
            List<FunPosition> allPositions = repository.findAllByBranchCompanyCode(branchCode);
            List<FunPositionDTO> allDtos = allPositions
                .stream()
                .map(position -> mapper.toDto(position))
                .collect(Collectors.toList());
            List<FunPositionDTO> root = Lists.newArrayList();

            for (FunPosition position : allPositions) {
                if ("0".equals(position.getParentId())) {
                    FunPositionDTO dto = mapper.toDto(position);
                    root.add(dto);
                }
            }

            for (FunPositionDTO dto : root) {
                List<FunPositionDTO> childList = getChild(dto.getPositionCode().trim(), allDtos, allDeviceDto);
                dto.setChildren(childList);
            }

            return root;
        }
        return Lists.newArrayListWithExpectedSize(0);
    }

    private List<DeviceDTO> getDeviceChild(String deviceCode, List<DeviceDTO> allDtos) {
        List<DeviceDTO> childList = Lists.newArrayList();
        for (DeviceDTO dto : allDtos) {
            if (dto.getParentCode().trim().equalsIgnoreCase(deviceCode)) {
                childList.add(dto);
            }
        }

        for (DeviceDTO dto : childList) {
            dto.setChildren(getDeviceChild(dto.getDeviceCode().trim(), allDtos));
        }

        if (childList.size() == 0) {
            return Lists.newArrayList();
        }
        return childList;
    }

    private List<FunPositionDTO> getChild(String positionCode, List<FunPositionDTO> allDtos, List<DeviceDTO> allDeviceDto) {
        List<FunPositionDTO> childList = Lists.newArrayList();
        for (FunPositionDTO dto : allDtos) {
            if (dto.getParentId().equalsIgnoreCase(positionCode)) {
                List<DeviceDTO> deviceOfPosition = deviceRepository.findByPositionCode(dto.getPositionCode().trim()).stream()
                    .map(device -> new DeviceDTO(device.getId(), device.getParentCode().trim(), device.getPositionCode().trim(), device.getDeviceCode().trim(), device.getDeviceName(), Lists.newArrayListWithCapacity(0))).collect(Collectors.toList());

                List<DeviceDTO> rootDeviceDto = Lists.newArrayList();
                for (DeviceDTO deviceDTO : deviceOfPosition) {
                    if (deviceDTO.getParentCode().isEmpty() || "0".equals(deviceDTO.getParentCode())) {
                        rootDeviceDto.add(deviceDTO);
                    }
                }
                log.info("root device size: {}", rootDeviceDto.size());

                List<DeviceDTO> deviceChildren = Lists.newArrayList();
                for (DeviceDTO deviceDTO : rootDeviceDto) {
                    List<DeviceDTO> deviceChild = getDeviceChild(deviceDTO.getDeviceCode().trim(), allDeviceDto);
                    deviceDTO.setChildren(deviceChild);
                    deviceChildren.add(deviceDTO);
                }
                dto.setDeviceChildren(deviceChildren);
                childList.add(dto);
            }
        }

        for (FunPositionDTO dto : childList) {
            dto.setChildren(getChild(dto.getPositionCode().trim(), allDtos, allDeviceDto));
        }

        if (childList.size() == 0) {
            return Lists.newArrayList();
        }
        return childList;
    }

    /**
     * 获取功能位置和对应的设备
     *
     * @throws URISyntaxException URISyntaxException
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncPositionsAndDevices() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);
        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_search_tplnr_new/888/zpm_search_tplnr_new/zpm_search_tplnr_new";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildGetPositionAndDeviceXml();

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();

        List<FunPosition> positions = null;
        try {
            positions = parsePositions(resXml);
        } catch (DocumentException e) {
            log.error("解析功能位置出错: {}", e.getMessage());
            try {
                parsePositions(resXml);
            } catch (DocumentException ex) {
                log.error("重试解析功能位置方法失败");
                ex.printStackTrace();
            }
        }
        if (null != positions) {
            for (FunPosition position : positions) {
                Optional<FunPosition> optional = repository.findByPositionCode(position.getPositionCode());
                if (!optional.isPresent()) {
                    repository.save(position);
                }
            }
        }

        List<Device> devices = null;
        try {
            devices = parseDevices(resXml);
        } catch (DocumentException e) {
            log.error("解析设备出错: {}", e.getMessage());
            try {
                parseDevices(resXml);
            } catch (DocumentException ex) {
                log.error("重试解析设备方法失败");
                ex.printStackTrace();
            }
        }
        if (null != devices) {
            for (Device device : devices) {
                Optional<Device> optional = deviceRepository.findByDeviceCode(device.getDeviceCode());
                if (!optional.isPresent()) {
                    deviceRepository.save(device);
                }
            }
        }
    }
}
