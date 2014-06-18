package com.attestator.common.shared;

public class SharedConstants {
    public static String CLIENT_ID_COOKIE_NAME = "clientId";
    public static int    SECONDS_IN_YEAR = 60 * 60 * 24 * 365;
    public static long   MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    public static String DATE_TRANSFER_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static String VALUES_SEPARATOR = ", ";
    public static String EMAIL_VALIDATION_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static String USERNAME_VALIDATION_REGEX = "^[a-z][a-z0-9]*$";
}
