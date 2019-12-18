package com.swiftcode.service;

import com.google.common.collect.Lists;
import com.swiftcode.domain.Device;
import com.swiftcode.repository.DeviceRepository;
import com.swiftcode.service.dto.DeviceDTO;
import com.swiftcode.service.mapper.DeviceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chen
 **/
@Service
public class DeviceService {
    private DeviceRepository repository;
    private DeviceMapper mapper;

    public DeviceService(DeviceRepository repository, DeviceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeviceDTO addDevice(DeviceDTO deviceDTO) {
        Device device = repository.save(mapper.toEntity(deviceDTO));
        return mapper.toDto(device);
    }

    public DeviceDTO findByDeviceId(Long deviceId) {
        List<Device> allDevice = repository.findAll();
        List<DeviceDTO> allDtos = allDevice
            .stream()
            .map(device -> mapper.toDto(device))
            .collect(Collectors.toList());
        Optional<Device> optional = repository.findById(deviceId);

        if (optional.isPresent()) {
            DeviceDTO rootDto = mapper.toDto(optional.get());

            List<DeviceDTO> childList = getChild(rootDto.getDeviceCode(), allDtos);
            rootDto.setChildren(childList);
            return rootDto;
        }
        return null;
    }

    private List<DeviceDTO> getChild(String deviceCode, List<DeviceDTO> allDtos) {
        List<DeviceDTO> childList = Lists.newArrayList();
        for (DeviceDTO dto : allDtos) {
            if (dto.getParentCode().equals(deviceCode)) {
                childList.add(dto);
            }
        }

        for (DeviceDTO dto : childList) {
            dto.setChildren(getChild(dto.getDeviceCode(), allDtos));
        }

        if (childList.size() == 0) {
            return Lists.newArrayList();
        }
        return childList;
    }
}
