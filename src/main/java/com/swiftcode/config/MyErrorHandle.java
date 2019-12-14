package com.swiftcode.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * @author chen
 **/
@Slf4j
public class MyErrorHandle extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        int status = statusCode.value();
        if (status == 200 || status == 400 || status == 500) {
            log.info("response: {}", response);
        } else {
            super.handleError(response, statusCode);
        }
    }
}
