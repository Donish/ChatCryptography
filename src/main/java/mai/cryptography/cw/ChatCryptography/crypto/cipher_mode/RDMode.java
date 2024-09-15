package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public final class RDMode extends ACipherMode {

    public RDMode(ICipher cipher, byte[] IV, ExecutorService executorService) {
        super(cipher, IV, cipher.getBlockLength(), executorService);
    }
    
    @Override
    public byte[] encryptWithMode(byte[] text) {
        byte[] result = new byte[text.length];
        BigInteger delta = new BigInteger(Arrays.copyOfRange(IV, IV.length / 2, IV.length));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, text.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockLength);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] encryptedBlock = cipher.encrypt(BitUtils.xorArrays(block, initialDelta.toByteArray()));
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText) {
        byte[] result = new byte[cipheredText.length];
        BigInteger delta = new BigInteger(Arrays.copyOfRange(IV, IV.length / 2, IV.length));
        BigInteger initial = new BigInteger(IV);

        IntStream.range(0, cipheredText.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockLength);
                    BigInteger initialDelta = initial.add(delta.multiply(BigInteger.valueOf(i)));
                    byte[] decryptedBlock = BitUtils.xorArrays(cipher.decrypt(block), initialDelta.toByteArray());
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
