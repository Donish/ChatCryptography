package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import java.util.UUID;

@PageTitle("Dialog")
@Route(value = "dialog")
public class DialogView extends VerticalLayout {

    public DialogView() {
        TextField dialogIdField = new TextField("Enter dialog ID to join");
        Button createButton = new Button("Create new dialog", event -> {
            String dialogId = UUID.randomUUID().toString();
            Notification.show("Dialog created with ID: " + dialogId);
            getUI().ifPresent(ui -> ui.navigate("chat-view/" + dialogId));
        });

        Button joinButton = new Button("Join dialog", event -> {
            String dialogId = dialogIdField.getValue();
            if (!dialogId.isEmpty()) {
                getUI().ifPresent(ui -> ui.navigate("chat-view/" + dialogId));
            }
        });

        add(dialogIdField, joinButton, createButton);
    }
}
