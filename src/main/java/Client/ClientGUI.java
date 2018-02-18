package Client;

import library.DefaultGUIExceptionHandler;
import library.Messages;
import network.SocketThread;
import network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, SocketThreadListener {

        public static void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ClientGUI();
                }
            });
        }

        private static final int WIDTH = 800;
        private static final int HEIGHT = 300;
        private static final String TITLE = "Dropbox client";
        private static final String [] EMPTY_STRING = new String[0];

        private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
        private final JTextField fieldIPAddr = new JTextField("127.0.0.1");
        private final JTextField fieldPort = new JTextField("8189");
        private final JCheckBox chkAlwaysOnTop = new JCheckBox("Always on top", true);
        private final JTextField fieldLogin = new JTextField("");
        private final JPasswordField fieldPass = new JPasswordField("");
        private final JButton btnLogin = new JButton("Login");

        private final JTextArea log = new JTextArea();
        private final JList<String> userList = new JList<>();

        private final JPanel bottomPanel = new JPanel(new BorderLayout());
        private final JButton btnDisconnect = new JButton("Disconnect");
        private final JTextField fieldInput = new JTextField();
        private final JButton btnSend = new JButton("Send");

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss - ");

    private ClientGUI() {
            Thread.setDefaultUncaughtExceptionHandler(new DefaultGUIExceptionHandler());
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setSize(WIDTH, HEIGHT);
            setTitle(TITLE);
            upperPanel.add(fieldIPAddr);
            upperPanel.add(fieldPort);
            upperPanel.add(chkAlwaysOnTop);
            upperPanel.add(fieldLogin);
            upperPanel.add(fieldPass);
            upperPanel.add(btnLogin);
            add(upperPanel, BorderLayout.NORTH);

            JScrollPane scrollLog = new JScrollPane(log);
            log.setEditable(false);
            log.setLineWrap(true);
            add(scrollLog, BorderLayout.CENTER);

            JScrollPane scrollUsers = new JScrollPane(userList);
            scrollUsers.setPreferredSize(new Dimension(150, 0));
            add(scrollUsers, BorderLayout.EAST);

            bottomPanel.add(btnDisconnect, BorderLayout.WEST);
            bottomPanel.add(fieldInput, BorderLayout.CENTER);
            bottomPanel.add(btnSend, BorderLayout.EAST);
            bottomPanel.setVisible(false);
            add(bottomPanel, BorderLayout.SOUTH);

            fieldIPAddr.addActionListener(this);
            fieldPort.addActionListener(this);
            fieldLogin.addActionListener(this);
            fieldPass.addActionListener(this);
            btnLogin.addActionListener(this);
            btnDisconnect.addActionListener(this);
            fieldInput.addActionListener(this);
            btnSend.addActionListener(this);
            chkAlwaysOnTop.addActionListener(this);

            setAlwaysOnTop(chkAlwaysOnTop.isSelected());
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (    src == fieldIPAddr ||
                    src == fieldPort   ||
                    src == fieldLogin  ||
                    src == fieldPass   ||
                    src == btnLogin) {
                connect();
            } else if (src == btnDisconnect) {
                disconnect();
            } else if (src == fieldInput || src == btnSend) {
                sendMsg();
            } else if (src == chkAlwaysOnTop) {
                setAlwaysOnTop(chkAlwaysOnTop.isSelected());
            } else {
                throw new RuntimeException("Unknown src = " + src);
            }
        }

        private SocketThread socketThread;

        private void connect() {
            try {
                Socket socket = new Socket(fieldIPAddr.getText(), Integer.parseInt(fieldPort.getText()));
                socketThread = new SocketThread(this, "SocketThread", socket);
            } catch (IOException e) {
                e.printStackTrace();
                log.append("Exception: " + e.getMessage() + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        }

        private void disconnect() {
            socketThread.close();
        }

        private void sendMsg() {
            String msg = fieldInput.getText();
            if(msg.equals("")) return;
            fieldInput.setText(null);
            fieldInput.requestFocus();
            socketThread.sendMsg(msg);
        }

        @Override
        public void onStartSocketThread(SocketThread socketThread) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    log.append("Поток сокета запущен.\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }
            });
        }

        @Override
        public void onStopSocketThread(SocketThread socketThread) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    log.append("Соединение потеряно.\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    upperPanel.setVisible(true);
                    bottomPanel.setVisible(false);
                    userList.setListData(EMPTY_STRING);
                    setTitle(TITLE);
                }
            });
        }

        @Override
        public void onReadySocketThread(SocketThread socketThread, Socket socket) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    log.append("Соединение установлено.\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    upperPanel.setVisible(false);
                    bottomPanel.setVisible(true);
                    String login = fieldLogin.getText();
                    String password = new String(fieldPass.getPassword());
                    socketThread.sendMsg(Messages.getAuthRequest(login, password));
                }
            });
        }

        @Override
        public void onReceiveString(SocketThread socketThread, Socket socket, String value) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    handleMsg(value);
                }
            });
        }

        private void handleMsg(String msg) {
            String[] tokens = msg.split(Messages.DELIMITER);
            String type = tokens[0];
            switch (type) {
                case Messages.AUTH_ACCEPT:
                    setTitle(TITLE + " nickname: " + tokens[1]);
                    break;
                case Messages.AUTH_ERROR:
                    log.append(dateFormat.format(System.currentTimeMillis()) + "Неправильные имя/пароль\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    break;
                case Messages.BROADCAST:
                    log.append(dateFormat.format(Long.parseLong(tokens[1])) + tokens[2] + ": " + tokens[3] + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    break;
                case Messages.USERS_LIST:
                    String allUsersMsg = msg.substring(Messages.USERS_LIST.length() + Messages.DELIMITER.length());
                    String[] users = allUsersMsg.split(Messages.DELIMITER);
                    Arrays.sort(users);
                    userList.setListData(users);
                    break;
                case Messages.RECONNECT:
                    log.append(dateFormat.format(System.currentTimeMillis()) + "Переподключён с другого клиента.\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    break;
                case Messages.MSG_FORMAT_ERROR:
                    log.append(dateFormat.format(System.currentTimeMillis()) + "Неправильный формат сообщения: '" + msg + "'\n");
                    log.setCaretPosition(log.getDocument().getLength());
                    break;
                default:
                    throw new RuntimeException("Unknown message type: " + type);
            }
        }

        @Override
        public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    e.printStackTrace();
                    log.append("Exception: " + e + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }
            });
        }
    }

