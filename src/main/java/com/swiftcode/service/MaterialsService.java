package com.swiftcode.service;

import com.swiftcode.config.Constants;
import com.swiftcode.domain.Materials;
import com.swiftcode.repository.MaterialsRepository;
import com.swiftcode.service.util.SapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
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
 * @author chen
 **/
@Slf4j
@Service
public class MaterialsService {
    @Value("${sap-url}")
    private String sapUrl;
    @Value("${sap-secret}")
    private String sapSecret;

    private MaterialsRepository repository;

    public MaterialsService(MaterialsRepository repository) {
        this.repository = repository;
    }

    private static List<Materials> parseMaterials(String resXml) throws DocumentException {
        Document document = DocumentHelper.parseText(resXml);
        DefaultXPath xPath = new DefaultXPath("//EtData");
        xPath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
        return xPath.selectNodes(document).stream()
            .flatMap(itemNode -> ((Element) itemNode).elements().stream())
            .map(node -> {
                List<Element> items = node.elements();
                Materials entity = new Materials();
                items.forEach(element -> {
                    if (element.getName().equalsIgnoreCase("MATNR")) {
                        entity.setMaterialsCode(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("MAKTX")) {
                        entity.setMaterialsName(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("WERKS")) {
                        entity.setFactoryCode(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("NAME")) {
                        entity.setFactoryName(element.getText());
                    }
                    if (element.getName().equalsIgnoreCase("MEINS")) {
                        entity.setUnit(element.getText());
                    }
                });
                return entity;
            })
            .collect(Collectors.toList());

    }

    /**
     * 根据物料编码查询物料信息
     *
     * @param code 物料编码
     * @return 物料信息
     */
    public Materials findByCode(String code) {
        return repository.findByMaterialsCode(code).orElse(null);
    }

    /**
     * 同步物料数据
     *
     * @throws URISyntaxException URISyntaxException
     */
    @Transactional(rollbackFor = Exception.class)
    @Async
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncMaterials() throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);
        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_search_matnr/888/zpm_search_matnr/zpm_search_matnr";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildMaterialsXml();

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();

        List<Materials> materialsList = null;
        try {
            materialsList = parseMaterials(resXml);
        } catch (DocumentException e) {
            try {
                materialsList = parseMaterials(resXml);
            } catch (DocumentException ex) {
                log.error("同步物料数据出错: {}", ex.getMessage());
            }
        }
        if (null != materialsList) {
            for (Materials materials : materialsList) {
                Optional<Materials> optional = repository.findByMaterialsCode(materials.getMaterialsCode());
                if (!optional.isPresent()) {
                    repository.save(materials);
                }
            }
        }
    }
}
