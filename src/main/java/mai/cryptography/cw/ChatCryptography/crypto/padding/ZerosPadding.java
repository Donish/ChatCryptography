package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;

import java.util.Arrays;

public final class ZerosPadding implements IPadding {

    @Override
    public byte[] makePadding(byte[] data, int blockSize) {
        int paddingLength = blockSize - (data.length % blockSize);

        byte[] paddedInput = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, paddedInput, 0, data.length);

        return paddedInput;
    }

    @Override
    public byte[] removePadding(byte[] data) {
        int lastNonZeroIndex = data.length - 1;

        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != 0) {
                lastNonZeroIndex = i;
                break;
            }
        }

        return Arrays.copyOf(data, lastNonZeroIndex + 1);
    }

}
