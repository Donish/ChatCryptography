package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public final class ECBMode extends ACipherMode {

    public ECBMode(ICipher cipher, byte[] IV, ExecutorService executorService) {
        super(cipher, IV, cipher.getBlockLength(), executorService);
    }

    @Override
    public byte[] encryptWithMode(byte[] text) {
        byte[] result = new byte[text.length];
        IntStream.range(0, text.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockLength);
                    byte[] encryptedBlock = cipher.encrypt(block);
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });
        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText) {
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockLength);
                    byte[] decryptedBlock = cipher.decrypt(block);
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
