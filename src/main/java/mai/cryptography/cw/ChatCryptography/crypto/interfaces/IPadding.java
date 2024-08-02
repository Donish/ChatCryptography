package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface IPadding {

    public byte[] makePadding(byte[] text, int requiredSizeInBytes);

    public byte[] removePadding(byte[] text);

}
