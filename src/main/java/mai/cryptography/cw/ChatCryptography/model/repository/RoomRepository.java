package mai.cryptography.cw.ChatCryptography.model.repository;

import mai.cryptography.cw.ChatCryptography.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
