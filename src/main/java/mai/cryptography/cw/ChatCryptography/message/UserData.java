package mai.cryptography.cw.ChatCryptography.message;

public class UserData {
    private static UserData instance;

    private String username;

    private UserData() {
        // Приватный конструктор для предотвращения создания экземпляров извне
    }

    public static synchronized UserData getInstance() {
        if (instance == null) {
            instance = new UserData();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
