package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;

import java.util.concurrent.ExecutorService;

public class CipherMode {
    public enum Mode {
        ECB,
        CBC,
        PCBC,
        CFB,
        OFB,
        CTR,
        RD
    }

    public static ACipherMode getInstance(
            Mode mode,
            ICipher cipher,
            byte[] IV,
            ExecutorService executor) {
        return switch (mode) {
            case ECB -> new ECBMode(cipher, IV, executor);
            case CBC -> new CBCMode(cipher, IV, executor);
            case PCBC -> new PCBCMode(cipher, IV);
            case CFB -> new CFBMode(cipher, IV, executor);
            case OFB -> new OFBMode(cipher, IV);
            case CTR -> new CTRMode(cipher, IV, executor);
            case RD -> new RDMode(cipher, IV, executor);
        };
    }
}
