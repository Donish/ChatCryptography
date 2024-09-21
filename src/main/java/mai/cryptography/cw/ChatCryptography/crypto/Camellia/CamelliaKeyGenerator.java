package mai.cryptography.cw.ChatCryptography.crypto.Camellia;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IRoundKeyGenerator;

public class CamelliaKeyGenerator implements IRoundKeyGenerator {
    @Override
    public byte[][] generateRKeys(byte[] key) {
        return new byte[0][];
    }
}
