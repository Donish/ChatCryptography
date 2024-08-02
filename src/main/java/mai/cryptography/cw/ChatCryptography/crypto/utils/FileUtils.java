package mai.cryptography.cw.ChatCryptography.crypto.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

public class FileUtils {

    public static String getFilePath(String filename) {
        return Objects.requireNonNull(FileUtils.class.getClassLoader().getResource(".")).getPath() + filename;
    }

    public static byte[] readFileBlock(String filePath, int fileBlockSize, long offset) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(offset);
            byte[] result;

            if (file.length() - offset < fileBlockSize) {
                result = new byte[(int) (file.length() - file.getFilePointer())];
            } else {
                result = new byte[fileBlockSize];
            }
            file.read(result);

            return result;

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void writeFileBlock(String filePath, byte[] toWrite) {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(file.length());
            file.write(toWrite);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
