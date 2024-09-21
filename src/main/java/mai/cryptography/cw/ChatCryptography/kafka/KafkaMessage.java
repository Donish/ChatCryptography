package mai.cryptography.cw.ChatCryptography.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public record KafkaMessage(Action action, Object message) {

    public enum Action {
        SETUP_CONNECTION,
        EXCHANGE_KEYS,
        TEXT_MESSAGE,
        FILE_MESSAGE,
        CLEAR,
        BEGIN_FILE_MESSAGE
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(KafkaMessage.class, new KafkaMessageAdapter())
            .create();

    public byte[] toBytes() {
        return gson.toJson(this).getBytes();
    }

    public static KafkaMessage toKafkaMessage(String json) {
        return gson.fromJson(json, KafkaMessage.class);
    }
}
