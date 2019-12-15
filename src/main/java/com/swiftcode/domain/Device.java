package com.swiftcode.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 功能位置对应的设备
 *
 * @author chen
 **/
@Data
@Entity
@Table(name = "sap_device")
public class Device implements Serializable {
    private static final long serialVersionUID = 2702860039317092234L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint COMMENT '主键，自动生成'")
    private Long id;
    @Column(name = "parent_code", columnDefinition = "bigint COMMENT '父节点ID'")
    @NotNull(message = "'父节点ID' 字段不能为空")
    private String parentCode;
    @Column(name = "position_code", columnDefinition = "bigint COMMENT '设备对应的功能位置ID'")
    @NotNull(message = "'功能位置' 字段不能为空")
    private String positionCode;
    @Column(name = "device_code", columnDefinition = "varchar(30) COMMENT '设备编号'")
    @NotNull(message = "'设备编码' 字段不能为空")
    private String deviceCode;
    @Column(name = "device_name", columnDefinition = "varchar(40) COMMENT '设备名称'")
    @NotNull(message = "'设备名称' 字段不能为空")
    private String deviceName;
}
