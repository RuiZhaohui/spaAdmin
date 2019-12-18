package com.swiftcode.service;

import com.google.common.collect.Lists;
import com.swiftcode.domain.Device;
import com.swiftcode.domain.FunPosition;
import com.swiftcode.domain.SapUser;
import com.swiftcode.repository.DeviceRepository;
import com.swiftcode.repository.FunPositionRepository;
import com.swiftcode.repository.SapUserRepository;
import com.swiftcode.service.dto.DeviceDTO;
import com.swiftcode.service.dto.FunPositionDTO;
import com.swiftcode.service.mapper.FunPositionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
