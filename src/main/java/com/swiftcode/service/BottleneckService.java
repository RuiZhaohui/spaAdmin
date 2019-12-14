package com.swiftcode.service;

import com.google.common.collect.Lists;
import com.swiftcode.config.Constants;
import com.swiftcode.config.MyErrorHandle;
import com.swiftcode.service.dto.BottleneckDTO;
import com.swiftcode.service.dto.UserDeviceDTO;
import com.swiftcode.service.util.SapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    private static Boolean checkImportResult(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            DefaultXPath xpath = new DefaultXPath("//Message");
            List list = xpath.selectNodes(document);
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

    private static List<UserDeviceDTO> parseDeviceXml(String resXml) {
        List<UserDeviceDTO> dtoList = Lists.newArrayList();
        try {
            Document document = DocumentHelper.parseText(resXml);
            DefaultXPath xpath = new DefaultXPath("//EtData");
            xpath.setNamespaceURIs(Collections.singletonMap("n0", "urn:sap-com:document:sap:soap:functions:mc-style"));
            List list = xpath.selectNodes(document);
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Element node = (Element) iterator.next();
                List<Element> eleList = node.elements();
                for (Element element : eleList) {
                    List<Element> items = element.elements();
                    UserDeviceDTO dto = new UserDeviceDTO();
                    for (Element item : items) {
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
                    }
                    dtoList.add(dto);
                }
            }
            log.info("list: {} size: {}", dtoList, dtoList.size());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return dtoList;
    }

    public Boolean importDevice(BottleneckDTO bottleneckDTO) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new MyErrorHandle());
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", Constants.AUTH_CODE);
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
        headers.add("authorization", Constants.AUTH_CODE);
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
