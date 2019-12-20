package com.swiftcode.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 物料
 *
 * @author chen
 **/
@Data
@Entity
@Table(name = "sap_materials")
public class Materials implements Serializable {
    private static final long serialVersionUID = 3835477873754414681L;
    @Id
    @Column(name = "materials_code", columnDefinition = "varchar(20) COMMENT '主键，物料代码'")
    private String materialsCode;
    @Column(name = "materials_name", columnDefinition = "varchar(40) COMMENT '物料名称'")
    private String materialsName;
    @Column(name = "factory_code", columnDefinition = "varchar(20) COMMENT '工厂代码'")
    private String factoryCode;
    @Column(name = "factory_name", columnDefinition = "varchar(20) COMMENT '工厂名称'")
    private String factoryName;
    @Column(name = "unit", columnDefinition = "varchar(20) COMMENT '物料单位'")
    private String unit;
}
