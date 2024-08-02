package mai.cryptography.cw.ChatCryptography.message;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class ChatMessage {
    private String user;
    private String message;
    private Instant timestamp;

    public ChatMessage() {}

    public ChatMessage(String user, String message, Instant timestamp) {
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
