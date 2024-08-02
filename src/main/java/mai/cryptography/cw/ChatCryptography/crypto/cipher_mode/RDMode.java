package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class RDMode implements ICipherMode {

    @Override
    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[text.length];
        BigInteger delta = new BigInteger(Arrays.copyOfRange(IV, IV.length / 2, IV.length));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, text.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockSize);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] encryptedBlock = algorithm.encryptBlock(BitUtils.xorArrays(block, initialDelta.toByteArray()));
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize) {
        byte[] result = new byte[cipheredText.length];
        BigInteger delta = new BigInteger(Arrays.copyOfRange(IV, IV.length / 2, IV.length));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, cipheredText.length / blockSize)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockSize;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockSize);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] decryptedBlock = BitUtils.xorArrays(algorithm.decryptBlock(block), initialDelta.toByteArray());
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
