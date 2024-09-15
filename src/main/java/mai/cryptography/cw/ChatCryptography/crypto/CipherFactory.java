package mai.cryptography.cw.ChatCryptography.crypto;

import mai.cryptography.cw.ChatCryptography.crypto.Camellia.Camellia;
import mai.cryptography.cw.ChatCryptography.crypto.MARS.MARS;
import mai.cryptography.cw.ChatCryptography.crypto.cipher_mode.CipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.padding.Padding;
import mai.cryptography.cw.ChatCryptography.model.RoomCipherParams;

public class CipherFactory {

    public static CipherService createCipherService(RoomCipherParams cipherParams, byte[] key) {
        return new CipherService(
                key,
                getAlgorithm(cipherParams.getAlgorithm()),
                getCipherMode(cipherParams.getCipherMode()),
                getPadding(cipherParams.getPadding()),
                cipherParams.getIV()
        );
    }

    private static ICipher getAlgorithm(String algorithm) {
        return switch (algorithm) {
            case "MARS" -> new MARS();
            case "Camellia" -> new Camellia();
            default -> throw new IllegalStateException("Invalid algorithm");
        };
    }

    private static CipherMode.Mode getCipherMode(String cipherMode) {
        return switch (cipherMode) {
            case "ECB" -> CipherMode.Mode.ECB;
            case "CBC" -> CipherMode.Mode.CBC;
            case "CFB" -> CipherMode.Mode.CFB;
            case "PCBC" -> CipherMode.Mode.PCBC;
            case "OFB" -> CipherMode.Mode.OFB;
            case "CTR" -> CipherMode.Mode.CTR;
            case "RD" -> CipherMode.Mode.RD;
            default -> throw new IllegalStateException("Invalid cipher mode");
        };
    }

    private static Padding.Mode getPadding(String padding) {
        return switch (padding) {
            case "Zeros" -> Padding.Mode.Zeros;
            case "PKCS7" -> Padding.Mode.PKCS7;
            case "ANSI_X_923" -> Padding.Mode.ANSI_X_923;
            case "ISO_10126" -> Padding.Mode.ISO_10126;
            default -> throw new IllegalStateException("Invalid padding");
        };
    }
}
