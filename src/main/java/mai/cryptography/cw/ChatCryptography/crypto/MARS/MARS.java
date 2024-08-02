package mai.cryptography.cw.ChatCryptography.crypto.MARS;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;

public class MARS implements IAlgorithm {

    private final ICipher feistelNetwork;

    public MARS(byte[] key) {
        this.feistelNetwork = new Type3FeistelNetwork(key, new MARSConversion(), new MARSKeyGenerator());
    }

    @Override
    public byte[] encryptBlock(byte[] block) {
        return feistelNetwork.encrypt(block);
    }

    @Override
    public byte[] decryptBlock(byte[] block) {
        return feistelNetwork.decrypt(block);
    }

    public void setRKeys(byte[] key) {
        feistelNetwork.setRKeys(key);
    }

    public int getBlockLength() {
        return 16;
    }
}
