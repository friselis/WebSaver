package Server;

import library.Messages;
import network.ServerSocketThread;
import network.ServerSocketThreadListener;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Vector;

/** Dropbox server. */
public class DropboxServer implements ServerSocketThreadListener, SocketThreadListener {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
        private final DropboxServerListener eventListener;
        private final AuthService authService;
        private ServerSocketThread serverSocketThread;
        private final Vector<SocketThread> clients = new Vector<>();

        public DropboxServer(DropboxServerListener eventListener, AuthService authService) {
            this.eventListener = eventListener;
            this.authService = authService;
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
                sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNickname() + " disconnected."));
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
            sendToAllAuthorizedClients(Messages.getBroadcast(client.getNickname(), msg));
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
                sendToAllAuthorizedClients(Messages.getBroadcast("Server", newClient.getNickname() + " connected."));
            } else {
                oldClient.reconnected();
            }
            sendToAllAuthorizedClients(Messages.getFilesList(getAllNicknamesString()));
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

        @Override
        public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
            putLog("Exception: " + e);
        }
    }

