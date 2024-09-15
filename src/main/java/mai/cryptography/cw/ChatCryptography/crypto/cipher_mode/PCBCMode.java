package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;

public final class PCBCMode extends ACipherMode {

    public PCBCMode(ICipher cipher, byte[] IV) {
        super(cipher, IV, cipher.getBlockLength(), null);
    }

    @Override
    public byte[] encryptWithMode(byte[] text) {
        byte[] result = new byte[text.length];
        byte[] prevBlock = IV;
        int blocksCount = text.length / blockLength;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockLength;
            byte[] block = Arrays.copyOfRange(text, idx, idx + blockLength);
            byte[] encryptedBlock = cipher.encrypt(BitUtils.xorArrays(prevBlock, block));
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
            prevBlock = BitUtils.xorArrays(block, encryptedBlock);
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] cipheredText) {
        byte[] result = new byte[cipheredText.length];
        byte[] prevBlock = IV;
        int blocksCount = cipheredText.length / blockLength;

        for (int i = 0; i < blocksCount; i++) {
            int idx = i * blockLength;
            byte[] block = Arrays.copyOfRange(cipheredText, idx, idx + blockLength);
            byte[] decryptedBlock = BitUtils.xorArrays(cipher.decrypt(block), prevBlock);
            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
            prevBlock = BitUtils.xorArrays(block, decryptedBlock);
        }

        return result;
    }
}
