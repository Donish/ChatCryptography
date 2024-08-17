package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import mai.cryptography.cw.ChatCryptography.model.Room;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RoomService;
import mai.cryptography.cw.ChatCryptography.service.ServerService;
import mai.cryptography.cw.ChatCryptography.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // Почему не надо Autowired?
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
            updateUI(() -> frontend.notify()); // TODO
        }
    }

    private void navigateToChat(Long roomId) {
        UI.getCurrent().navigate(ChatView.class, new RouteParameters(Map.of(
                "userId", String.valueOf(user.getId()),
                "roomId", String.valueOf(roomId)
        )));
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

        private final Grid<String> userGrid;
        private final Grid<String> roomGrid;
        private final List<String> userList;
        private final List<String> roomNameList;

        public Frontend() {
            setSizeFull();
            setSpacing(true);

            VerticalLayout mainLayout = new VerticalLayout();
            HorizontalLayout header = createHeader();

            this.userGrid = new Grid<>();
            this.userList = userService.getAllUsernames();
            if (userList != null) {
                this.userGrid.setItems(this.userList);
            }
            this.userGrid.addColumn(String::toString).setHeader("Users");
            this.userGrid.asSingleSelect().addValueChangeListener(event -> showChatProperties(event.getValue()));

            roomGrid = new Grid<>();
            roomNameList = roomService.getAllRoomNames();
            if (roomNameList != null) {
                roomGrid.setItems(roomNameList);
            }
            roomGrid.addColumn(String::toString).setHeader("Chats");
            roomGrid.asSingleSelect().addValueChangeListener(event -> navigateToExistingChat(event.getValue()));

            HorizontalLayout contentLayout = new HorizontalLayout();
            contentLayout.setSizeFull();
            contentLayout.setSpacing(true);
            contentLayout.add(userGrid, roomGrid);

            mainLayout.add(header, contentLayout);
            mainLayout.setSizeFull();
            mainLayout.setSpacing(true);
            mainLayout.setAlignItems(Alignment.STRETCH);

            add(mainLayout);
        }

        private HorizontalLayout createHeader() {
            H2 userHeader = new H2(user.getUsername());
            userHeader.getStyle().set("color", "white");
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setHeight("50px");
            header.getStyle().set("background-color", "#696969");
            header.setAlignItems(Alignment.CENTER);
            header.setJustifyContentMode(JustifyContentMode.CENTER);
            header.add(userHeader);

            return header;
        }

        private void showChatProperties(String otherUsername) {
            Optional<User> possibleOtherUser = userService.getByUsername(otherUsername);

            if (possibleOtherUser.isPresent()) {
                User otherUser = possibleOtherUser.get();
                Dialog dialog = new Dialog();
                dialog.setWidth("400px");
                VerticalLayout propertiesLayout = getVerticalLayout(otherUser, dialog);
                dialog.add(propertiesLayout);
                dialog.open();
            }
        }

        private VerticalLayout getVerticalLayout(User otherUser, Dialog dialog) {
            TextField roomNameField = new TextField("Room name");
            Button createButton = new Button("Create", event -> {
                String roomName = roomNameField.getValue();
                createRoom(otherUser, roomName, "some", "some", "some");
                dialog.close();
            });
            Button cancelButton = new Button("Cancel", event -> dialog.close());

            return new VerticalLayout(roomNameField, createButton, cancelButton);
        }

        private void createRoom(
                User otherUser,
                String roomName,
                String algorithm,
                String mode,
                String padding) {
            try {
                Room room = backend.createRoom(user.getId(), otherUser.getId(), roomName, algorithm, mode, padding);
                user.getRooms().add(room.getId());
                otherUser.getRooms().add(room.getId());
//                userService.getRooms(user.getId()).add(room.getId());
//                userService.getRooms(user.getId()).add(room.getId());
                navigateToChat(room.getId());
            } catch (IllegalArgumentException e) {
                notifyUser("Name is occupied");
            }
        }

        private void navigateToExistingChat(String roomName) {
            Optional<Room> possibleRoom = roomService.getRoomByRoomName(roomName);

            if (possibleRoom.isPresent()) {
                Room room = possibleRoom.get();
                navigateToChat(room.getId());
            }
        }

    }

    private class Backend {
        private boolean connectRoom(long roomId) {
            return serverService.connectRoom(user.getId(), roomId);
        }

        private Room createRoom(
                long userId,
                long otherUserId,
                String roomName,
                String algorithm,
                String mode,
                String padding) {
            return serverService.createRoom(userId, otherUserId, roomName, algorithm, mode, padding);
        }
    }

}
