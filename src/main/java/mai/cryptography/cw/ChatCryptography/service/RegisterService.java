package mai.cryptography.cw.ChatCryptography.service;

import com.vaadin.flow.server.VaadinSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.model.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public static class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }

    @Transactional
    public User registration(String username, String password) throws Exception {
        Optional<User> possibleUser = userRepository.findByUsername(username);

        if (!validateUsername(username)) {
            throw new IllegalArgumentException("Passed invalid username");
        }
        if (possibleUser.isPresent()) {
            throw new RegisterException("User already exists");
        }

        User user = User.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password))
                .build();


        userRepository.save(user);
        VaadinSession.getCurrent().setAttribute(User.class, user);
        return user;
    }

    @Transactional
    public User login(String username, String password) throws Exception {
        Optional<User> possibleUser = userRepository.findByUsername(username);

        if (possibleUser.isPresent()) {
            User user = possibleUser.get();
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                VaadinSession.getCurrent().setAttribute(User.class, user);
                return user;
            }
        }

        throw new RegisterException("User not found");
    }

    private boolean validateUsername(String username) {
        return username.matches("\\w+") && username.length() <= 20;
    }
}
