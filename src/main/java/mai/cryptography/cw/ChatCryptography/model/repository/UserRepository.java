package mai.cryptography.cw.ChatCryptography.model.repository;

import mai.cryptography.cw.ChatCryptography.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
