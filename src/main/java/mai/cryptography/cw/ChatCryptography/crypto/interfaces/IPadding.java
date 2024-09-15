package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface IPadding {

    byte[] makePadding(byte[] text, int requiredSizeInBytes);

    byte[] removePadding(byte[] text);

}
