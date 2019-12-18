package com.swiftcode.service.dto;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author chen
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO implements Serializable {
    private static final long serialVersionUID = 1336764195714329114L;
    private Long id;
    @ApiModelProperty(value = "父节点ID")
    private String parentCode;
    @ApiModelProperty(value = "功能位置ID")
    private String positionCode;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "设备子节点")
    private List<DeviceDTO> children = Lists.newArrayList();
}
