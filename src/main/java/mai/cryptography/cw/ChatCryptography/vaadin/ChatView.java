package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.html.*;

import mai.cryptography.cw.ChatCryptography.crypto.CipherService;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaMessage;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaReader;
import mai.cryptography.cw.ChatCryptography.kafka.KafkaWriter;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RoomService;
import mai.cryptography.cw.ChatCryptography.service.ServerService;
import mai.cryptography.cw.ChatCryptography.service.UserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hibernate.boot.model.naming.IllegalIdentifierException;

@PageTitle("Chat")
@Route(value = "user/:userId/room/:roomId")
public class ChatView extends Composite<VerticalLayout> implements BeforeEnterObserver {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private User user;
    private User otherUser;
    private Room room;

    private final ServerService serverService;
    private final UserService userService;
    private final RoomService roomService;

    private final KafkaReader kafkaReader;
    private final KafkaWriter kafkaWriter;

    private Frontend frontend;
    private Backend backend;

    private Registration registration;
    private boolean isDisconnected;

    private String outputTopic;

    public ChatView(ServerService serverService,
                    UserService userService,
                    RoomService roomService,
                    KafkaReader kafkaReader,
                    KafkaWriter kafkaWriter) {
        this.userService = userService;
        this.roomService = roomService;
        this.serverService = serverService;
        this.kafkaReader = kafkaReader;
        this.kafkaWriter = kafkaWriter;
        this.isDisconnected = false;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> possibleUserId = event.getRouteParameters().getLong("userId");
        Optional<Long> possibleOtherUserId = event.getRouteParameters().getLong("otherUserId");
        Optional<Long> possibleRoomId = event.getRouteParameters().getLong("roomId");

        if (possibleUserId.isPresent() && possibleRoomId.isPresent() && possibleOtherUserId.isPresent()) {
            Optional<User> possibleUser = userService.getUserById(possibleUserId.get());
            Optional<User> possibleOtherUser = userService.getUserById(possibleOtherUserId.get());
            Optional<Room> possibleRoom = roomService.getRoomById(possibleRoomId.get());

            if (possibleUser.isPresent() && possibleRoom.isPresent() && possibleOtherUser.isPresent()) {
                this.user = possibleUser.get();
                this.otherUser = possibleOtherUser.get();
                this.room = possibleRoom.get();
                this.registration = Broadcaster.registration(this::receiveBroadcasterMessage);
                this.frontend = new Frontend();
                this.backend = new Backend();
            } else {
                isDisconnected = true;
            }
        } else {
            isDisconnected = true;
        }
    }

    @Override
    protected void onDetach(DetachEvent event) {
        super.onDetach(event);
        kafkaReader.stop();

        if (outputTopic != null) {
            KafkaMessage kafkaMessage = new KafkaMessage(KafkaMessage.Action.DISCONNECT, new byte[0]);
            kafkaWriter.write(kafkaMessage.toBytes(), outputTopic);
        }

        if (backend != null) {
            if (!isDisconnected) {
                if (backend.disconnect()) {
                    Broadcaster.broadcast("update");
                }
            }

            // TODO: close cipher service

        }

        if (registration != null) {
            registration.remove();
        }

    }

    private void receiveBroadcasterMessage(String message) {
        if (message.contains("delete_room")) {
            long roomId = Long.parseLong(message.split("_")[2]);
            if (roomId == this.room.getId()) {
                this.isDisconnected = true;
                updateUI(this::navigateToUserView);
            }
        }
    }

    private void navigateToUserView() {
        UI.getCurrent().navigate(UserView.class, new RouteParameters("userId", String.valueOf(user.getId())));
    }

    private void updateUI(Command command) {
        Optional<UI> possibleUI = getUI();
        possibleUI.ifPresent(ui -> ui.access(command));
    }

    private void notifyUser(String message) {
        updateUI(() -> Notification.show(message));
    }

    private class Frontend {

        private final MessageList messageList;
        private final List<MessageListItem> messages;

        public Frontend() {
            addClassNames("chat");
            this.messageList = new MessageList();
            this.messages = new ArrayList<>();

            getContent().setWidth("100%");
            getContent().getStyle().set("flex-grow", "1");
            getContent().setAlignSelf(FlexComponent.Alignment.CENTER, messageList);
            messageList.setWidth("100%");
            messageList.getStyle().set("flex-grow", "1");
            HorizontalLayout header = createHeader();

            MessageInput messageInput = new MessageInput();
            messageInput.addSubmitListener(event -> {
                String text = event.getValue();
                sendMessage(text);
            });

            getContent().add(messageList, messageInput, header);
        }

        private HorizontalLayout createHeader() {
            H2 roomName = new H2(room.getRoomName());

            Button disconnectButton = new Button("Leave chat", event -> {
                if (backend.disconnect()) {
                    isDisconnected = true;
                    notifyUser("You have left the room");
                    Broadcaster.broadcast("update");
                    navigateToUserView();
                } else {
                    notifyUser("Unable to leave the room");
                }
            });

            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.add(roomName, disconnectButton);

            return header;
        }

        private void sendMessage(String message) {
            addTextMessageToUI(user.getUsername(), message, LocalDateTime.now(MOSCOW_ZONE));
            backend.sendMessage(KafkaMessage.Action.TEXT_MESSAGE, message);
        }

        private void addTextMessageToUI(String username, String message, LocalDateTime timestamp) {
            updateUI(() -> {
                MessageListItem messageListItem = new MessageListItem(message, timestamp.atZone(MOSCOW_ZONE).toInstant(), username);
                messageListItem.setUserColorIndex(username.hashCode() % 6);
                messages.add(messageListItem);
                messageList.setItems(messages);
            });
        }

    }

    private class Backend {

        private CipherService cipherService;
        private byte[] privateKey;

        public Backend() {
            String inputTopic = String.format("input_room_%s_user_%s", room.getId(), user.getId());

            kafkaReader.subscribe(inputTopic);
            kafkaReader.addListener(this::handleKafkaMessage);
            kafkaReader.startKafkaConsumer();
        }

        private boolean disconnect() {
            return serverService.disconnectRoom(user.getId(), room.getId());
        }

        private void handleKafkaMessage(ConsumerRecord<byte[], byte[]> consumerRecord) {
            KafkaMessage kafkaMessage = KafkaMessage.toKafkaMessage(new String(consumerRecord.value()));

            switch (kafkaMessage.action()) {
                case SETUP_CONNECTION -> {
                    if (kafkaMessage.message() instanceof Long otherUserId) {
                        if (!otherUserId.equals(otherUser.getId())) {
                            throw new IllegalIdentifierException("User id mismatch");
                        }
                        outputTopic = String.format("input_room_%s_user_%s", room.getId(), otherUserId);
                        exchangeKeys();
                    }
                }

                case EXCHANGE_KEYS -> {
                    if (kafkaMessage.message() instanceof byte[] otherKey) {
                        setPrivateKey(otherKey);
                    }
                }

                case TEXT_MESSAGE -> {
                    if (kafkaMessage.message() instanceof byte[] text) {
                        handleTextMessage(text);
                    }
                }

                case FILE_MESSAGE -> {
                    if (kafkaMessage.message() instanceof byte[] file) {
                        // TODO: handleFileMessage
                    }
                }

                //TODO: add metaData action

                default -> throw new IllegalStateException("Invalid action");
            }
        }

        private void exchangeKeys() {
            privateKey = new byte[0];
            byte[] publicKey = new byte[0]; // TODO: Diffie-Hellman
            KafkaMessage keyMessage = new KafkaMessage(KafkaMessage.Action.EXCHANGE_KEYS, publicKey);
            kafkaWriter.write(keyMessage.toBytes(), outputTopic);
        }

        private void setPrivateKey(byte[] otherPublicKey) {
            byte[] sharedPrivateKey = new byte[0];
            // TODO: cipherService
            notifyUser("Connected to user");
        }

        private void handleTextMessage(byte[] textMessage) {
            // TODO: decrypt message
//            CompletableFuture<byte[]> future = textMessage;
            frontend.addTextMessageToUI(otherUser.getUsername(), new String(textMessage), LocalDateTime.now(MOSCOW_ZONE));
        }

        private synchronized void handleFileMessage() {
        }

        private void sendMessage(KafkaMessage.Action action, Object message) {
            if (outputTopic != null) {

                // TODO: if fileMessage
                byte[] messageByte;
                if (message instanceof String) {
                    messageByte = ((String) message).getBytes();
                } else {
                    throw new IllegalStateException("Invalid message type");
                }

                // TODO: encrypt message

                KafkaMessage kafkaMessage = new KafkaMessage(action, messageByte);
                kafkaWriter.write(kafkaMessage.toBytes(), outputTopic);
            } else {
                notifyUser("Unable to send message");
            }
        }
    }
}