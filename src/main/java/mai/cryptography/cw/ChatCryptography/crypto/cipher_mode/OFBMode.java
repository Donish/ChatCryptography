package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;

public final class OFBMode extends ACipherMode {

    public OFBMode(ICipher cipher, byte[] IV) {
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
            prevBlock = cipher.encrypt(prevBlock);
            byte[] encryptedBlock = BitUtils.xorArrays(block, prevBlock);
            System.arraycopy(encryptedBlock, 0, result, idx, encryptedBlock.length);
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
            prevBlock = cipher.encrypt(prevBlock);
            byte[] decryptedBlock = BitUtils.xorArrays(block, prevBlock);
            System.arraycopy(decryptedBlock, 0, result, idx, decryptedBlock.length);
        }

        return result;
    }
}
