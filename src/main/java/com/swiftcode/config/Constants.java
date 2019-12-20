package com.swiftcode.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String DEFAULT_LANGUAGE = "zh-cn";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_PWD = "123456";
//    public static final String AUTH_CODE = "Basic ZGV2MDM6MTIzNDU2";
    public static final String AUTH_CODE = "Basic UE1BUFAtRVJQOlBtYXBwKzY2Ng==";
    public static final String JPUSH_APP_KEY = "ac58b959ac802cd39ed20fbb";
    public static final String JPUSH_MASTER_SECRET = "84497fd9b6de9af2b1ddc4e5";

    private Constants() {
    }
}
