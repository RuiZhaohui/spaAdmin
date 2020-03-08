package com.swiftcode.service;

import cn.hutool.core.date.DateTime;
import com.google.common.collect.Lists;
import com.swiftcode.domain.Device;
import com.swiftcode.domain.FunPosition;
import com.swiftcode.domain.Malfunction;
import com.swiftcode.repository.MalfunctionRepository;
import com.swiftcode.service.dto.MalfunctionDTO;
import com.swiftcode.service.mapper.MalfunctionMapper;
import com.swiftcode.service.util.SapXmlUtil;
import org.dom4j.*;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author chen
 **/
@Service
public class MalfunctionService {
    private MalfunctionRepository repository;
    private MalfunctionMapper mapper;

    @Value("${sap-url}")
    private String sapUrl;
    @Value("${sap-secret}")
    private String sapSecret;

    public MalfunctionService(MalfunctionRepository repository, MalfunctionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public MalfunctionDTO add(MalfunctionDTO dto) {
        Malfunction malfunction = repository.findBySapNo(dto.getSapNo()).orElse(new Malfunction());
        malfunction.newMalfunction(dto.getUserCode(), dto.getLocation(), dto.getDevice(), dto.getPictures(), dto.getVideo(), dto.getAudio(), dto.getTarget(), dto.getDesc(), dto.getAddDesc(), dto.getRemark(), dto.getIsStop(), dto.getTitle(), dto.getSapNo());
        Malfunction entity = repository.save(malfunction);
        return mapper.toDto(entity);
    }

    public MalfunctionDTO link(MalfunctionDTO dto) {
        Malfunction malfunction = repository.findById(dto.getId()).orElseThrow(IllegalArgumentException::new);
        malfunction.linkMalfunction(dto.getTitle(), dto.getSapNo());
        repository.save(malfunction);
        return mapper.toDto(malfunction);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Optional<MalfunctionDTO> findByTradeNo(String tradeNo) {
        return Optional.of(repository.findByTradeNo(tradeNo))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(entity -> mapper.toDto(entity));
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Optional<MalfunctionDTO> findById(Long id) {
        return repository.findById(id)
            .map(entity -> mapper.toDto(entity));
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Optional<MalfunctionDTO> findBySapNo(String sapNo) {
        return Optional.of(repository.findBySapNo(sapNo))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(entity -> mapper.toDto(entity));
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<MalfunctionDTO> findByUser(String userCode) {
        return repository.findAllByUserCode(userCode)
            .stream()
            .map(entity -> mapper.toDto(entity))
            .collect(Collectors.toList());
    }

    private static List<Malfunction> parseOrders(String rexXml) {
        List<Malfunction> malfunctions = Lists.newArrayList();
        try {
            Document document = DocumentHelper.parseText(rexXml);
            DefaultXPath xPath = new DefaultXPath("//EtData");
            xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
            List<Node> list = xPath.selectNodes(document);
            for (Object o : list) {
                Element itemNode = (Element) o;
                List<Element> items = itemNode.elements();
                for (Element item : items) {
                    Malfunction notificationOrder = new Malfunction();
                    Malfunction repairOrder = new Malfunction();
                    String tradeNo = (DateTime.now().getTime() + UUID.randomUUID().toString().substring(0,3)).substring(0, 16);
                    notificationOrder.setTradeNo(tradeNo);
                    repairOrder.setTradeNo(tradeNo);
                    List<Element> elements = item.elements();
                    for (Element element : elements) {
                        if ("PERNR".equalsIgnoreCase(element.getName())) {
                            notificationOrder.setUserCode(element.getText());
                            repairOrder.setUserCode(element.getText());
                        } else if ("AUFNR".equalsIgnoreCase(element.getName())) {
                            if (!element.getText().isEmpty()) {
                                repairOrder.setType(1);
                                repairOrder.setSapNo(element.getText());
                            }
                        } else if ("QMNUM".equalsIgnoreCase(element.getName())) {
                            notificationOrder.setType(0);
                            notificationOrder.setSapNo(element.getText());
                        }
                    }
                    if (!StringUtils.isEmpty(repairOrder.getSapNo())) {
                        malfunctions.add(repairOrder);
                    }
                    if (!StringUtils.isEmpty(notificationOrder.getSapNo())) {
                        malfunctions.add(notificationOrder);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return malfunctions;
    }

    /**
     * 获取订单
     *
     * @throws URISyntaxException URISyntaxException
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncOrders() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);
        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_export_order/888/zpm_export_order/zpm_export_order";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildOrderXml();

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();
        List<Malfunction> malfunctions = parseOrders(resXml);
        for (Malfunction malfunction : malfunctions) {
            Optional<Malfunction> optional = repository.findBySapNo(malfunction.getSapNo());
            if (!optional.isPresent()) {
                repository.save(malfunction);
            }
        }
    }
}
