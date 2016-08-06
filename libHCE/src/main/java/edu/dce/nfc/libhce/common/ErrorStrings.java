package edu.dce.nfc.libhce.common;

/**
 * Created by brandonau on 8/4/16.
 */
public class ErrorStrings {

    public static final String ERROR_UNKNOWN_COMMAND = "error_unknown_command";

    public static final String ERROR_SECURE_CONNECT = "error_secure_connect";

    public static final String ERROR_OPEN_ROOM = "error_open_room";

    public static final String SUCCESS_OPEN_ROOM = "success_open_room";

    public static final String ERROR_NO_ROOM = "error_no_room";

    public static final String ERROR_CHECK_IN = "error_check_in";

    public static final String SUCCESS_CHECK_IN = "success_check_in";

    String mType;
    String mError;

    public ErrorStrings(String type, String error) {
        mType = type;
        mError = error;
    }
}
