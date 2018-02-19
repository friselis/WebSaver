package Client;

import library.DefaultGUIExceptionHandler;
import library.Messages;
import network.SocketThread;
import network.SocketThreadListener;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/** Client GUI. */
public class ClientGUI extends JFrame implements ActionListener, SocketThreadListener {

    private static Logger clientGuiLog = Logger.getLogger(ClientGUI.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clientGuiLog.debug("Client GUI is starting.");
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

    private final JPanel actionsToFiles = new JPanel();
    private final JList<String> fileList = new JList<>();

    private final JPanel eastPanel = new JPanel(new GridLayout(4, 1));
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JButton btnUpload = new JButton("Upload file");
    private final JButton btnSave = new JButton("Save file");
    private final JButton btnDelete = new JButton("Delete file");

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
        add(actionsToFiles, BorderLayout.EAST);

        JScrollPane scrollFiles = new JScrollPane(fileList);
        scrollFiles.setPreferredSize(new Dimension(680, 0));
        add(scrollFiles, BorderLayout.WEST);

        eastPanel.add(btnUpload);
        eastPanel.add(btnSave);
        eastPanel.add(btnDelete);
        eastPanel.add(btnDisconnect);
        eastPanel.setVisible(false);
        add(eastPanel, BorderLayout.EAST);

        fieldIPAddr.addActionListener(this);
        fieldPort.addActionListener(this);
        fieldLogin.addActionListener(this);
        fieldPass.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
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
            clientGuiLog.debug("Exception: " + e.getMessage() + "\n");
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clientGuiLog.debug("Socket thread started.\n");
            }
        });
    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clientGuiLog.debug("Connection lost.\n");
                upperPanel.setVisible(true);
                eastPanel.setVisible(false);
                fileList.setListData(EMPTY_STRING);
                setTitle(TITLE);
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clientGuiLog.debug("Connection established.\n");
                upperPanel.setVisible(false);
                eastPanel.setVisible(true);
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
                clientGuiLog.debug(dateFormat.format(System.currentTimeMillis()) + "Wrong login/password\n");
                break;
            case Messages.BROADCAST:
                clientGuiLog.debug(dateFormat.format(Long.parseLong(tokens[1])) + tokens[2] + ": " + tokens[3] + "\n");
                break;
            case Messages.FILES_LIST:
                String allUsersMsg = msg.substring(Messages.FILES_LIST.length() + Messages.DELIMITER.length());
                String[] files = allUsersMsg.split(Messages.DELIMITER);
                Arrays.sort(files);
                fileList.setListData(files);
                break;
            case Messages.RECONNECT:
                clientGuiLog.debug(dateFormat.format(System.currentTimeMillis()) + "Reconnect from a different client.\n");
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
                clientGuiLog.debug("Exception: " + e + "\n");
            }
        });
    }
}

