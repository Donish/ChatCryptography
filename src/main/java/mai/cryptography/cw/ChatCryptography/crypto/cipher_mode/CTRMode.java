package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public final class CTRMode extends ACipherMode {

    public CTRMode(ICipher cipher, byte[] IV, ExecutorService executor) {
        super(cipher, IV, cipher.getBlockLength(), executor);
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

        int numBlocks = data.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(numBlocks);

        for (int i = 0; i < numBlocks; ++i) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                int startIndex = index * blockLength;
                byte[] block = new byte[blockLength];
                System.arraycopy(data, startIndex, block, 0, blockLength);

                byte[] blockForProcess = new byte[blockLength];
                int length = blockLength - Integer.BYTES;
                System.arraycopy(IV, 0, blockForProcess, 0, length);

                byte[] counterInBytes = new byte[Integer.BYTES];
                for (int j = 0; j < counterInBytes.length; ++j) {
                    counterInBytes[j] = (byte) (index >> (3 - j) * 8);
                }
                System.arraycopy(counterInBytes, 0, blockForProcess, length, counterInBytes.length);

                byte[] processedBlock = BitUtils.xor(block, cipher.encrypt(blockForProcess));
                System.arraycopy(processedBlock, 0, result, startIndex, processedBlock.length);
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
