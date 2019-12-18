package com.swiftcode.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chen
 **/
@Data
public class JPushDTO implements Serializable {
    private static final long serialVersionUID = -3012077911456001700L;
    @ApiModelProperty(value = "别名列表")
    private List<String> alias;
    @ApiModelProperty(value = "tags")
    private List<String> tagsList;
    @ApiModelProperty(value = "通知标题")
    private String notificationTitle;
    @ApiModelProperty(value = "消息标题")
    private String msgTitle;
    @ApiModelProperty(value = "消息内容")
    private String msgContent;
}
