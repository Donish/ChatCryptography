package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface ICipher {

    byte[] encrypt(byte[] text);

    byte[] decrypt(byte[] text);

    void setRKeys(byte[] key);

    int getBlockLength();
}
