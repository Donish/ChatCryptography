package mai.cryptography.cw.ChatCryptography.service;

import lombok.AllArgsConstructor;
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

    public List<Long> getRooms(Long id) {
        return userRepository.getAllRooms(id);
    }
}
