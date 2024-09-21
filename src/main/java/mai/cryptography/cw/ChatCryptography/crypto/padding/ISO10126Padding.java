package mai.cryptography.cw.ChatCryptography.crypto.padding;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public final class ISO10126Padding implements IPadding {

    @Override
    public byte[] makePadding(byte[] data, int blockSize) {
        int paddingLength = blockSize - (data.length % blockSize);

        byte[] paddingBytes = new byte[paddingLength];
        new SecureRandom().nextBytes(paddingBytes);
        paddingBytes[paddingLength - 1] = (byte) paddingLength;

        byte[] paddedInput = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, paddedInput, 0, data.length);
        System.arraycopy(paddingBytes, 0, paddedInput, data.length, paddingLength);

        return paddedInput;
    }

    @Override
    public byte[] removePadding(byte[] data) {
        return Arrays.copyOf(data, data.length - data[data.length - 1]);
    }

}
