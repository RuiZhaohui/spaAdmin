package com.swiftcode.service.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * BottleneckDTO Class
 *
 * @author Ray
 * @date 2019/12/11 14:31
 */
@Data
public class BottleneckDTO implements Serializable {
    private static final long serialVersionUID = -6539465732315468975L;

    private String userCode;
    private String userName;
    private String deviceCode;
    private String deviceName;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
}
