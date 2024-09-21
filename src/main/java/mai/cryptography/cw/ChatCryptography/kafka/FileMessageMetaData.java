package mai.cryptography.cw.ChatCryptography.kafka;

import com.google.gson.Gson;

public record FileMessageMetaData(String messageId, Type type, String filename, long length) {
    public enum Type {
        IMAGE,
        FILE
    }

    private static final Gson gson = new Gson();

    public byte[] toBytes() {
        return gson.toJson(this).getBytes();
    }

    public static FileMessageMetaData toFileMessageMetadata(String json) {
        return gson.fromJson(json, FileMessageMetaData.class);
    }
}
