package mai.cryptography.cw.ChatCryptography.crypto.Camellia;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;

public class Camellia implements ICipher {

    private final ICipher feistelNetwork;

    public Camellia() {
        this.feistelNetwork = new CamelliaFeistelNetwork(new CamelliaConversion(), new CamelliaKeyGenerator());
    }

    @Override
    public byte[] encrypt(byte[] text) {
        return feistelNetwork.encrypt(text);
    }

    @Override
    public byte[] decrypt(byte[] text) {
        return feistelNetwork.decrypt(text);
    }

    @Override
    public void setRKeys(byte[] key) {
        feistelNetwork.setRKeys(key);
    }

    @Override
    public int getBlockLength() {
        return feistelNetwork.getBlockLength();
    }
}
