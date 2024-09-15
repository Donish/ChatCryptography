package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

public interface IRoundKeyGenerator {

    byte[][] generateRKeys(byte[] key);

}