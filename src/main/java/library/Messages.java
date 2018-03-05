package library;

/** Class Messages stores all messages available. */
public class Messages {

    public static final String DELIMITER =          ";";
    public static final String AUTH_REQUEST =       "/auth_request";
    public static final String AUTH_ACCEPT =        "/auth_accept";
    public static final String AUTH_ERROR =         "/auth_error";
    public static final String FILES_LIST =         "/files_list";
    public static final String RECONNECT =          "/reconnect";
    public static final String BROADCAST =          "/bcast";
    public static final String MSG_FORMAT_ERROR =   "/msg_format_error";
    public static final String FILE_UPLOAD = "/file_upload";
    public static final String SOCKET_OPENED = "/socket_opened";

    // /auth_request login password
    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    // /auth_accept nick
    public static String getAuthAccept(String nick){
        return AUTH_ACCEPT + DELIMITER + nick;
    }

    // BROADCAST time src message
    public static String getBroadcast(String src, String value){
        return BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + src + DELIMITER + value;
    }

    // /file_list
    public static String getFilesList(String files){
        return FILES_LIST + DELIMITER + files;
    }

    // AUTH_ERROR time message
    public static String getAuthError(){
        return AUTH_ERROR;
    }

    public static String getReconnect(){
        return RECONNECT;
    }

    // /msg_format_error time value
    public static String getMsgFormatError(String value){
        return MSG_FORMAT_ERROR + DELIMITER + value;
    }

    // /file_upload message
    public static String getFileName(String fileName, String fullPath) {
        return FILE_UPLOAD + DELIMITER + fileName + DELIMITER + fullPath;
    }

    public static String getSocketOpened(int port, String fileName) {
        return String.format("%s%s%d%s%s", SOCKET_OPENED, DELIMITER, port, DELIMITER, fileName);
    }
}

