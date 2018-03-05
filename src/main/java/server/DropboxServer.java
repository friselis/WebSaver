package server;

import Client.ClientGUI;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import database.Directory;
import database.DirectoryRepository;
import database.User;
import database.UserRepository;
import library.Messages;
import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import static com.google.common.base.Preconditions.checkState;

/** Dropbox server. */
public class DropboxServer implements ServerSocketThreadListener, SocketThreadListener {

    private static Logger logger = Logger.getLogger(DropboxServer.class);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final DropboxServerListener eventListener;
    private final AuthService authService;
    private ServerSocketThread serverSocketThread;
    private final Vector<SocketThread> clients = new Vector<>();
    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;

    public DropboxServer(DropboxServerListener eventListener, AuthService authService,
                         DirectoryRepository directoryRepository, UserRepository userRepository) {
        this.eventListener = eventListener;
        this.authService = authService;
        this.directoryRepository = directoryRepository;
        this.userRepository = userRepository;
    }

    public void startListening(int port) {
        if(serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Server thread already started.");
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 2000);
        authService.start();
    }

    public synchronized void dropAllClients() {
        for (int i = 0; i < clients.size(); i++) clients.get(i).close();
    }

    public void stopListening() {
        if(serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Server thread is not started.");
            return;
        }
        serverSocketThread.interrupt();
        authService.stop();
    }

    private synchronized void putLog(String msg) {
        String msgLog = dateFormat.format(System.currentTimeMillis());
        msgLog += Thread.currentThread().getName() + ": " + msg;
        eventListener.onLogChatServer(this, msgLog);
    }


    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("stopped.");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("ServerSocket is ready...");
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
//        putLog("accept() timeout");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ":" + socket.getPort();
        new DropboxSocketThread(this, threadName, socket);
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }


    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        putLog("stopped.");
        clients.remove(socketThread);
        DropboxSocketThread client = (DropboxSocketThread) socketThread;
        if(client.isAuthorized() && !client.isReconnected()) {
            sendToAllAuthorizedClients(Messages.getBroadcast("server", client.getNickname() + " disconnected."));
            sendToAllAuthorizedClients(Messages.getFilesList(getAllNicknamesString()));
        }
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready.");
        clients.add(socketThread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String value) {
        DropboxSocketThread client = (DropboxSocketThread) socketThread;
        if(client.isAuthorized()) {
            handleAuthorizeClient(client, value);
        } else {
            handleNonAuthorizeClient(client, value);
        }
    }

    private void handleAuthorizeClient(DropboxSocketThread client, String msg) {
        List<String> tokens = Splitter.on(Messages.DELIMITER).splitToList(msg);
        String type = tokens.get(0);
        String login = client.getNickname();
        Directory directory = getDirectory(login);
        switch (type) {
            case Messages.FILES_LIST:
                client.sendMsg(getFilesList(directory));
                break;
            case Messages.FILE_UPLOAD:
                logger.debug("File uploading started.");
                Path path = Paths.get(directory.path, tokens.get(1));
                File file = path.toFile();
                try {
                    file.createNewFile();
                    ReceiveFileThread receiveFileThread = new ReceiveFileThread(file);
                    logger.debug("Created ReceiveFileThread object: " + receiveFileThread);
                    client.sendMsg(Messages.getSocketOpened(receiveFileThread.getPort(), tokens.get(2)));
                    logger.debug(String.format("port: %d", receiveFileThread.getPort()));
                    receiveFileThread.start();
                    logger.debug("File is uploaded.");
                    client.sendMsg(getFilesList(directory));
                } catch (IOException e) {
                }

        }
    }

    private Directory getDirectory(String login) {
        User user = userRepository.findByLogin(login);
        checkState(user != null, String.format("Did not find user for login: %s", login));
        Directory directory = directoryRepository.findByUserId(user.id);
        checkState(directory != null, String.format(
                "Did not find dicrectory for userId: %s (login: %s)", user.id, login));
        return directory;
    }

    private String getFilesList(Directory directory) {
        File[] listOfFiles = FileSystemUtil.getFilesFromDirectory(directory.path);
        String[] filesNames = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            filesNames[i] = listOfFiles[i].getName();
        }
        String files = Joiner.on(Messages.DELIMITER).join(filesNames);
        String response = Messages.getFilesList(files);
        return response;
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            DropboxSocketThread client = (DropboxSocketThread) clients.get(i);
            if(client.isAuthorized()) client.sendMsg(msg);
        }
    }

    private void handleNonAuthorizeClient(DropboxSocketThread newClient, String msg) {
        String tokens[] = msg.split(Messages.DELIMITER);
        if(tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)) {
            newClient.messageFormatError(msg);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = authService.getNickname(login, password);
        if(nickname == null) {
            newClient.authError();
            return;
        }
        DropboxSocketThread oldClient = getClientByNickname(nickname);
        newClient.authAccept(nickname);
        if (oldClient == null) {
            sendToAllAuthorizedClients(Messages.getBroadcast("server", newClient.getNickname() + " connected."));
        } else {
            oldClient.reconnected();
        }
    }

    private DropboxSocketThread getClientByNickname(String nickname) {
        final int cnt = clients.size();
        for (int i = 0; i < cnt; i++) {
            DropboxSocketThread client = (DropboxSocketThread) clients.get(i);
            if(!client.isAuthorized()) continue;
            if(client.getNickname().equals(nickname)) return client;
        }
        return null;
    }

    private String getAllNicknamesString() {
        StringBuilder sb = new StringBuilder();
        final int cnt = clients.size();
        final int last = cnt - 1;
        for (int i = 0; i < cnt; i++) {
            DropboxSocketThread client = (DropboxSocketThread) clients.get(i);
            if(!client.isAuthorized() || client.isReconnected()) continue;
            sb.append(client.getNickname());
            if(i != last) sb.append(Messages.DELIMITER);
        }
        return sb.toString();
    }

    private String[] getFilesList() {
        String[] files = {"1", "2", "3"};
        return files;
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e);
    }
}

