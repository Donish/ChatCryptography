package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

import java.util.List;

public interface ICipher {

    byte[] encrypt(byte[] text);

    byte[] decrypt(byte[] text);

    void setRKeys(byte[] key);

}
