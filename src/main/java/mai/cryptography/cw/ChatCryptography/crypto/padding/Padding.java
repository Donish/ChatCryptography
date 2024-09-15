package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;

public class Padding {
    public enum Mode {
        Zeros,
        ANSI_X_923,
        PKCS7,
        ISO_10126
    }

    public static IPadding getInstance(Mode mode) {
        return switch (mode) {
            case Zeros -> new ZerosPadding();
            case ANSI_X_923 -> new ANSIX923Padding();
            case PKCS7 -> new PKCS7Padding();
            case ISO_10126 -> new ISO10126Padding();
        };
    }
}
