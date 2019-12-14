package com.swiftcode.service;

import com.swiftcode.config.Constants;
import com.swiftcode.domain.SapUser;
import com.swiftcode.repository.SapUserRepository;
import com.swiftcode.service.dto.SapUserDTO;
import com.swiftcode.service.mapper.SapUserMapper;
import com.swiftcode.service.util.SapXmlUtil;
import com.swiftcode.web.rest.vm.ManagedSapUserVm;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author chen
 **/
@Slf4j
@Service
public class SapUserService {
    @Value("${sap-url}")
    private String sapUrl;
    private SapUserRepository repository;
    private SapUserMapper mapper;
    private PasswordEncoder passwordEncoder;

    public SapUserService(SapUserRepository repository, SapUserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public SapUserDTO addSapUser(SapUserDTO dto) {
        SapUser aUser = mapper.toEntity(dto);
        aUser.setPassword(passwordEncoder.encode(Constants.DEFAULT_PWD));
        SapUser sapUser = repository.save(aUser);
        return mapper.toDto(sapUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public SapUserDTO updateSapUser(ManagedSapUserVm sapUserVm) {
        SapUser user = repository.findByUserCode(sapUserVm.getPERNR()).orElse(new SapUser());
        SapUser aUser = mapper.toEntity(sapUserVm);
        aUser.setId(user.getId());
        if (!StringUtils.isEmpty(sapUserVm.getPassword())) {
            aUser.setPassword(passwordEncoder.encode(sapUserVm.getPassword()));
        } else {
            aUser.setPassword(user.getPassword());
        }
        if (!StringUtils.isEmpty(sapUserVm.getPhoneNumber())) {
            aUser.setPhoneNumber(sapUserVm.getPhoneNumber());
        } else {
            aUser.setPhoneNumber(user.getPhoneNumber());
        }
        SapUser sapUser = repository.save(aUser);
        return mapper.toDto(sapUser);
    }

    /**
     * 释放维修人员
     *
     * @param userCode 员工工号
     * @throws URISyntaxException URISyntaxException
     */
    public boolean releaseUser(String userCode) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", Constants.AUTH_CODE);
        headers.setContentType(MediaType.TEXT_XML);

        String url = sapUrl + "/sap/bc/srt/rfc/sap/zpm_release_pernr/888/zpm_release_pernr/zpm_release_pernr";
        URI uri = new URI(url);
        String xml = SapXmlUtil.buildReleaseUserXml(userCode);

        HttpEntity<String> request = new HttpEntity<>(xml, headers);
        ResponseEntity<String> entity = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        String resXml = entity.getBody();
        return parseReleaseUserResult(resXml);
    }

    private boolean parseReleaseUserResult(String resXml) {
        try {
            Document document = DocumentHelper.parseText(resXml);
            DefaultXPath xpath = new DefaultXPath("//MType");
            Node node = xpath.selectSingleNode(document);
            log.info("node: {}, value: {}", node, node.getText());
            if (!"e".equals(node.getText())) {
                return true;
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public SapUserDTO findByUserCode(String userCode) {
        SapUser sapUser = repository.findByUserCode(userCode).orElse(new SapUser());
        return mapper.toDto(sapUser);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public List<SapUser> findAll() {
        return repository.findAll();
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Boolean isExist(String userCode) {
        return repository.findByUserCode(userCode).isPresent();
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userCode) {
        repository.findByUserCode(userCode).ifPresent(sapUser -> {
            sapUser.setPassword(passwordEncoder.encode(Constants.DEFAULT_PWD));
            repository.save(sapUser);
        });
    }
}
