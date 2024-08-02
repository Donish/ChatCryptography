package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class CFBMode implements ICipherMode {

    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> notUsed, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        byte[] prevBlock = IV;
        int blocksCount = text.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
            byte[] encryptedBlock = BitUtils.xorArrays(block, algorithm.encryptBlock(prevBlock));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = encryptedBlock;
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> notUsed, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] prevBlock = (i == 0) ? IV : Arrays.copyOfRange(cipheredText, idx - blockSize, idx);
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] decryptedBlock = BitUtils.xorArrays(block, algorithm.encryptBlock(prevBlock));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
