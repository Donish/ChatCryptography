package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class ECBMode implements ICipherMode {
    @Override
    public byte[] encryptWithMode(byte[] text, byte[] notUsed, List<String> notUsed2, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        IntStream.range(0, text.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
                    byte[] encryptedBlock = algorithm.encryptBlock(block);
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });
        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] notUsed, List<String> notUsed2, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] decryptedBlock = algorithm.decryptBlock(block);
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
