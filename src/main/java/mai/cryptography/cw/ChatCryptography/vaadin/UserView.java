package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RoomService;
import mai.cryptography.cw.ChatCryptography.service.ServerService;
import mai.cryptography.cw.ChatCryptography.service.UserService;

import java.util.*;

@PageTitle("User")
@Route(value = "user/:userId")
public class UserView extends HorizontalLayout implements BeforeEnterObserver {
    private User user;

    private final ServerService serverService;
    private final UserService userService;
    private final RoomService roomService;

    private Frontend frontend;
    private Backend backend;

    private Registration registration;

    public UserView(ServerService serverService, UserService userService, RoomService roomService) {
        this.serverService = serverService;
        this.userService = userService;
        this.roomService = roomService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> userId = event.getRouteParameters().getLong("userId");

        if (userId.isPresent()) {
            Optional<User> possibleUser = userService.getUserById(userId.get());

            if (possibleUser.isPresent()) {
                this.user = possibleUser.get();
                this.registration = Broadcaster.registration(this::receiveBroadcasterMessage);
                this.backend = new Backend();
                this.frontend = new Frontend();
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (registration != null) {
            registration.remove();
        }
    }

    private void receiveBroadcasterMessage(String message) {
        if (message.equals("update")) {
            updateUI(() -> frontend.updateRoomList());
        }
    }

    private void navigateToChat(Long roomId) {
        UI.getCurrent().navigate(ChatView.class, new RouteParameters(Map.of(
                "userId", String.valueOf(user.getId()),
                "roomId", String.valueOf(roomId)
        )));
    }

    private void navigateToLoginView() {
        UI.getCurrent().navigate(LoginView.class);
    }

    private void updateUI(Command command) {
        Optional<UI> possibleUI = getUI();
        possibleUI.ifPresent(ui -> ui.access(command));
    }

    private void notifyUser(String message) {
        updateUI(() -> Notification.show(message));
    }

    private class Frontend {

        private final Scroller roomList = new Scroller();

//        private final Grid<String> userGrid;
//        private final Grid<String> roomGrid;
//        private final List<String> userList;
//        private final List<String> roomNameList;

        public Frontend() {
            setSizeFull();
            setSpacing(true);

//            VerticalLayout mainLayout = new VerticalLayout();
//            HorizontalLayout header = createHeader();
            VerticalLayout leftLayout = createLeftLayout();
            VerticalLayout rightLayout = createRightLayout();

            add(leftLayout, rightLayout);

//            this.userGrid = new Grid<>();
//            this.userList = userService.getAllUsernames();
//            if (userList != null) {
//                this.userList.remove(user.getUsername());
//                this.userGrid.setItems(this.userList);
//            }
//            this.userGrid.addColumn(String::toString).setHeader("Users");
//            this.userGrid.asSingleSelect().addValueChangeListener(event -> showChatProperties(event.getValue()));
//
//            roomGrid = new Grid<>();
//            List<Room> rooms = user.getRooms();
//            if (rooms != null) {
//                roomNameList = rooms.stream()
//                        .map(Room::getRoomName)
//                        .collect(Collectors.toCollection(ArrayList::new));
//                roomGrid.setItems(roomNameList);
//            } else {
//                roomNameList = new ArrayList<>();
//            }
//            roomGrid.addColumn(String::toString).setHeader("Chats");
//            roomGrid.asSingleSelect().addValueChangeListener(event -> navigateToExistingChat(event.getValue()));
//
//            HorizontalLayout contentLayout = new HorizontalLayout();
//            contentLayout.setSizeFull();
//            contentLayout.setSpacing(true);
//            contentLayout.add(userGrid, roomGrid);
//
//            mainLayout.add(header, contentLayout);
//            mainLayout.setSizeFull();
//            mainLayout.setSpacing(true);
//            mainLayout.setAlignItems(Alignment.STRETCH);
//
//            add(mainLayout);
        }

        private VerticalLayout createLeftLayout() {
            VerticalLayout leftLayout = new VerticalLayout();
            leftLayout.setWidth("50%");
            leftLayout.setSpacing(false);
            VerticalLayout thisUser = createUserLayout();
            VerticalLayout roomLayout = createRoomLayout();

            leftLayout.add(thisUser, new Hr(), roomLayout);

            return leftLayout;
        }

        private VerticalLayout createUserLayout() {
            VerticalLayout userLayout = new VerticalLayout();
            userLayout.setWidthFull();
            userLayout.setAlignItems(Alignment.CENTER);

            Avatar avatar = new Avatar(user.getUsername());

            Span span = new Span(user.getUsername());
            MenuBar menuBar = new MenuBar();
            MenuItem menuItem = menuBar.addItem(span);
            menuItem.getSubMenu().addItem("Log out", event -> navigateToLoginView());

            userLayout.add(avatar, menuBar);
            return userLayout;
        }

        private VerticalLayout createRoomLayout() {
            VerticalLayout roomLayout = new VerticalLayout();
            roomLayout.setWidthFull();
            roomLayout.setAlignItems(Alignment.CENTER);

            H2 title = new H2("Create room");
            TextField roomName = new TextField("Room name");
            Select<String> algorithm = new Select<>();
            algorithm.setLabel("Algorithm");
            algorithm.setItems("MARS", "Camellia");

            Select<String> cipherMode = new Select<>();
            cipherMode.setLabel("Cipher Mode");
            cipherMode.setItems("ECB", "CBC", "CFB", "PCBC", "OFB", "CTR", "RD");

            Select<String> padding = new Select<>();
            padding.setLabel("Padding");
            padding.setItems("Zeros", "ANSI_X_923", "PKCS7", "ISO_10126");

            Button createButton = new Button("Create", event -> {
                if (backend.createRoom(
                        user.getId(),
                        roomName.getValue(),
                        algorithm.getValue(),
                        cipherMode.getValue(),
                        padding.getValue()
                )) {
                    notifyUser("Room created");
                    Broadcaster.broadcast("update");
                } else {
                    notifyUser("Unable to create room");
                }
            });

            roomLayout.add(title, roomName, algorithm, cipherMode, padding, createButton);
            return roomLayout;
        }

        private VerticalLayout createRightLayout() {
            VerticalLayout rightLayout = new VerticalLayout();
            rightLayout.setWidth("50%");
            rightLayout.setSpacing(false);
            Div roomsTitle = new Div();
            roomsTitle.setWidthFull();
            roomsTitle.add(new H1("Rooms"));

            this.roomList.setWidthFull();
            this.roomList.setContent(createRoomList());

            rightLayout.add(roomsTitle, new Hr(), this.roomList);
            return rightLayout;
        }

        private VerticalLayout createRoomList() {
            VerticalLayout roomsListLayout = new VerticalLayout();
            List<Room> rooms = roomService.getAllRooms();

            for (Room room : rooms) {
                if (room.getUsers().size() < 2) {
                    roomsListLayout.add(createRoomComponent(room));
                }
            }

            return roomsListLayout;
        }

        private HorizontalLayout createRoomComponent(Room room) {
            HorizontalLayout roomComponent = new HorizontalLayout();
            roomComponent.setWidthFull();
            roomComponent.setMargin(true);
            roomComponent.setSpacing(true);
            roomComponent.setPadding(true);

            StringJoiner roomMembers = new StringJoiner(" ");
            roomMembers.add("Members:");
            for (User user : room.getUsers()) {
                roomMembers.add(user.getUsername());
            }

            Span details = new Span("Room name: " + room.getRoomName() + " | " + roomMembers);
            Button joinButton = new Button("Join room", event -> {
                if (backend.connectRoom(room.getId())) {
                    notifyUser("Connected to room");
                    Broadcaster.broadcast("update");
                    navigateToChat(room.getId());
                } else {
                    notifyUser("Unable to connect to room");
                }
            });

            Button deleteButton = new Button("Delete", event -> {
                if (backend.deleteRoom(room.getId())) {
                    notifyUser("Room deleted");
                    Broadcaster.broadcast(String.format("delete_room_%s", room.getId()));
                    Broadcaster.broadcast("update");
                } else {
                    notifyUser("Unable to delete room");
                }
            });

            if (user.getId().equals(room.getCreatorUser())) {
                roomComponent.add(details, joinButton, deleteButton);
            } else {
                roomComponent.add(details, joinButton);
            }

            roomComponent.expand(details);
            roomComponent.setAlignItems(Alignment.CENTER);
            return roomComponent;
        }

        private void updateRoomList() {
            roomList.setContent(createRoomList());
        }

//        private HorizontalLayout createHeader() {
//            H2 userHeader = new H2(user.getUsername());
//            userHeader.getStyle().set("color", "white");
//            HorizontalLayout header = new HorizontalLayout();
//            header.setWidthFull();
//            header.setHeight("50px");
//            header.getStyle().set("background-color", "#696969");
//            header.setAlignItems(Alignment.CENTER);
//            header.setJustifyContentMode(JustifyContentMode.CENTER);
//            header.add(userHeader);
//
//            return header;
//        }

//        private void showChatProperties(String otherUsername) {
//            Optional<User> possibleOtherUser = userService.getByUsername(otherUsername);
//
//            if (possibleOtherUser.isPresent()) {
//                User otherUser = possibleOtherUser.get();
//                Dialog dialog = new Dialog();
//                dialog.setWidth("400px");
//                VerticalLayout propertiesLayout = getVerticalLayout(otherUser, dialog);
//                dialog.add(propertiesLayout);
//                dialog.open();
//            }
//        }

//        private VerticalLayout getVerticalLayout(User otherUser, Dialog dialog) {
//            TextField roomNameField = new TextField("Room name");
//            Button createButton = new Button("Create", event -> {
//                String roomName = roomNameField.getValue();
//
//                try {
//                    long roomId = createRoom(otherUser, roomName, "some", "some", "some");
//                    backend.connectRoom(otherUser.getId(), roomName);
//                    backend.exchangeInformation(user.getId(), otherUser.getId(), roomId);
//                    navigateToChat(roomId);
//                } catch (IllegalArgumentException e) {
//                    notifyUser("Name is occupied");
//                } catch (IllegalIdentifierException e) {
//                    notifyUser("Room with such users already exists");
//                } catch (IllegalStateException e) {
//                    notifyUser("Unable to connect to room");
//                }
//
//                dialog.close();
//            });
//            Button cancelButton = new Button("Cancel", event -> dialog.close());
//
//            return new VerticalLayout(roomNameField, createButton, cancelButton);
//        }

//        private long createRoom(
//                User otherUser,
//                String roomName,
//                String algorithm,
//                String mode,
//                String padding) {
//            Room room = backend.createRoom(user.getId(), otherUser.getId(), roomName, algorithm, mode, padding);
//            userService.addRoomToUser(user, room);
//            userService.addRoomToUser(otherUser, room);
//            return room.getId();
//        }
//
//        private void navigateToExistingChat(String roomName) {
//            Optional<Room> possibleRoom = roomService.getRoomByRoomName(roomName);
//
//            if (possibleRoom.isPresent()) {
//                Room room = possibleRoom.get();
//                navigateToChat(room.getId());
//            }
//        }

    }

    private class Backend {

        private boolean connectRoom(long roomId) {
            return serverService.connectRoom(user.getId(), roomId);
        }

        private boolean deleteRoom(long roomId) {
            return serverService.deleteRoom(roomId);
        }

        private boolean createRoom(
                long userId,
                String roomName,
                String algorithm,
                String mode,
                String padding) {
            return serverService.createRoom(userId, roomName, algorithm, mode, padding);
        }

    }

}
