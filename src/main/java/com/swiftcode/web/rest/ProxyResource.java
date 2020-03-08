package com.swiftcode.web.rest;

import com.swiftcode.service.ProxyService;
import com.swiftcode.service.dto.UserDeviceDTO;
import com.swiftcode.service.util.CommonResult;
import com.swiftcode.web.rest.errors.BadRequestAlertException;
import com.swiftcode.web.rest.errors.SearchUserDevicesException;
import com.swiftcode.web.rest.vm.ProxyVM;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

/**
 * ProxyResource Class
 *
 * @author Ray
 * @date 2020/02/29 16:41
 */
@RestController
@RequestMapping("/api/sap")
public class ProxyResource {

    private ProxyService proxyService;

    public ProxyResource(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @PostMapping("/proxy")
    public CommonResult<String> proxy(@RequestBody ProxyVM proxyVM) {
        try {
            String resXml = proxyService.proxy(proxyVM.getPath(), proxyVM.getXml());
            return CommonResult.success(resXml);
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
    }
}
