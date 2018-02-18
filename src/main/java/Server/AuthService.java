package Server;

public interface AuthService {
    void start();
    String getNickname(String login, String password);
    void stop();
}