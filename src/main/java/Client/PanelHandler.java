package Client;

import javax.swing.*;
import java.awt.*;

/**Handler for screens.*/
public class PanelHandler {

    public static void main(String[] args) {
        JFrame loginFrame = new JFrame("WebSaver");
        loginFrame.setContentPane(new LoginWindow().getLoginPanel());

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        loginFrame.setLocation(dimension.width / 2 - loginFrame.getWidth() / 2,
                dimension.height / 2 - loginFrame.getHeight() / 2);

        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.pack();
        loginFrame.setVisible(true);
    }
}
