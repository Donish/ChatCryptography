package mai.cryptography.cw.ChatCryptography.crypto.MARS;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;

public class MARS implements ICipher {

    private final ICipher feistelNetwork;

    public MARS() {
        this.feistelNetwork = new Type3FeistelNetwork(new MARSConversion(), new MARSKeyGenerator());
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
