package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface IAlgorithm {
    public byte[] encryptBlock(byte[] block);

    public byte[] decryptBlock(byte[] block);

}
