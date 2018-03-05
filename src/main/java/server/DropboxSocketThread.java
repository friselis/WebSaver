package server;

import library.Messages;
import network.SocketThread;
import network.SocketThreadListener;

import java.net.Socket;

/** Dropbox socket thread. */
public class DropboxSocketThread extends SocketThread {

        private boolean isAuthorized;
        private boolean isReconnected;
        private String nickname;


        DropboxSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
            super(eventListener, name, socket);
        }

        boolean isAuthorized() {
            return isAuthorized;
        }

        boolean isReconnected() {
            return isReconnected;
        }

        String getNickname() {
            return nickname;
        }

        void authAccept(String nickname) {
            this.isAuthorized = true;
            this.nickname = nickname;
            sendMsg(Messages.getAuthAccept(nickname));
        }

        void authError() {
            sendMsg(Messages.getAuthError());
            close();
        }

        void reconnected() {
            isReconnected = true;
            sendMsg(Messages.getReconnect());
            close();
        }

        void messageFormatError(String msg) {
            sendMsg(Messages.getMsgFormatError(msg));
            close();
        }
    }

