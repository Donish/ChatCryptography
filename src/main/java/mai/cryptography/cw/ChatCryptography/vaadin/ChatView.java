package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneOffset;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import mai.cryptography.cw.ChatCryptography.message.ChatMessage;
import mai.cryptography.cw.ChatCryptography.message.UserData;
import mai.cryptography.cw.ChatCryptography.vaadin.MainLayout;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@PageTitle("Chat")
@Route(value = "chat-view/:dialogId", layout = MainLayout.class)
public class ChatView extends Composite<VerticalLayout> implements BeforeEnterObserver {


    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String username = "Anonymous";  // Set a default username or get it from session or another source
    private String dialogId;
    private MessageList messageList;
    private List<MessageListItem> messages;
    private String lastSender;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        dialogId = params.get("dialogId").orElse("default");
    }

    public ChatView() {
        messageList = new MessageList();
        messages = new ArrayList<>();
        MessageInput messageInput = new MessageInput();
        username = UserData.getInstance().getUsername();

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        getContent().setAlignSelf(FlexComponent.Alignment.CENTER, messageList);

        messageList.setWidth("100%");
        messageList.getStyle().set("flex-grow", "1");

        messageInput.addSubmitListener(event -> {
            String text = event.getValue();
            sendMessage(username, text);
//            addMessageToUI(username, text, LocalDateTime.now());
            lastSender = username; // Set the last sender when a message is sent
        });

        getContent().add(messageList, messageInput);

        connectToWebSocket();
    }

    private void sendMessage(String user, String text) {
        JsonObject messageJson = Json.createObject();
        messageJson.put("user", user);
        messageJson.put("message", text);
        messageJson.put("timestamp", LocalDateTime.now(MOSCOW_ZONE).format(FORMATTER));
        UI.getCurrent().getPage().executeJs(
                "if (window.chatSocket) { window.chatSocket.send($0); }", messageJson.toJson());
    }

    private void addMessageToUI(String user, String text, LocalDateTime timestamp) {
        // Check if the sender is different from the last sender to avoid duplicating messages
        MessageListItem messageItem = new MessageListItem(text, timestamp.atZone(MOSCOW_ZONE).toInstant(), user);
        messageItem.setUserColorIndex(user.hashCode() % 6);
        messages.add(messageItem);
        messageList.setItems(messages);

    }

    private void connectToWebSocket() {
        UI.getCurrent().getPage().executeJs(
                "window.chatSocket = new WebSocket('ws://' + window.location.host + '/chat/' + $0);" +
                        "window.chatSocket.onmessage = function(event) {" +
                        "   var message = JSON.parse(event.data);" +
                        "   var text = message.message;" +
                        "   var user = message.user;" +
                        "   var timestamp = message.timestamp;" +
                        "   $1.$server.receiveMessage(user, text, timestamp);" +
                        "};", dialogId, getElement());
    }

    @ClientCallable
    public void receiveMessage(String user, String text, String timestamp) {
        LocalDateTime time = LocalDateTime.parse(timestamp, FORMATTER);
        addMessageToUI(user, text, time);
        lastSender = user;
    }
}