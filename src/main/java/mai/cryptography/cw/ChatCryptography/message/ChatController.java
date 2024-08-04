package mai.cryptography.cw.ChatCryptography.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private ChatService chatService;

    @GetMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ChatMessage> getMessage() {
        return chatService.getMessages();
    }

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMessage(@RequestBody ChatMessage message) {
        chatService.addMessage(message);
    }
}
