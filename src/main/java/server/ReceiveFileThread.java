package server;

import com.google.common.io.ByteStreams;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/** Open socket on a random available port and copies socket input stream to a given file. */
public class ReceiveFileThread extends Thread {

    private static final Logger logger = Logger.getLogger(ReceiveFileThread.class);
    private final File file;
//    private final int port;
   // private final String ipAddress;
    private ServerSocket serverSocket;

    public ReceiveFileThread(File file) {
        this.file = file;
//        this.port = port;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            logger.error(String.format("Failed to open server socket due to: %s", e));
        }
//        this.ipAddress = ipAddress;
    }

    @Override
    public void run() {
        logger.debug("Transferring file");
        try (
                InputStream in = serverSocket.accept().getInputStream();
                OutputStream out = new FileOutputStream(file)) {
            long numBytes = ByteStreams.copy(in, out);
            logger.debug(String.format("File received. %d bytes transfered", numBytes));
        } catch (IOException e) {
            logger.error(String.format("Failed to receive file due to: %s", e));
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}
