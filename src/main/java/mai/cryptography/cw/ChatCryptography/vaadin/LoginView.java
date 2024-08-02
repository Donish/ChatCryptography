package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import mai.cryptography.cw.ChatCryptography.message.UserData;

@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout {

    public LoginView() {
        TextField usernameField = new TextField("Enter your username");
        Button loginButton = new Button("Login", event -> {
            String username = usernameField.getValue();
            if (!username.isEmpty()) {
                UserData.getInstance().setUsername(username);
                getUI().ifPresent(ui -> ui.navigate("dialog"));
            }
        });

        add(usernameField, loginButton);
    }
}
