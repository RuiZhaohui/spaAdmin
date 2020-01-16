package com.swiftcode.web.rest;

import com.swiftcode.service.MalfunctionService;
import com.swiftcode.service.dto.MalfunctionDTO;
import com.swiftcode.service.util.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * @author chen
 **/
@RestController
@RequestMapping("/api")
@Api(tags = "故障单")
public class MalfunctionResource {
    private MalfunctionService service;

    public MalfunctionResource(MalfunctionService service) {
        this.service = service;
    }

    @ApiOperation("上报故障单")
    @PostMapping("/malfunction")
    public CommonResult<MalfunctionDTO> add(@RequestBody MalfunctionDTO dto) {
        MalfunctionDTO aDto = service.add(dto);
        return CommonResult.success(aDto, "添加成功");
    }

    @ApiOperation("通过更新SAP单号，关联SAP和本地故障单")
    @PostMapping("/malfunction/link")
    public CommonResult<MalfunctionDTO> link(@RequestBody MalfunctionDTO dto) {
        MalfunctionDTO link = service.link(dto);
        return CommonResult.success(link, "关联成功");
    }

    @ApiOperation("根据内部故障单和ID查询故障单信息")
    @GetMapping("/malfunction")
    public CommonResult<MalfunctionDTO> findByTradeNo(@RequestParam(value = "tradeNo", required = false) String tradeNo,
                                                      @RequestParam(value = "sapNo", required = false) String sapNo,
                                                      @RequestParam(value = "id", required = false) Long id) {
        Optional<MalfunctionDTO> dto;
        if (null != tradeNo) {
            dto = service.findByTradeNo(tradeNo);
        } else if (null != sapNo) {
            dto = service.findBySapNo(sapNo);
        } else {
            dto = service.findById(id);
        }
        return dto.map(CommonResult::success).orElseGet(CommonResult::failed);
    }

    @ApiOperation("根据用户工号查询用户上报的故障单列表")
    @GetMapping("/malfunction/user/{userCode}")
    public CommonResult<List<MalfunctionDTO>> findByUser(@PathVariable("userCode") String userCode) {
        List<MalfunctionDTO> list = service.findByUser(userCode);
        return CommonResult.success(list, "查询成功");
    }

    @ApiOperation("手动同步接口")
    @GetMapping("/malfunction/sync")
    public CommonResult<Void> syncPositionsAndDevices() {
        try {
            service.syncOrders();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return CommonResult.success(null);
    }
}
