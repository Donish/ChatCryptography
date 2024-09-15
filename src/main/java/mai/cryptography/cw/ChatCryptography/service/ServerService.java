package mai.cryptography.cw.ChatCryptography.service;

import jakarta.transaction.Transactional;
import mai.cryptography.cw.ChatCryptography.crypto.DiffieHellmanProtocol;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaMessage;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaWriter;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

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
    public synchronized boolean createRoom(
            long userId,
            String roomName,
            String algorithm,
            String cipherMode,
            String padding) {

        if (!roomService.existsByRoomName(roomName)) {
            BigInteger[] params = DiffieHellmanProtocol.generateParameters(32);
            byte[] g = params[0].toByteArray();
            byte[] p = params[1].toByteArray();
            roomService.createRoom(userId, roomName, algorithm, cipherMode, padding, g, p);

            return true;
        }
        return false;
    }

    @Transactional
    public synchronized boolean deleteRoom(long roomId) {
        Optional<Room> possibleRoom = roomService.getRoomById(roomId);

        if (possibleRoom.isPresent()) {
            Room room = possibleRoom.get();
            boolean status;
            Set<User> users = room.getUsers();
            for (User user : users) {
                status = disconnectRoom(user.getId(), room.getId());
                if (!status) {
                    return false;
                }
            }

            roomService.deleteRoom(room.getId());
            return true;
        }

        return false;
    }

    @Transactional
    public synchronized boolean disconnectRoom(long userId, long roomId) {
        Optional<Room> maybeRoom = roomService.getRoomById(roomId);
        Optional<User> maybeUser = userService.getUserById(userId);

        if (maybeRoom.isPresent() && maybeUser.isPresent()) {
            Room room = maybeRoom.get();
            User user = maybeUser.get();

            boolean roomStatus = roomService.removeUserFromRoom(user, room);
            boolean userStatus = userService.removeRoomFromUser(user, room);

            return roomStatus && userStatus;
        }

        return false;
    }

    @Transactional
    public synchronized boolean connectRoom(long userId, long roomId) {
        Optional<Room> maybeRoom = roomService.getRoomById(roomId);
        Optional<User> maybeUser = userService.getUserById(userId);

        if (maybeRoom.isPresent() && maybeUser.isPresent()) {
            Room room = maybeRoom.get();
            User user = maybeUser.get();

            if (!(room.getUsers().contains(user))) {
                if (!(room.getUsers().size() == 2)) {
                    long consumerId = 0;
                    boolean setupConnection = false;
                    if (room.getUsers().size() == 1) {
                        consumerId = room.getUsers().iterator().next().getId();
                        setupConnection = true;
                    }

                    roomService.addUserToRoom(user, room);
                    userService.addRoomToUser(user, room);

                    if (setupConnection) {
                        exchangeInformation(userId, consumerId, roomId);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public void exchangeInformation(long producerId, long consumerId, long roomId) {
        String producerTopic = String.format("input_room_%s_user_%s", roomId, producerId);
        String consumerTopic = String.format("input_room_%s_user_%s", roomId, consumerId);

        KafkaMessage messageToUser = new KafkaMessage(KafkaMessage.Action.SETUP_CONNECTION, consumerId);
        KafkaMessage messageToOtherUser = new KafkaMessage(KafkaMessage.Action.SETUP_CONNECTION, producerId);

        kafkaWriter.write(messageToUser.toBytes(), producerTopic);
        kafkaWriter.write(messageToOtherUser.toBytes(), consumerTopic);
    }

}
