package mai.cryptography.cw.ChatCryptography.crypto.Camellia;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;

public class Camellia implements ICipher {
    @Override
    public byte[] encrypt(byte[] text) {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] text) {
        return new byte[0];
    }

    @Override
    public void setRKeys(byte[] key) {

    }

    @Override
    public int getBlockLength() {
        return 0;
    }
}
