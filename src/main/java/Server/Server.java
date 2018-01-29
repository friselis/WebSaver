package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/** Server that provides required API. */
public class Server {
    private final int port;

    /** Initialises a server instance with given port. */
    public Server(int port) {
        this.port = port;
    }

    /** Starts the server. */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RouterHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static class RouterHandler implements HttpHandler {
        Router router = new Router();

        @Override
        public void handle(HttpExchange t) throws IOException {
            router.route(t);
        }
    }

}
