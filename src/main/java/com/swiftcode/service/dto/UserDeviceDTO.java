package com.swiftcode.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * UserDeviceDTO Class
 *
 * @author Ray
 * @date 2019/12/11 14:01
 */
@Data
public class UserDeviceDTO implements Serializable {
    private static final long serialVersionUID = -6539465732386548519L;

    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "功能位置编码")
    private String functionPositionCode;
    @ApiModelProperty(value = "功能位置名称")
    private String functionPositionName;
    @ApiModelProperty(value = "是否瓶颈设备")
    private String bottleneckDevice;
    @ApiModelProperty(value = "剩余时间")
    private String restTime;
    @ApiModelProperty(value = "维护工厂编码")
    private String factoryCode;
    @ApiModelProperty(value = "工厂名称")
    private String factoryName;
    @ApiModelProperty(value = "资产卡片号")
    private String careCode;
    @ApiModelProperty(value = "ABC分类编码")
    private String abcCode;
    @ApiModelProperty(value = "ABC分类名称")
    private String abcName;
}
