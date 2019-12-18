package com.swiftcode.service;

import cn.jpush.api.JPushClient;

import static com.swiftcode.config.Constants.JPUSH_APP_KEY;
import static com.swiftcode.config.Constants.JPUSH_MASTER_SECRET;

/**
 * @author chen
 **/
public class JPushFactory {
    private JPushFactory() {
    }

    public static JPushClient getInstance() {
        return JPushClientHolder.INSTANCE;
    }

    private static class JPushClientHolder {
        private static final JPushClient INSTANCE = new JPushClient(JPUSH_MASTER_SECRET, JPUSH_APP_KEY);
    }
}
