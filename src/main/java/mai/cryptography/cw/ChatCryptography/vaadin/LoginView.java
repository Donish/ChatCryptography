package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout {

    final RegisterService registerService;

    @Autowired
    public LoginView(RegisterService registerService) {
        this.registerService = registerService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Button backButton = new Button(VaadinIcon.BACKSPACE.create(), event -> {
            UI.getCurrent().navigate(MainView.class);
        });

        TextField usernameField = new TextField("Enter your username");
        TextField passwordField = new TextField("Enter your password");

        Button loginButton = new Button("Login", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (username.isEmpty()) {
                Notification.show("Enter your username");
            } else if (password.isEmpty()) {
                Notification.show("Enter your password");
            } else {
                User user;
                try {
                    user = this.registerService.login(username, password);
                    // TODO: navigate to chat
                } catch (Exception e) {
                    Notification.show("Wrong username or password");
                }
            }
        });

        add(usernameField, passwordField, loginButton, backButton);
    }
}
