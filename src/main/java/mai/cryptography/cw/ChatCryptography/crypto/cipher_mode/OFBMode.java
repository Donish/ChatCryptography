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
    public byte[] encryptWithMode(byte[] data) {
        return process(data);
    }

    @Override
    public byte[] decryptWithMode(byte[] data) {
        return process(data);
    }

    private byte[] process(byte[] data) {
        byte[] result = new byte[data.length];
        byte[] previousBlock = IV;

        int length = data.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            System.arraycopy(data, startIndex, block, 0, blockLength);

            byte[] encryptedPart = cipher.encrypt(previousBlock);
            byte[] processedBlock = BitUtils.xor(block, encryptedPart);

            System.arraycopy(processedBlock, 0, result, startIndex, processedBlock.length);
            previousBlock = encryptedPart;
        }

        return result;
    }
}
