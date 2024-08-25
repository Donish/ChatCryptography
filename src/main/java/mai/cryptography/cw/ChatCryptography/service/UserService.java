package mai.cryptography.cw.ChatCryptography.service;

import lombok.AllArgsConstructor;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<String> getAllUsernames() {
        return userRepository.findAllUsernames();
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void addRoomToUser(User user, Room room) {
        user.getRooms().add(room);
        userRepository.save(user);
    }

    public boolean removeRoomFromUser(User user, Room room) {
        if (user.getRooms().contains(room)) {
            user.getRooms().remove(room);
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
