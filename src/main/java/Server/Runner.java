package Server;

import org.apache.log4j.Logger;

import java.io.IOException;

/** An entry point with a single public method that starts the server. */
public class Runner {

    private static final int PORT = 8092;
    private static final Logger log = Logger.getLogger(Runner.class);

    /** Starts an instance of the server. */
    public static void main(String[] args) {
        Server server = new Server(PORT);
        try {
            //log.info("Starting server.");
            System.out.println("Starting server.");
            server.start();
            //log.info("Server started.");
            System.out.println("Server started.");
        } catch (IOException e) {
           // log.error("Couldn`t start server.");
            System.out.println("The server couldn`t start.");
            e.printStackTrace();
        }
    }

}
