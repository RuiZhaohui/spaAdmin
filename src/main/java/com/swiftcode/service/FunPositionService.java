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
import org.dom4j.*;
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

    private static List<FunPosition> parsePositions(String rexXml) {
        List<FunPosition> positions = Lists.newArrayList();
        try {
            Document document = DocumentHelper.parseText(rexXml);
            DefaultXPath xPath = new DefaultXPath("//EtData");
            xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
            List<Node> list = xPath.selectNodes(document);
            for (Object o : list) {
                Element itemNode = (Element) o;
                List<Element> items = itemNode.elements();
                for (Element item : items) {
                    FunPosition entity = new FunPosition();
                    List<Element> elements = item.elements();
                    for (Element element : elements) {
                        if (element.getName().equalsIgnoreCase("TPLMA")) {
                            String parentCode;
                            if (element.getText().isEmpty()) {
                                parentCode = "0";
                            } else {
                                parentCode = element.getText();
                            }
                            entity.setParentId(parentCode);
                        } else if (element.getName().equalsIgnoreCase("TPLNR")) {
                            entity.setPositionCode(element.getText());
                        } else if (element.getName().equalsIgnoreCase("PLTXT")) {
                            entity.setPositionName(element.getText());
                        } else if (element.getName().equalsIgnoreCase("MATYP")) {
                            entity.setBranchCompanyCode(element.getText());
                        } else if (element.getName().equalsIgnoreCase("MATYT")) {
                            entity.setBranchCompanyName(element.getText());
                        }
                    }
                    positions.add(entity);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return positions;
    }

    private static List<Device> parseDevices(String resXml) {
        List<Device> devices = Lists.newArrayList();
        try {
            Document document = DocumentHelper.parseText(resXml);
            DefaultXPath xPath = new DefaultXPath("//EtData1");
            xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
            List<Node> list = xPath.selectNodes(document);
            for (Object object : list) {
                Element itemNode = (Element) object;
                List<Element> items = itemNode.elements();
                for (Element item : items) {
                    Device entity = new Device();
                    List<Element> elements = item.elements();
                    for (Element element : elements) {
                        if (element.getName().equalsIgnoreCase("HEQUI")) {
                            String parentCode;
                            if (element.getText().isEmpty()) {
                                parentCode = "0";
                            } else {
                                parentCode = element.getText();
                            }
                            entity.setParentCode(parentCode);
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
                    }
                    devices.add(entity);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return devices;
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
                .map(device -> new DeviceDTO(device.getId(), device.getParentCode(), device.getPositionCode(), device.getDeviceCode(), device.getDeviceName(), Lists.newArrayListWithCapacity(0))).collect(Collectors.toList());

            SapUser sapUser = optional.get();
            String branchCode = sapUser.getBranchCode();
            List<FunPosition> allPositions = repository.findAllByBranchCompanyCode(branchCode);
            List<FunPositionDTO> allDtos = allPositions
                .stream()
                .map(position -> mapper.toDto(position))
                .collect(Collectors.toList());
            List<FunPositionDTO> root = Lists.newArrayList();

            for (FunPosition position : allPositions) {
                if (position.getParentId().equals("0")) {
                    FunPositionDTO dto = mapper.toDto(position);
                    root.add(dto);
                }
            }

            for (FunPositionDTO dto : root) {
                List<FunPositionDTO> childList = getChild(dto.getPositionCode(), allDtos, allDeviceDto);
                dto.setChildren(childList);
            }

            return root;
        }
        return Lists.newArrayListWithExpectedSize(0);
    }

    private List<DeviceDTO> getDeviceChild(String deviceCode, List<DeviceDTO> allDtos) {
        List<DeviceDTO> childList = Lists.newArrayList();
        for (DeviceDTO dto : allDtos) {
            if (dto.getParentCode().equals(deviceCode)) {
                childList.add(dto);
            }
        }

        for (DeviceDTO dto : childList) {
            dto.setChildren(getDeviceChild(dto.getDeviceCode(), allDtos));
        }

        if (childList.size() == 0) {
            return Lists.newArrayList();
        }
        return childList;
    }

    private List<FunPositionDTO> getChild(String positionCode, List<FunPositionDTO> allDtos, List<DeviceDTO> allDeviceDto) {
        List<FunPositionDTO> childList = Lists.newArrayList();
        for (FunPositionDTO dto : allDtos) {
            if (dto.getParentId().equals(positionCode)) {
                List<DeviceDTO> deviceOfPosition = deviceRepository.findByPositionCode(dto.getPositionCode()).stream()
                    .map(device -> new DeviceDTO(device.getId(), device.getParentCode(), device.getPositionCode(), device.getDeviceCode(), device.getDeviceName(), Lists.newArrayListWithCapacity(0))).collect(Collectors.toList());

                List<DeviceDTO> rootDeviceDto = Lists.newArrayList();
                for (DeviceDTO deviceDTO : deviceOfPosition) {
                    if (deviceDTO.getParentCode().isEmpty()) {
                        rootDeviceDto.add(deviceDTO);
                    }
                }
                log.info("root device size: {}", rootDeviceDto.size());

                List<DeviceDTO> deviceChildren = Lists.newArrayList();
                for (DeviceDTO deviceDTO : rootDeviceDto) {
                    List<DeviceDTO> deviceChild = getDeviceChild(deviceDTO.getDeviceCode(), allDeviceDto);
                    deviceDTO.setChildren(deviceChild);
                    deviceChildren.add(deviceDTO);
                }
                dto.setDeviceChildren(deviceChildren);
                childList.add(dto);
            }
        }

        for (FunPositionDTO dto : childList) {
            dto.setChildren(getChild(dto.getPositionCode(), allDtos, allDeviceDto));
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
        headers.add("authorization", Constants.AUTH_CODE);
        headers.setContentType(MediaType.TEXT_XML);
        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_search_tplnr_new/888/zpm_search_tplnr_new/zpm_search_tplnr_new";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildGetPositionAndDeviceXml();

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();
        List<FunPosition> positions = parsePositions(resXml);
        List<Device> devices = parseDevices(resXml);
        for (FunPosition position : positions) {
            Optional<FunPosition> optional = repository.findByPositionCode(position.getPositionCode());
            if (!optional.isPresent()) {
                repository.save(position);
            }
        }

        for (Device device : devices) {
            Optional<Device> optional = deviceRepository.findByDeviceCode(device.getDeviceCode());
            if (!optional.isPresent()) {
                deviceRepository.save(device);
            }
        }
    }
}
