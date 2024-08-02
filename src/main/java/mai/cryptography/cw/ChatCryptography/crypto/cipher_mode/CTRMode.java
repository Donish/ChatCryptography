package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class CTRMode implements ICipherMode {

    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        int length = blockSize / 2;

        IntStream.range(0, text.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
                    byte[] toEncrypt = new byte[blockSize];
                    System.arraycopy(IV, 0, toEncrypt, 0, length);
                    System.arraycopy(BitUtils.intToByteArr(i), 0, toEncrypt, toEncrypt.length - Integer.BYTES, length);
                    byte[] encryptedBlock = BitUtils.xorArrays(block, algorithm.encryptBlock(toEncrypt));
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        int length = blockSize / 2;

        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    byte[] toDecrypt = new byte[blockSize];
                    System.arraycopy(IV, 0, toDecrypt, 0, length);
                    System.arraycopy(BitUtils.intToByteArr(i), 0, toDecrypt, toDecrypt.length - Integer.BYTES, length);
                    byte[] decryptedBlock = BitUtils.xorArrays(block, algorithm.encryptBlock(toDecrypt));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
