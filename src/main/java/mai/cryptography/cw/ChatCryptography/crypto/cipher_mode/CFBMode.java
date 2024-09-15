package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public final class CFBMode extends ACipherMode {

    public CFBMode(ICipher cipher, byte[] IV, ExecutorService executor) {
        super(cipher, IV, cipher.getBlockLength(), executor);
    }

    @Override
    public byte[] encryptWithMode(byte[] text) {
        byte[] result = new byte[text.length];
        byte[] prevBlock = IV;
        int blocksCount = text.length / blockLength;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockLength;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockLength);
            byte[] encryptedBlock = BitUtils.xorArrays(block, cipher.encrypt(prevBlock));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = encryptedBlock;
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText) {
        byte[] result = new byte[cipheredText.length];
        IntStream.range(0, cipheredText.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] prevBlock = (i == 0) ? IV : Arrays.copyOfRange(cipheredText, idx - blockLength, idx);
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockLength);
                    byte[] decryptedBlock = BitUtils.xorArrays(block, cipher.encrypt(prevBlock));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });
        return result;
    }
}
