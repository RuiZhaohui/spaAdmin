package com.swiftcode.web.rest;

import cn.jpush.api.push.PushResult;
import com.swiftcode.service.JPushService;
import com.swiftcode.service.dto.JPushDTO;
import com.swiftcode.service.util.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chen
 **/
@RestController
@RequestMapping("/api/sap")
@Api(tags = "消息推送")
public class JPushResource {
    private JPushService pushService;

    public JPushResource(JPushService pushService) {
        this.pushService = pushService;
    }

    @ApiOperation("使用别名推送消息")
    @PostMapping("/push/alias")
    public CommonResult<PushResult> sendToAlias(@RequestBody JPushDTO dto) {
        PushResult result = pushService.sendToAliasList(dto.getAlias(), dto.getNotificationTitle(), dto.getMsgTitle(), dto.getMsgContent(), "extras");
        return CommonResult.success(result, "通知成功");
    }

    @ApiOperation("使用tags推送消息")
    @PostMapping("/push/tags")
    public CommonResult<PushResult> sendToTags(@RequestBody JPushDTO dto) {
        PushResult result = pushService.sendToTagsList(dto.getTagsList(), dto.getNotificationTitle(), dto.getMsgTitle(), dto.getMsgContent(), "extras");
        return CommonResult.success(result, "通知成功");
    }

    @ApiOperation("向所有安卓设备推送消息")
    @PostMapping("/push/androids")
    public CommonResult<PushResult> sendToAllAndroid(@RequestBody JPushDTO dto) {
        PushResult result = pushService.sendToAllAndroid(dto.getNotificationTitle(), dto.getMsgTitle(), dto.getMsgContent(), "extras");
        return CommonResult.success(result, "通知成功");
    }

    @ApiOperation("向所有IOS设备推送消息")
    @PostMapping("/push/ios")
    public CommonResult<PushResult> sendToAllIOS(@RequestBody JPushDTO dto) {
        PushResult result = pushService.sendToAllIOS(dto.getNotificationTitle(), dto.getMsgTitle(), dto.getMsgContent(), "extras");
        return CommonResult.success(result, "通知成功");
    }

    @ApiOperation("向所有设备推送消息")
    @PostMapping("/push/all")
    public CommonResult<PushResult> sendToAll(@RequestBody JPushDTO dto) {
        PushResult result = pushService.sendToAll(dto.getNotificationTitle(), dto.getMsgTitle(), dto.getMsgContent(), "extras");
        return CommonResult.success(result, "通知成功");
    }
}
