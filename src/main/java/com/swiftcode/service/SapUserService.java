package com.swiftcode.service;

import com.swiftcode.config.Constants;
import com.swiftcode.domain.SapUser;
import com.swiftcode.repository.SapUserRepository;
import com.swiftcode.service.dto.SapUserDTO;
import com.swiftcode.service.mapper.SapUserMapper;
import com.swiftcode.web.rest.vm.ManagedSapUserVm;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chen
 **/
@Service
public class SapUserService {
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

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    @Modifying
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
}
