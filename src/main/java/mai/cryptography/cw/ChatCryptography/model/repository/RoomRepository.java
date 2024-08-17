package mai.cryptography.cw.ChatCryptography.model.repository;

import mai.cryptography.cw.ChatCryptography.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r.roomName FROM Room r")
    List<String> findAllRoomNames();

    boolean existsByRoomName(String roomName);

    Optional<Room> findByRoomName(String roomName);
}
