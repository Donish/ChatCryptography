package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.List;

public final class OFBMode implements ICipherMode {

    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        byte[] prevBlock = IV;
        int blocksCount = text.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
            prevBlock = algorithm.encryptBlock(prevBlock);
            byte[] encryptedBlock = BitUtils.xorArrays(block, prevBlock);
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        byte[] prevBlock = IV;
        int blocksCount = cipheredText.length / blockSize;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockSize;
            byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
            prevBlock = algorithm.encryptBlock(prevBlock);
            byte[] decryptedBlock = BitUtils.xorArrays(block, prevBlock);
            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
        }

        return result;
    }
}
