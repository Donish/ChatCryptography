package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface ICipherConversion {

    public byte[] convert(byte[] block, byte[] roundKey);

}
