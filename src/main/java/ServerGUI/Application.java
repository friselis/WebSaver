package ServerGUI;

import database.Directory;
import database.DirectoryRepository;
import database.User;
import database.UserRepository;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
@ComponentScan({"database"})
@EntityScan("database")
@EnableJpaRepositories("database")
public class Application {

    private static Logger logger = Logger.getLogger(Application.class);

    private ServerGUI serverGui;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).headless(false).build().run(args);
    }

//    @Bean
//    CommandLineRunner init(UserRepository userRepository) {
//        return (args) -> {
//            Scanner scan = new Scanner(System.in);
//            while (true) {
//                String login = scan.next();
//                if (login.equals("exit")) {
//                    break;
//                }
//                Optional<User> user = Optional.ofNullable(userRepository.findByLogin(login));
//                System.out.println(user.map(u -> u.password).orElse("null"));
//            }
//        };
//    }


    @Bean
    CommandLineRunner init(DirectoryRepository directoryRepository, UserRepository userRepository) {
        return (args) -> {
            //logger.debug("Server GUI is starting.");

//            SwingUtilities.invokeLater(() -> {
//                logger.debug("Server GUI is starting.");
//                new ServerGUI(directoryRepository, userRepository);
//            });
            serverGui = new ServerGUI(directoryRepository, userRepository);

//            Scanner scan = new Scanner(System.in);
//            while (true) {
//                String input = scan.next();
//                if (input.equals("exit")) {
//                    break;
//                }
//                Long userId = Long.parseLong(input);
//                Optional<Directory> directory = Optional.ofNullable(directoryRepository.findByUserId(userId));
//                System.out.println(directory.map(d -> d.path).orElse("none"));
//            }
        };
    }
}
