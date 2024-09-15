package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public final class CTRMode extends ACipherMode {

    public CTRMode(ICipher cipher, byte[] IV, ExecutorService executor) {
        super(cipher, IV, cipher.getBlockLength(), executor);
    }
    
    @Override
    public byte[] encryptWithMode(byte[] text) {
        byte[] result = new byte[text.length];
        int length = blockLength / 2;

        IntStream.range(0, text.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(text, idx, idx + blockLength);
                    byte[] toEncrypt = new byte[blockLength];
                    System.arraycopy(IV, 0, toEncrypt, 0, length);
                    System.arraycopy(BitUtils.intToByteArr(i), 0, toEncrypt, toEncrypt.length - Integer.BYTES, length);
                    byte[] encryptedBlock = BitUtils.xorArrays(block, cipher.encrypt(toEncrypt));
                    System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
                });

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText) {
        byte[] result = new byte[cipheredText.length];
        int length = blockLength / 2;

        IntStream.range(0, cipheredText.length / blockLength)
                .parallel()
                .forEach(i -> {
                    int idx = i * blockLength;
                    byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockLength);
                    byte[] toDecrypt = new byte[blockLength];
                    System.arraycopy(IV, 0, toDecrypt, 0, length);
                    System.arraycopy(BitUtils.intToByteArr(i), 0, toDecrypt, toDecrypt.length - Integer.BYTES, length);
                    byte[] decryptedBlock = BitUtils.xorArrays(block, cipher.encrypt(toDecrypt));
                    System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
                });

        return result;
    }
}
