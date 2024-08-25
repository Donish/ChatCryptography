package mai.cryptography.cw.ChatCryptography.service;

import lombok.AllArgsConstructor;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.RoomCipherParams;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.model.repository.RoomRepository;
import mai.cryptography.cw.ChatCryptography.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoomService {
    private RoomRepository roomRepository;
    private UserRepository userRepository;

    public void createRoom(
            long userId,
            String roomName,
            String algorithm,
            String cipherMode,
            String padding,
            byte[] g,
            byte[] p) {

        RoomCipherParams roomCipherParams = RoomCipherParams.builder()
                .algorithm(algorithm)
                .cipherMode(cipherMode)
                .padding(padding)
                .IV(g) // TODO: IV generator
                .g(g)
                .p(p)
                .build();

        Room room = Room.builder()
                .roomName(roomName)
                .creatorUser(userId)
                .roomCipherParams(roomCipherParams)
                .build();

        roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    public Optional<Room> getRoomByRoomName(String roomName) {
        return roomRepository.findByRoomName(roomName);
    }

    public List<String> getAllRoomNames() {
        return roomRepository.findAllRoomNames();
    }

    public boolean existsByRoomName(String roomName) {
        return roomRepository.existsByRoomName(roomName);
    }

    public void addUserToRoom(User user, Room room) {
        room.getUsers().add(user);
        roomRepository.save(room);
    }

    public boolean removeUserFromRoom(User user, Room room) {
        if (room.getUsers().contains(user)) {
            room.getUsers().remove(user);
            roomRepository.save(room);
            return true;
        }
        return false;
    }
}
