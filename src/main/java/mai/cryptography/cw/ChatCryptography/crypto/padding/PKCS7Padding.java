package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.Arrays;

public final class PKCS7Padding implements IPadding {

    @Override
    public byte[] makePadding(byte[] data, int blockSize) {
        int paddingLength = blockSize - (data.length % blockSize);

        byte[] paddedInput = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, paddedInput, 0, data.length);
        Arrays.fill(paddedInput, data.length, paddedInput.length, (byte) paddingLength);
        return paddedInput;
    }

    @Override
    public byte[] removePadding(byte[] data) {
        return Arrays.copyOf(data, data.length - data[data.length - 1]);
    }

}
