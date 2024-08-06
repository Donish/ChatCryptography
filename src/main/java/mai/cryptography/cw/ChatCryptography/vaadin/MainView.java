package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;

@PageTitle("Main")
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        addClassName("main-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Button loginButton = new Button("Login", event -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        Button registerButton = new Button("Register", event -> {
            getUI().ifPresent(ui -> ui.navigate("register"));
        });

        add(loginButton, registerButton);
    }
}
