package com.swiftcode.service;

import com.swiftcode.service.util.SapXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ProxyService Class
 *
 * @author Ray
 * @date 2020/02/29 16:11
 */
@Slf4j
@Service
public class ProxyService {

    @Value("${sap-url}")
    private String sapUrl;
    @Value("${sap-secret}")
    private String sapSecret;

    public String proxy(String path, String xml) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", sapSecret);
        headers.setContentType(MediaType.TEXT_XML);

        String url = sapUrl +  "/sap/bc/srt/rfc/sap" + path;
        URI uri = new URI(url);
        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        return entity.getBody();
    }
}
