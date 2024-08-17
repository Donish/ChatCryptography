package mai.cryptography.cw.ChatCryptography.service;

import jakarta.transaction.Transactional;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaWriter;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class ServerService {
    private final UserService userService;
    private final RoomService roomService;
    private final KafkaWriter kafkaWriter;

    @Autowired
    public ServerService(UserService userService, RoomService roomService, KafkaWriter kafkaWriter) {
        this.userService = userService;
        this.roomService = roomService;
        this.kafkaWriter = kafkaWriter;
    }

    @Transactional
    public synchronized Room createRoom(
            long userId,
            long secondUserId,
            String roomName,
            String algorithm,
            String cipherMode,
            String padding) {

        if (roomService.existsByRoomName(roomName)) {
            throw new IllegalArgumentException("Room with this name already exists");
        }

        BigInteger[] params = new BigInteger[2]; //TODO: DiffieHellman
//        byte[] g = params[0].toByteArray();
//        byte[] p = params[1].toByteArray();
        byte[] g = new byte[1];
        byte[] p = new byte[1];

        return roomService.createRoom(userId, secondUserId, roomName, algorithm, cipherMode, padding, g, p);
    }

    @Transactional
    public synchronized boolean deleteRoom(long roomId) {
        Optional<Room> possibleRoom = roomService.getRoomById(roomId);

        if (possibleRoom.isPresent()) {
            Room room = possibleRoom.get();
            Long creatorUserId = room.getCreatorUser();
            Long secondUserId = room.getSecondUser();

            if (!(disconnectRoom(creatorUserId, room.getId()) && disconnectRoom(secondUserId, room.getId()))) {
                return false;
            }

            roomService.deleteRoom(room.getId());
            return true;
        }

        return false;
    }

    @Transactional
    public synchronized boolean disconnectRoom(long userId, long roomId) {
        return true;
    }

    @Transactional
    public synchronized boolean connectRoom(long userId, long roomId) {
        return true;
    }

}
