package mai.cryptography.cw.ChatCryptography.crypto.cipher_mode;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public final class RDMode extends ACipherMode {

    private final BigInteger delta;

    public RDMode(ICipher cipher, byte[] IV, ExecutorService executorService) {
        super(cipher, IV, cipher.getBlockLength(), executorService);
        delta = new BigInteger(Arrays.copyOf(IV, blockLength / 2));
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
        BigInteger initialStart = new BigInteger(IV);

        int numBlocks = data.length / blockLength;
        List<Future<?>> futures = new ArrayList<>(numBlocks);

        for (int i = 0; i < numBlocks; ++i) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                BigInteger initial = initialStart.add(delta.multiply(BigInteger.valueOf(index)));
                int startIndex = index * blockLength;
                byte[] block = new byte[blockLength];
                System.arraycopy(data, startIndex, block, 0, blockLength);
                byte[] processedBlock = isEncrypt
                        ? cipher.encrypt(BitUtils.xor(initial.toByteArray(), block))
                        : BitUtils.xor(cipher.decrypt(block), initial.toByteArray());
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
