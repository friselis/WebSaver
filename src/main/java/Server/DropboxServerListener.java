package Server;

/** Interface Dropbox server listener. */
public interface DropboxServerListener {
    void onLogChatServer(DropboxServer dropboxServer, String msg);
}
