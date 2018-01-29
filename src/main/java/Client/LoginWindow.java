package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**Class describes apearance and behaviour of the login window*/
public class LoginWindow {

    private static final int LOGIN_PANEL_WIDTH = 640;
    private static final int LOGIN_PANEL_HEIGHT = 480;
    private static final int RED_FOR_BLUE = 174;
    private static final int GREEN_FOR_BLUE = 226;
    private static final int BLUE_FOR_BLUE = 232;

    private JButton loginButton;
    private JPanel loginPanel;

    public LoginWindow() {

        loginPanel.setPreferredSize(new Dimension(LOGIN_PANEL_WIDTH, LOGIN_PANEL_HEIGHT));
        Color lightBlue= new Color(RED_FOR_BLUE,GREEN_FOR_BLUE,BLUE_FOR_BLUE);
        loginPanel.setBackground(lightBlue);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Hello!");
            }
        });
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JPanel getLoginPanel() {
        return loginPanel;
    }
}
