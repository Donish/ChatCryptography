package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

import java.util.List;

public interface IRoundKeyGenerator {

    public byte[][] generateRKeys(byte[] key);

}