package mai.cryptography.cw.ChatCryptography.crypto.interfaces;

import java.util.List;

public interface ICipherMode {

    public byte[] encryptWithMode(byte[] text, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize);

    public byte[] decryptWithMode(byte[] cipheredText, byte[] IV, List<String> parameters, IAlgorithm algorithm, int blockSize);

}