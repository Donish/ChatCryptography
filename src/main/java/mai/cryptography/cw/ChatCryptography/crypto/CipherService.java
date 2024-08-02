package mai.cryptography.cw.ChatCryptography.crypto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import mai.cryptography.cw.ChatCryptography.crypto.MARS.MARS;
import mai.cryptography.cw.ChatCryptography.crypto.cipher_mode.*;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IAlgorithm;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherMode;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IPadding;
import mai.cryptography.cw.ChatCryptography.crypto.padding.ANSIX923Padding;
import mai.cryptography.cw.ChatCryptography.crypto.padding.ISO10126Padding;
import mai.cryptography.cw.ChatCryptography.crypto.padding.PKCS7Padding;
import mai.cryptography.cw.ChatCryptography.crypto.padding.ZerosPadding;;
import mai.cryptography.cw.ChatCryptography.crypto.utils.FileUtils;

public class CipherService {

    private final byte[] key;
    private final ICipherMode cipherMode;
    private final IPadding padding;
    private byte[] IV = null;
    private List<String> modeArgs = null;
    private final IAlgorithm algorithm;
    private int byteBlockSize = 16;

    private final int FILE_BLOCK_SIZE = 2097152;

    public enum CipherMode {
        ECB, CBC, PCBC, CFB, OFB, CTR, RandomDelta
    }

    public enum Padding {
        ZEROS, ANSIX923, PKCS7, ISO10126
    }

    public CipherService(byte[] key, CipherMode cipherMode, Padding padding, IAlgorithm algorithm) {
        if (key == null) {
            throw new RuntimeException("passed null key to CipherService");
        }
        this.key = key;

        if (padding == Padding.ZEROS) {
            this.padding = new ZerosPadding();
        } else if (padding == Padding.ANSIX923) {
            this.padding = new ANSIX923Padding();
        } else if (padding == Padding.PKCS7) {
            this.padding = new PKCS7Padding();
        } else if (padding == Padding.ISO10126) {
            this.padding = new ISO10126Padding();
        } else {
            throw new RuntimeException("passed incorrect padding to CipherService");
        }

        if (cipherMode == CipherMode.ECB) {
            this.cipherMode = new ECBMode();
        } else if (cipherMode == CipherMode.CBC) {
            this.cipherMode = new CBCMode();
        } else if (cipherMode == CipherMode.PCBC) {
            this.cipherMode = new PCBCMode();
        } else if (cipherMode == CipherMode.CFB) {
            this.cipherMode = new CFBMode();
        } else if (cipherMode == CipherMode.OFB) {
            this.cipherMode = new OFBMode();
        } else if (cipherMode == CipherMode.CTR) {
            this.cipherMode = new CTRMode();
        } else if (cipherMode == CipherMode.RandomDelta) {
            this.cipherMode = new RDMode();
        } else {
            throw new RuntimeException("passed incorrect cipherMode to CipherService");
        }

        if (algorithm == null) {
            throw new RuntimeException("passed null algorithm to CipherService");
        }
        if (algorithm instanceof MARS) {
            this.byteBlockSize = 16;
        }
        this.algorithm = algorithm;
    }

    public CipherService(byte[] key, CipherMode cipherMode, Padding padding, IAlgorithm algorithm, byte[] IV) {
        this(key, cipherMode, padding, algorithm);
        if (IV == null) {
            throw new RuntimeException("passed null to Cipher");
        }
        this.IV = IV.clone();
    }

    public CipherService(byte[] key,
                         CipherMode cipherMode,
                         Padding padding,
                         IAlgorithm algorithm,
                         byte[] IV,
                         List<String> modeArgs) {
        this(key, cipherMode, padding, algorithm, IV);

        if (modeArgs == null) {
            throw new RuntimeException("passed null to Cipher");
        }
        this.modeArgs = new ArrayList<>(modeArgs);
    }

    private byte[] encryptFileBlock(byte[] text) {
        text = padding.makePadding(text, byteBlockSize);
        return cipherMode.encryptWithMode(text, IV, modeArgs, algorithm, byteBlockSize);
    }

    public void encrypt(String inputFilename, String outputFilename) {
        readFile(inputFilename, outputFilename, true);
    }

    private byte[] decryptFileBlock(byte[] text) {
        return padding.removePadding(cipherMode.decryptWithMode(text, IV, modeArgs, algorithm, byteBlockSize));
    }

    public void decrypt(String inputFilename, String outputFilename) {
        readFile(inputFilename, outputFilename, false);
    }

    private void readFile(String fileName, String outputFileName, boolean isEncrypt) {
        Path path = Paths.get(fileName);
        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            long position = 0;

            while (position < fileSize) {
                CompletableFuture<ByteBuffer> readFuture = readChunk(fileChannel, position, isEncrypt);
                ByteBuffer byteBuffer = readFuture.get();

                if (byteBuffer.position() > 0) {
                    byteBuffer.flip();
                    byte[] text = new byte[byteBuffer.remaining()];
                    byteBuffer.get(text);

                    byte[] cipheredChunk = isEncrypt ? encryptFileBlock(text) : decryptFileBlock(text);
                    FileUtils.writeFileBlock(outputFileName, cipheredChunk);
                    byteBuffer.clear();
                } else {
                    break;
                }
                position += isEncrypt ? FILE_BLOCK_SIZE : FILE_BLOCK_SIZE + byteBlockSize;
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private CompletableFuture<ByteBuffer> readChunk(AsynchronousFileChannel fileChannel, long position, boolean isEncrypt) {
        ByteBuffer byteBuffer = isEncrypt ? ByteBuffer.allocate(FILE_BLOCK_SIZE) : ByteBuffer.allocate(FILE_BLOCK_SIZE + byteBlockSize);

        CompletableFuture<Integer> future = new CompletableFuture<>();
        fileChannel.read(byteBuffer, position, byteBuffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                future.complete(result);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {}
        });

        return future.thenApply(readBytes -> byteBuffer);
    }
}