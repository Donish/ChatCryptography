package mai.cryptography.cw.ChatCryptography.crypto;

import lombok.extern.slf4j.Slf4j;
import mai.cryptography.cw.ChatCryptography.crypto.cipher_mode.CipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ACipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.padding.Padding;

import java.util.concurrent.*;

@Slf4j
public class CipherService {
    private final ExecutorService executorService;
    private final int blockLength;
    private final ACipherMode cipherMode;
    private final IPadding padding;

    public CipherService(
            byte[] key,
            ICipher cipher,
            CipherMode.Mode cypherMode,
            Padding.Mode paddingMode,
            byte[] IV) {
        blockLength = cipher.getBlockLength();
        cipher.setRKeys(key);

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        padding = Padding.getInstance(paddingMode);
        cipherMode = CipherMode.getInstance(cypherMode, cipher, IV, executorService);

        log.info("CryptoContext build successfully");
    }

    public CompletableFuture<byte[]> encrypt(byte[] text) {
        return CompletableFuture.supplyAsync(() -> cipherMode.encryptWithMode(padding.makePadding(text, blockLength)));
    }

    public CompletableFuture<byte[]> decrypt(byte[] cipherText) {
        return CompletableFuture.supplyAsync(() -> padding.removePadding(cipherMode.decryptWithMode(cipherText)));
    }

    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}