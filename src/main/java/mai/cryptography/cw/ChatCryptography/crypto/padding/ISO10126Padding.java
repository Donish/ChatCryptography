package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;
import java.util.Random;

public final class ISO10126Padding implements IPadding {

    @Override
    public byte[] makePadding(byte[] text, int requiredSizeInBytes) {
        Random random = new Random();
        byte[] result;
        boolean isMultiple = text.length % requiredSizeInBytes == 0;
        if (isMultiple) {
            result = new byte[text.length + requiredSizeInBytes];
        } else {
            result = new byte[text.length + (requiredSizeInBytes - text.length % requiredSizeInBytes)];
        }
        int paddedBytes = requiredSizeInBytes - (text.length % requiredSizeInBytes);
        System.arraycopy(text, 0, result, 0, text.length);
        byte[] randomBytes = new byte[paddedBytes];
        random.nextBytes(randomBytes);
        System.arraycopy(randomBytes, 0, result, text.length, randomBytes.length);
        result[result.length - 1] = (byte) paddedBytes;

        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        int count = BitUtils.getUnsignedByte(text[text.length - 1]);
        return Arrays.copyOf(text, text.length - count);
    }

}
