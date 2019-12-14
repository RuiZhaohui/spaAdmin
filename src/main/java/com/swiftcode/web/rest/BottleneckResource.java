package com.swiftcode.web.rest;

import com.swiftcode.service.BottleneckService;
import com.swiftcode.service.dto.BottleneckDTO;
import com.swiftcode.service.dto.UserDeviceDTO;
import com.swiftcode.service.util.CommonResult;
import com.swiftcode.web.rest.errors.SearchUserDevicesException;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author chen
 **/
@RestController
@RequestMapping("/api/sap")
public class BottleneckResource {
    private BottleneckService service;

    public BottleneckResource(BottleneckService service) {
        this.service = service;
    }

    @GetMapping("/bottleneck")
    public CommonResult<List<UserDeviceDTO>> findUserDevices(@RequestParam("userCode") String userCode) {
        try {
            List<UserDeviceDTO> userDevices = service.findUserDevices(userCode);
            return CommonResult.success(userDevices);
        } catch (URISyntaxException e) {
            throw new SearchUserDevicesException();
        }
    }

    @PostMapping("/bottleneck")
    public CommonResult<Boolean> importDevice(@RequestBody BottleneckDTO dto) {
        try {
            Boolean result = service.importDevice(dto);
            return CommonResult.success(result);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException();
        }
    }
}
