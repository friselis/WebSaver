package database;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Nullable;

/** Provides access to user data table. */
public interface UserRepository extends JpaRepository<User, Long> {

    //@Nullable
    User findByLogin(String login);

}
