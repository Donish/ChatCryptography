package mai.cryptography.cw.ChatCryptography.kafka;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

public record KafkaMessage(Action action, Object message) {

    public enum Action {
        SETUP_CONNECTION,
        EXCHANGE_KEYS,
        TEXT_MESSAGE,
        FILE_MESSAGE,
        DISCONNECT
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
