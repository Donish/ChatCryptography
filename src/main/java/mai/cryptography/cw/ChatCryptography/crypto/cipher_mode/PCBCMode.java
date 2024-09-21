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
    public byte[] encryptWithMode(byte[] data) {
        return process(data, true);
    }

    @Override
    public byte[] decryptWithMode(byte[] data) {
        return process(data, false);
    }

    private byte[] process(byte[] data, boolean isEncrypt) {
        byte[] result = new byte[data.length];
        byte[] blockForXor = IV;

        int length = data.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            System.arraycopy(data, startIndex, block, 0, blockLength);

            byte[] processedBlock = isEncrypt
                    ? cipher.encrypt(BitUtils.xor(block, blockForXor))
                    : BitUtils.xor(blockForXor, cipher.decrypt(block));
            System.arraycopy(processedBlock, 0, result, startIndex, processedBlock.length);
            blockForXor = BitUtils.xor(processedBlock, block);
        }

        return result;
    }
}
