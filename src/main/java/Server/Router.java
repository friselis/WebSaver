package Server;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.httpclient.HttpStatus;
import java.io.IOException;
import java.io.OutputStream;

/** Route http messages to the commands. */
public class Router {

    /** Routes an incoming request to the corresponding handler. */
    public static void route(HttpExchange httpExchange) throws IOException {
        String response = "Hellooooooo!";
        httpExchange.sendResponseHeaders(HttpStatus.SC_OK, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
