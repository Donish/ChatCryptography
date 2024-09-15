package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

import java.util.concurrent.ExecutorService;

public abstract class ACipherMode {

    protected final ICipher cipher;
    protected final byte[] IV;
    protected ExecutorService executorService;
    protected final int blockLength;

    protected ACipherMode(ICipher cipher, byte[] IV, int blockLength, ExecutorService executorService) {
        this.cipher = cipher;
        this.IV = IV;
        this.blockLength = blockLength;
        this.executorService = executorService;
    }

    public abstract byte[] encryptWithMode(byte[] data);

    public abstract  byte[] decryptWithMode(byte[] data);

}