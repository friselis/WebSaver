package ServerGUI;

import Client.ClientGUI;
import Server.DropboxServer;
import Server.DropboxServerListener;
import Server.DropboxSocketThread;
import Server.SimpleAuthService;
import library.DefaultGUIExceptionHandler;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**Server GUI**/
public class ServerGUI extends JFrame implements ActionListener, DropboxServerListener {

    private static Logger serverGuiLog = Logger.getLogger(ServerGUI.class);

    private static final int POS_X = 1100;
    private static final int POS_Y = 150;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;

    private static final String TITLE = "Dropbox Server";
    private static final String START_LISTENING = "Start listening";
    private static final String DROP_ALL_CLIENTS = "Drop all clients";
    private static final String STOP_LISTENING = "Stop listening";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                serverGuiLog.debug("Server GUI is starting.");
                new ServerGUI();
            }
        });
    }

    private final DropboxServer dropboxServer = new DropboxServer(this, new SimpleAuthService());
    private final JButton btnStartListening = new JButton(START_LISTENING);
    private final JButton btnStopListening = new JButton(STOP_LISTENING);
    private final JButton btnDropAllClients = new JButton(DROP_ALL_CLIENTS);
    private final JTextArea log = new JTextArea();

    private ServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultGUIExceptionHandler());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle(TITLE);

        JPanel upperPanel = new JPanel(new GridLayout(1, 3));
        upperPanel.add(btnStartListening);
        upperPanel.add(btnStopListening);
        upperPanel.add(btnDropAllClients);
        add(upperPanel, BorderLayout.NORTH);

        JScrollPane scrollLog = new JScrollPane(log);
        log.setEditable(false);
        add(scrollLog, BorderLayout.CENTER);

        btnStartListening.addActionListener(this);
        btnStopListening.addActionListener(this);
        btnDropAllClients.addActionListener(this);

        setAlwaysOnTop(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStartListening) {
            serverGuiLog.debug("dropboxServer start listening port + " + 8189 + ".");
            dropboxServer.startListening(8189);
        } else if (src == btnDropAllClients) {
            serverGuiLog.debug("dropboxServer drops all clients.");
            dropboxServer.dropAllClients();
        } else if (src == btnStopListening) {
            serverGuiLog.debug("dropboxServer stops listening.");
            dropboxServer.stopListening();
        } else {
            serverGuiLog.debug("Unknown src = " + src);
            throw new RuntimeException("Unknown src = " + src);
        }
    }

    @Override
    public void onLogChatServer(DropboxServer dropboxServer, String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                serverGuiLog.debug(msg + "\n");
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}