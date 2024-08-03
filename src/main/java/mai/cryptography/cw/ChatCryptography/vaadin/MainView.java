package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.button.Button;

import java.awt.*;

@PageTitle("Main")
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {

        Button loginButton = new Button("Login", event -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        Button registerButton = new Button("Register", event -> {
            getUI().ifPresent(ui -> ui.navigate("register"));
        });

//        RouterLink loginLink = new RouterLink("Login", LoginView.class);
//        add(loginLink);
    }
}
