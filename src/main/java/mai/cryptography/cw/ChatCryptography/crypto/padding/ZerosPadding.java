package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;

import java.util.Arrays;

public final class ZerosPadding implements IPadding {

    @Override
    public byte[] makePadding(byte[] text, int requiredSizeInBytes) {
        byte[] result;
        boolean isMultiple = text.length % requiredSizeInBytes == 0;
        if (isMultiple) {
            result = new byte[text.length + requiredSizeInBytes];
        } else {
            result = new byte[text.length + (requiredSizeInBytes - text.length % requiredSizeInBytes)];
        }
        System.arraycopy(text, 0, result, 0, text.length);

        return result;
    }

    @Override
    public byte[] removePadding(byte[] text) {
        int idx = 0;
        for (int i = text.length - 1; i >= 0; i--) {
            if (text[i] != 0) {
                idx = i;
                break;
            }
        }
        return Arrays.copyOf(text, idx + 1);
    }

}
