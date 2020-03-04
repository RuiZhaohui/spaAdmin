package com.swiftcode.service;

import com.swiftcode.config.Constants;
import com.swiftcode.config.MyErrorHandle;
import com.swiftcode.service.dto.BottleneckDTO;
import com.swiftcode.service.dto.UserDeviceDTO;
import com.swiftcode.service.util.SapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BottleneckService Class
 *
 * @author Ray
 * @date 2019/12/11 14:38
 */
@Slf4j
@Service
public class BottleneckService {

    @Value("${sap-url}")
    private String sapUrl;
    @Value("${sap-secret}")
    private String sapSecret;

    private static Boolean checkImportResult(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            DefaultXPath xpath = new DefaultXPath("//Message");
            List<Node> list = xpath.selectNodes(document);
            for (Object o : list) {
                Element node = (Element) o;
                if (node.getText().equals("上传成功")) {
                    return true;
                }
            }
        } catch (DocumentException e) {
            throw new IllegalArgumentException("解析XML出错");
        }
        return false;
    }

    /**
     * 解析设备台账数据
     *
     * @param resXml resXml
     * @return 用户对应的设备台账列表
     */
    private static List<UserDeviceDTO> parseDeviceXml(String resXml) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(resXml);
        } catch (DocumentException e) {
            log.error("解析功能位置出错: {}", e.getMessage());
        }
        DefaultXPath xpath = new DefaultXPath("//EtData");
        xpath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
        return xpath.selectNodes(document).stream()
            .flatMap(itemNode -> ((Element) itemNode).elements().stream())
            .map(node -> {
                List<Element> items = node.elements();
                UserDeviceDTO dto = new UserDeviceDTO();
                items.forEach(item -> {
                    if (item.getName().equalsIgnoreCase("EQUNR")) {
                        dto.setDeviceCode(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("EQKTX")) {
                        dto.setDeviceName(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("TPLNR")) {
                        dto.setFunctionPositionCode(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("PLTXT")) {
                        dto.setFunctionPositionName(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("BOEQ")) {
                        dto.setBottleneckDevice(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("RTIME")) {
                        dto.setRestTime(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("SWERK")) {
                        dto.setFactoryCode(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("TXTMD")) {
                        dto.setFactoryName(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("ANLNR")) {
                        dto.setCareCode(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("ABCKZ")) {
                        dto.setAbcCode(item.getText());
                    }
                    if (item.getName().equalsIgnoreCase("ABCTX")) {
                        dto.setAbcName(item.getText());
                    }
                });
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * 调整瓶颈机台
     *
     * @param bottleneckDTO bottleneckDTO
     * @return 是否成功
     * @throws URISyntaxException URISyntaxException
     */
    public Boolean importDevice(BottleneckDTO bottleneckDTO) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new MyErrorHandle());
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);

        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_import_equnr/888/zpm_import_equnr/zpm_import_equnr";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildImportDeviceXml(bottleneckDTO);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();
        log.info("resXml: {}", resXml);
        Boolean result = checkImportResult(resXml);
        log.info("import result: {}", result);
        return result;
    }

    /**
     * 根据工号查找设备台账
     *
     * @param userCode 工号
     * @return 设备台账列表
     * @throws URISyntaxException URISyntaxException
     */
    public List<UserDeviceDTO> findUserDevices(String userCode) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);

        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_search_equnr/888/zpm_search_equnr/zpm_search_equnr";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildUserDevicesXml(userCode);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();
        return parseDeviceXml(resXml);
    }
}
