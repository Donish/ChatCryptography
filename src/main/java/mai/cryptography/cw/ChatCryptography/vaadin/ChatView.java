package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.*;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.component.html.*;

import mai.cryptography.cw.ChatCryptography.crypto.CipherFactory;
import mai.cryptography.cw.ChatCryptography.crypto.CipherService;
import mai.cryptography.cw.ChatCryptography.crypto.DiffieHellmanProtocol;
import mai.cryptography.cw.ChatCryptography.kafka.*;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.RoomCipherParams;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RoomService;
import mai.cryptography.cw.ChatCryptography.service.ServerService;
import mai.cryptography.cw.ChatCryptography.service.UserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@PageTitle("Chat")
@Route(value = "user/:userId/room/:roomId")
public class ChatView extends HorizontalLayout implements BeforeEnterObserver {

    private static final ZoneId MOSCOW_ZONE = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private User user;
    private Room room;

    private RoomCipherParams cipherParams;

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
        Optional<Long> possibleRoomId = event.getRouteParameters().getLong("roomId");

        if (possibleUserId.isPresent() && possibleRoomId.isPresent()) {
            Optional<User> possibleUser = userService.getUserById(possibleUserId.get());
            Optional<Room> possibleRoom = roomService.getRoomById(possibleRoomId.get());

            if (possibleUser.isPresent() && possibleRoom.isPresent()) {
                this.user = possibleUser.get();
                this.room = possibleRoom.get();

                if (this.room.getUsers().contains(this.user)) {
                    this.registration = Broadcaster.registration(this::receiveBroadcasterMessage);
                    this.cipherParams = this.room.getRoomCipherParams();
                    this.backend = new Backend();
                    this.frontend = new Frontend();
                    return;
                }
            }
        }

        isDisconnected = true;
    }

    @Override
    protected void onDetach(DetachEvent event) {
        super.onDetach(event);
        kafkaReader.stop();

        if (outputTopic != null) {
            KafkaMessage kafkaMessage = new KafkaMessage(KafkaMessage.Action.CLEAR, new byte[0]);
            kafkaWriter.write(kafkaMessage.toBytes(), outputTopic);
        }

        if (backend != null) {
            if (!isDisconnected) {
                if (backend.disconnect()) {
                    Broadcaster.broadcast("update");
                }
            }

            if (backend.cipherService != null) {
                backend.cipherService.close();
            }

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
        public enum Destination {
            PRODUCER,
            CONSUMER
        }

        private final VerticalLayout messages;

        public Frontend() {
            addClassNames("chat");
            setSpacing(false);
            setSizeFull();

            VerticalLayout roomContainer = new VerticalLayout();
            HorizontalLayout header = createHeader();
            this.messages = createMessagesContainer();
            HorizontalLayout inputContainer = createInputContainer();

            roomContainer.add(header, messages, inputContainer);
            roomContainer.expand(messages);
            add(roomContainer);
        }

        private HorizontalLayout createHeader() {
            H2 roomName = new H2(room.getRoomName());

            MenuBar menuBar = new MenuBar();
            menuBar.setOpenOnHover(true);
            MenuItem members = menuBar.addItem(VaadinIcon.USERS.create());

            Set<User> users = room.getUsers();
            if (users != null && !users.isEmpty()) {
                SubMenu subMenu = members.getSubMenu();
                for (User user : users) {
                    subMenu.addItem(user.getUsername());
                }
            }

            Button disconnectButton = new Button("Disconnect", event -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Disconnect");
                confirmDialog.setText("Confirm disconnect");
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Disconnect");
                confirmDialog.addConfirmListener(e -> {
                    if (backend.disconnect()) {
                        isDisconnected = true;
                        notifyUser("Disconnected from room");
                        Broadcaster.broadcast("update");
                        navigateToUserView();
                    } else {
                        notifyUser("Unable to disconnect");
                    }
                });
                confirmDialog.open();
            });

            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.add(roomName, menuBar, disconnectButton);
            header.expand(menuBar);

            return header;
        }

        private VerticalLayout createMessagesContainer() {
            VerticalLayout messagesContainer = new VerticalLayout();
            messagesContainer.getStyle().set("overflow-y", "auto");
            messagesContainer.setSizeFull();
            return messagesContainer;
        }

        private HorizontalLayout createInputContainer() {
            MessageInput messageInput = new MessageInput();
            Upload upload = createUpload();

            messageInput.getElement().getStyle().set("overflow-y", "auto");
            messageInput.addSubmitListener(event -> {
                String message = event.getValue();
                showTextMessage(message, Destination.PRODUCER);
                backend.sendMessage(KafkaMessage.Action.TEXT_MESSAGE, message);
            });

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setWidthFull();
            horizontalLayout.addAndExpand(messageInput);

            HorizontalLayout inputContainer = new HorizontalLayout();
            inputContainer.setAlignItems(Alignment.STRETCH);
            inputContainer.setHeight("100px");
            inputContainer.setWidthFull();
            inputContainer.add(horizontalLayout, upload);
            inputContainer.expand(horizontalLayout);

            return inputContainer;
        }

        private Upload createUpload() {
            FileBuffer fileBuffer = new FileBuffer();
            Upload upload = new Upload(fileBuffer);

            upload.setAutoUpload(false);
            upload.setDropAllowed(false);

            upload.addSucceededListener(event -> handleFileMessage(fileBuffer, event.getFileName()));

            return upload;
        }

        private void showTextMessage(String message, Destination destination) {
            updateUI(() -> {
                HorizontalLayout messageContent = new HorizontalLayout();
                messageContent.setSpacing(true);
                messageContent.getStyle().set("display", "flex").set("alight-items", "flex-end").set("max-width", "100%");

                Span messageSpan = new Span(message);
                messageSpan.getStyle()
                        .set("font-size", "20px")
                        .set("white-space", "normal")
                        .set("overflow-wrap", "break-word")
                        .set("flex-grow", "1")
                        .set("min-width", "0");

                Span timeSpan = new Span(getCurrentTime());
                timeSpan.getStyle()
                        .set("font-size", "12px")
                        .set("margin-left", "7px")
                        .set("flex-shrink", "0");

                messageContent.add(messageSpan, timeSpan);

                Div div = new Div(messageContent);
                div.getStyle()
                        .set("border-radius", "12px")
                        .set("padding", "5px")
                        .set("max-width", "100%")
                        .set("box-sizing", "border-box");

                if (destination.equals(Destination.PRODUCER)) {
                    div.getStyle().set("background-color", "lightblue");
                } else {
                    div.getStyle().set("background-color", "grey");
                }

                this.messages.add(div);
            });
        }

        private void showFileMessage(File file, String filename, Destination destination) {
            updateUI(() -> {
                StreamResource fileResource = getResource(file, filename);

                HorizontalLayout messageContent = new HorizontalLayout();
                messageContent.getStyle()
                        .set("display", "flex")
                        .set("align-items", "flex-end")
                        .set("max-width", "100%");
                messageContent.setSpacing(true);

                Anchor anchorFile = new Anchor(fileResource, filename);

                anchorFile.getElement().setAttribute("download", true);
                anchorFile.getStyle()
                        .set("font-size", "20px")
                        .set("white-space", "normal")
                        .set("overflow-wrap", "break-word")
                        .set("flex-grow", "1")
                        .set("min-width", "0");

                Span timeSpan = new Span(getCurrentTime());
                timeSpan.getStyle()
                        .set("font-size", "12px")
                        .set("color", "#888")
                        .set("margin-left", "7px")
                        .set("flex-shrink", "0");

                messageContent.add(anchorFile, timeSpan);

                Div div = new Div(messageContent);

                div.getStyle()
                        .set("border-radius", "12px")
                        .set("padding", "5px")
                        .set("max-width", "100%")
                        .set("box-sizing", "border-box");

                if (destination.equals(Destination.PRODUCER)) {
                    div.getStyle().set("background-color", "lightblue");
                } else {
                    div.getStyle().set("background-color", "grey");
                }

                this.messages.add(div);
            });
        }

        private void showImageMessage(File file, String filename) {
            updateUI(() -> {
                StreamResource imageResource = getResource(file, filename);

                Image image = new Image(imageResource, "image");

                image.setWidth("420px");
                image.setHeight("360px");
                image.getStyle()
                        .set("border", "1px solid #ccc")
                        .set("border-radius", "25px");

                Div div = new Div(image);

                this.messages.add(div);

            });
        }

        private void handleFileMessage(FileBuffer fileBuffer, String filename) {
            try {
                File file = fileBuffer.getFileData().getFile();
                if (isImage(fileBuffer.getFileData().getMimeType())) {
                    showImageMessage(file, filename);
                    sendFileMessage(fileBuffer.getInputStream(), FileMessageMetaData.Type.IMAGE, filename, file.length());
                } else {
                    showFileMessage(file, filename, Destination.PRODUCER);
                    sendFileMessage(fileBuffer.getInputStream(), FileMessageMetaData.Type.FILE, filename, file.length());
                }
            } catch (IOException e) {
                notifyUser("Unable to send file");
            }
        }

        private void sendFileMessage(InputStream stream, FileMessageMetaData.Type type, String filename, long length) throws IOException {
            String messageId = UUID.nameUUIDFromBytes(filename.getBytes()).toString();

            backend.sendMessage(KafkaMessage.Action.BEGIN_FILE_MESSAGE, new FileMessageMetaData(messageId, type, filename, length));

            int nRead;
            long offset = 0;
            byte[] buffer = new byte[10240];

            while ((nRead = stream.read(buffer, 0, buffer.length)) != -1) {
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                data.write(buffer, 0, nRead);
                data.flush();

                backend.sendMessage(KafkaMessage.Action.FILE_MESSAGE, new FileMessage(messageId, data.toByteArray(), offset));
                offset += nRead;
            }
        }

        private void clearChat() {
            updateUI(this.messages::removeAll);
        }

        private boolean isImage(String type) {
            return type.startsWith("image/");
        }

        private StreamResource getResource(File file, String filename) {
            return new StreamResource(filename, () -> {
                try {
                    return new BufferedInputStream(Files.newInputStream(file.toPath()));
                } catch (IOException e) {
                    notifyUser("Error getting resource");
                    return null;
                }
            });
        }

        private String getCurrentTime() {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return currentTime.format(formatter);
        }

    }

    private class Backend {

        private record FileWrapper(
                File file,
                RandomAccessFile randomAccessFile,
                FileMessageMetaData.Type type,
                String filename,
                long length
        ) {
        }

        private CipherService cipherService;
        private byte[] privateKey;
        private long otherUserId;
        private final Map<String, FileWrapper> tempFiles = new HashMap<>();


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
            System.out.println("MY DEBUG: " + kafkaMessage.action()); // here
            switch (kafkaMessage.action()) {
                case SETUP_CONNECTION -> {
                    if (kafkaMessage.message() instanceof Long otherUser) {
                        this.otherUserId = otherUser;
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
                        handleFileMessage(file);
                    }
                }

                case BEGIN_FILE_MESSAGE -> {
                    if (kafkaMessage.message() instanceof byte[] fileMetadata) {
                        handleMetadata(fileMetadata);
                    }
                }

                case CLEAR -> {
                    frontend.clearChat();
                }

                default -> throw new IllegalStateException("Invalid action");
            }
        }

        private void exchangeKeys() {
            privateKey = DiffieHellmanProtocol.generateOwnPrivateKey();
            byte[] publicKey = DiffieHellmanProtocol.generateOwnPublicKey(
                    privateKey,
                    cipherParams.getG(),
                    cipherParams.getP()
            );

            KafkaMessage keyMessage = new KafkaMessage(KafkaMessage.Action.EXCHANGE_KEYS, publicKey);
            kafkaWriter.write(keyMessage.toBytes(), outputTopic);
        }

        private void setPrivateKey(byte[] otherPublicKey) {
            byte[] sharedPrivateKey = DiffieHellmanProtocol.generateSharedPrivateKey(
                    otherPublicKey,
                    privateKey,
                    cipherParams.getP()
            );

            this.cipherService = CipherFactory.createCipherService(cipherParams, sharedPrivateKey);

            notifyUser("Connected to user");
        }

        private void handleTextMessage(byte[] textMessage) {
            if (cipherService != null) {
                CompletableFuture<byte[]> decryptedMessageFuture = cipherService.decrypt(textMessage);
                decryptedMessageFuture.thenAccept(decryptedMessage -> {
                    System.out.println("MY MESSAGE: " + new String(decryptedMessage));
                    frontend.showTextMessage(new String(decryptedMessage), Frontend.Destination.CONSUMER);
                });
            } else {
                notifyUser("Unable to decrypt message");
            }
        }

        private synchronized void handleFileMessage(byte[] data) {
            if (cipherService != null) {
                CompletableFuture<byte[]> decryptedMessageFuture = cipherService.decrypt(data);

                try {
                    byte[] decryptedMessage = decryptedMessageFuture.get();
                    FileMessage message = FileMessage.toMessage(new String(decryptedMessage));

                    FileWrapper fileWrapper = tempFiles.get(message.messageId());
                    RandomAccessFile randomAccessFile = fileWrapper.randomAccessFile();

                    if (randomAccessFile != null) {
                        randomAccessFile.seek(message.offset());
                        randomAccessFile.write(message.data());

                        if (fileWrapper.file().length() == fileWrapper.length()) {
                            randomAccessFile.close();

                            switch (fileWrapper.type()) {
                                case IMAGE -> frontend.showImageMessage(fileWrapper.file(), fileWrapper.filename());
                                case FILE -> frontend.showFileMessage(fileWrapper.file(), fileWrapper.filename(), Frontend.Destination.CONSUMER);
                                default -> throw new IllegalStateException("Invalid message type");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                notifyUser("Unable to process message");
            }
        }

        private synchronized void handleMetadata(byte[] data) {
            if (cipherService != null) {
                CompletableFuture<byte[]> decryptedMetadataFuture = cipherService.decrypt(data);

                decryptedMetadataFuture.thenAccept(decryptedMetadata -> {
                    try {
                        FileMessageMetaData fileMessageMetadata = FileMessageMetaData.toFileMessageMetadata(new String(decryptedMetadata));

                        File tempFile = File.createTempFile(fileMessageMetadata.messageId(), ".tmp");
                        tempFile.deleteOnExit();

                        RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
                        tempFiles.put(
                                fileMessageMetadata.messageId(),
                                new FileWrapper(
                                        tempFile,
                                        randomAccessFile,
                                        fileMessageMetadata.type(),
                                        fileMessageMetadata.filename(),
                                        fileMessageMetadata.length()));
                    } catch (IOException e) {
                        notifyUser("Unable to create temporary file");
                    }
                });
            }
        }


        private void sendMessage(KafkaMessage.Action action, Object message) {
            if (outputTopic != null && cipherService != null) {

                byte[] messageByte = switch (message) {
                    case String textMessage -> textMessage.getBytes();

                    case FileMessage fileMessage -> fileMessage.toBytes();

                    case FileMessageMetaData fileMessageMetaData -> fileMessageMetaData.toBytes();

                    default -> throw new IllegalStateException("Invalid message type");
                };

                CompletableFuture<byte[]> encryptedMessageFuture = cipherService.encrypt(messageByte);

                encryptedMessageFuture.thenAccept(encryptedMessage -> {
                    KafkaMessage kafkaMessage = new KafkaMessage(action, encryptedMessage);
                    kafkaWriter.write(kafkaMessage.toBytes(), outputTopic);
                });

            } else {
                notifyUser("Unable to send message");
            }
        }
    }
}