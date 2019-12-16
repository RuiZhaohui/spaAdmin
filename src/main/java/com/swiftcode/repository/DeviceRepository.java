package com.swiftcode.repository;

import com.swiftcode.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author chen
 **/
public interface DeviceRepository extends JpaRepository<Device, Long> {
    /**
     * 根据功能位置ID查找设备列表
     *
     * @param positionCode 功能位置编码
     * @return 设备列表
     */
    List<Device> findByPositionCode(String positionCode);
}
