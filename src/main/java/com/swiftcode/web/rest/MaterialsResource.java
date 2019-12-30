package com.swiftcode.web.rest;

import com.swiftcode.service.MaterialsService;
import com.swiftcode.service.util.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * @author chen
 **/
@RestController
@RequestMapping("/api/sap")
@Api(tags = "物料管理")
public class MaterialsResource {
    private MaterialsService service;

    public MaterialsResource(MaterialsService service) {
        this.service = service;
    }

    /**
     * 手动同步接口
     */
    @ApiOperation("手动同步接口")
    @GetMapping("/materials")
    public void syncMaterials() {
        try {
            service.syncMaterials();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询物料是否存在
     *
     * @param code 物料编码
     * @return 存在返回true，否则返回false
     */
    @ApiOperation("查询物料是否存在")
    @GetMapping("/materials/{code}")
    public CommonResult<Boolean> findByCode(@PathVariable("code") String code) {
        return CommonResult.success(service.findByCode(code) != null);
    }
}
