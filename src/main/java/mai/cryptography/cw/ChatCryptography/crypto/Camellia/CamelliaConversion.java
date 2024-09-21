package mai.cryptography.cw.ChatCryptography.crypto.Camellia;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherConversion;

public class CamelliaConversion implements ICipherConversion {
    @Override
    public byte[] convert(byte[] block, byte[] roundKey) {
        return new byte[0];
    }
}
