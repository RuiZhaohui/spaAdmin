package com.swiftcode.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 功能位置
 *
 * @author chen
 **/
@Data
@Entity
@Table(name = "sap_fun_position")
public class FunPosition implements Serializable {
    private static final long serialVersionUID = -3969368004470218633L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint COMMENT '主键，自动生成'")
    private Long id;
    @Column(name = "parent_code", columnDefinition = "bigint COMMENT '父节点ID'")
    private String parentId;
    @Column(name = "branch_company_code", columnDefinition = "varchar(10) COMMENT '分公司编码'")
    private String branchCompanyCode;
    @Column(name = "branch_company_name", columnDefinition = "varchar(10) COMMENT '分公司名称'")
    private String branchCompanyName;
    @Column(name = "position_code", columnDefinition = "varchar(30) COMMENT '功能位置编码'")
    private String positionCode;
    @Column(name = "position_name", columnDefinition = "varchar(30) COMMENT '功能位置名称'")
    private String positionName;
}
