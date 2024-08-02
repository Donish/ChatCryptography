package mai.cryptography.cw.ChatCryptography.vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@PageTitle("Main")
@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        RouterLink loginLink = new RouterLink("Login", LoginView.class);
        add(loginLink);
    }
}
