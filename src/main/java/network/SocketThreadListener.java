package network;

import java.net.Socket;

/** Interface socket thread listener. */
public interface SocketThreadListener {

    void onStartSocketThread(SocketThread socketThread);
    void onStopSocketThread(SocketThread socketThread);

    void onReadySocketThread(SocketThread socketThread, Socket socket);
    void onReceiveString(SocketThread socketThread, Socket socket, String value);

    void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e);

}