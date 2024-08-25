package mai.cryptography.cw.ChatCryptography.model.repository;

import mai.cryptography.cw.ChatCryptography.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();

}
