package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public final class ECBMode extends ACipherMode {

    public ECBMode(ICipher cipher, byte[] IV, ExecutorService executorService) {
        super(cipher, IV, cipher.getBlockLength(), executorService);
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

        int numBlocks = data.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(numBlocks);

        for (int i = 0; i < numBlocks; ++i) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                int startIndex = index * blockLength;
                byte[] block = new byte[blockLength];
                System.arraycopy(data, startIndex, block, 0, blockLength);
                block = isEncrypt ? cipher.encrypt(block) : cipher.decrypt(block);
                System.arraycopy(block, 0, result, startIndex, block.length);
            }));
        }

        for (var future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
