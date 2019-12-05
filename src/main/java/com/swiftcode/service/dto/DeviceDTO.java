package com.swiftcode.service.dto;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chen
 **/
@Data
public class DeviceDTO implements Serializable {
    private static final long serialVersionUID = 1336764195714329114L;
    private Long id;
    @ApiModelProperty(value = "父节点ID")
    private Long parentId;
    @ApiModelProperty(value = "功能位置ID")
    private Long positionId;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "设备子节点")
    private List<DeviceDTO> children = Lists.newArrayList();
}
