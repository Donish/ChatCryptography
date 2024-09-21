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

public final class CFBMode extends ACipherMode {

    public CFBMode(ICipher cipher, byte[] IV, ExecutorService executor) {
        super(cipher, IV, cipher.getBlockLength(), executor);
    }

    @Override
    public byte[] encryptWithMode(byte[] data) {
        byte[] result = new byte[data.length];
        byte[] previousBlock = IV;

        int length = data.length / blockLength;

        for (int i = 0; i < length; ++i) {
            int startIndex = i * blockLength;
            byte[] block = new byte[blockLength];
            System.arraycopy(data, startIndex, block, 0, blockLength);

            byte[] processedBlock = BitUtils.xor(block, cipher.encrypt(previousBlock));

            System.arraycopy(processedBlock, 0, result, startIndex, processedBlock.length);
            previousBlock = processedBlock;
        }

        return result;
    }

    @Override
    public byte[] decryptWithMode(byte[] data) {
        byte[] result = new byte[data.length];

        int numBlocks = data.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(numBlocks);

        for (int i = 0; i < numBlocks; ++i) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                byte[] previousBlock = (index == 0) ? IV : new byte[blockLength];
                if (index != 0) {
                    System.arraycopy(data, (index - 1) * blockLength, previousBlock, 0, blockLength);
                }

                int startIndex = index * blockLength;
                byte[] currentBlock = new byte[blockLength];
                System.arraycopy(data, startIndex, currentBlock, 0, blockLength);

                byte[] processedBlock = BitUtils.xor(currentBlock, cipher.encrypt(previousBlock));
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
