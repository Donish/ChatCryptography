package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.RouteParameters;
import mai.cryptography.cw.ChatCryptography.model.User;
import mai.cryptography.cw.ChatCryptography.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.textfield.TextField;

@PageTitle("Register")
@Route(value = "register")
public class RegisterView extends VerticalLayout {

    private final RegisterService registerService;

    @Autowired
    public RegisterView(RegisterService registerService) {
        this.registerService = registerService;

        addClassName("register-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Button backButton = new Button(VaadinIcon.BACKSPACE.create(), event -> {
            UI.getCurrent().navigate(MainView.class);
        });

        TextField usernameField = new TextField("Enter your username");
        TextField passwordField = new TextField("Enter your password");

        Button registerButton = new Button("Register", event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (username.isEmpty()) {
                Notification.show("Enter your username");
            } else if (password.isEmpty()) {
                Notification.show("Enter your password");
            } else {
                User user;
                try {
                    user = this.registerService.registration(username, password);
                    getUI().ifPresent(ui -> ui.navigate(UserView.class, new RouteParameters("userId", String.valueOf(user.getId()))));
                } catch (RegisterService.RegisterException e) {
                    Notification.show("User already exists!");
                } catch (IllegalArgumentException e) {
                    Notification.show("Username must contain only numbers, letters and must not contain more than 20 characters");
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        });

        add(usernameField, passwordField, registerButton, backButton);
    }

}
