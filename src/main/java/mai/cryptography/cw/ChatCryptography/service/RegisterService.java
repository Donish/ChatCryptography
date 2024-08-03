package mai.cryptography.cw.ChatCryptography.service;

import com.vaadin.flow.server.VaadinSession;
import lombok.AllArgsConstructor;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.model.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;

    public static class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }

    public User registration(String username, String password) throws Exception {
        Optional<User> possibleUser = userRepository.findByUsername(username);

        if (possibleUser.isPresent()) {
            throw new RegisterException("User already exists");
        }

        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        userRepository.save(user);
        VaadinSession.getCurrent().setAttribute(User.class, user);
        return user;
    }

    public User login(String username, String password) throws Exception {
        Optional<User> possibleUser = userRepository.findByUsername(username);

        if (possibleUser.isPresent()) {
            User user = possibleUser.get();
            if (user.getPassword().equals(password)) {
                VaadinSession.getCurrent().setAttribute(User.class, user);
                return user;
            }
        }

        throw new RegisterException("User not found");
    }
}
