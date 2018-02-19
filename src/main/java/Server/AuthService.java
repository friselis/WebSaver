package Server;

/**Interface of authentification service**/
public interface AuthService {
    void start();
    String getNickname(String login, String password);
    void stop();
}