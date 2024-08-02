package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;

public final class PKCS7Padding implements IPadding {

    @Override
    public byte[] makePadding(byte[] text, int requiredSizeInBytes) {
        byte[] result;
        boolean isMultiple = text.length % requiredSizeInBytes == 0;
        if (isMultiple) {
            result = new byte[text.length + requiredSizeInBytes];
        } else {
            result = new byte[text.length + (requiredSizeInBytes - text.length % requiredSizeInBytes)];
        }
        int paddedBytes = requiredSizeInBytes - (text.length % requiredSizeInBytes);
        System.arraycopy(text, 0, result, 0, text.length);

        for (int i = text.length; i < result.length; i++) {
            result[i] = (byte) paddedBytes;
        }

        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        int count = BitUtils.getUnsignedByte(text[text.length - 1]);
        return Arrays.copyOf(text, text.length - count);
    }

}
