package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

@PageTitle("Register")
@Route(value = "register")
public class RegisterView extends VerticalLayout {

    private final RegisterService registerService;

    @Autowired
    public RegisterView(RegisterService registerService) {
        this.registerService = registerService;

        TextField usernameField = new TextField("Enter your username");
        TextField passwordField = new TextField("Enter your password");

        Button registerButton = new Button("Register", event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            User user;

            try {
                user = registerService.registration(username, password);
            } catch (Exception e) {
                Notification.show("User already exists!");
            }

        });
    }

}
