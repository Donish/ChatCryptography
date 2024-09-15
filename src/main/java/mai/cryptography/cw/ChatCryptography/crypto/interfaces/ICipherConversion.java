package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface ICipherConversion {

    byte[] convert(byte[] block, byte[] roundKey);

}
