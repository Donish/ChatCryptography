package mai.cryptography.cw.ChatCryptography.crypto.MARS;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherConversion;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;

public class MARSConversion implements ICipherConversion {
    @Override
    public byte[] convert(byte[] block, byte[] roundKey) {
        int firstKey = BitUtils.byteArrToInt(roundKey, 0);
        int secondKey = BitUtils.byteArrToInt(roundKey, 4);

        int in = BitUtils.byteArrToInt(block, 0);

        int L, M, R;

        R = BitUtils.lCircularShift((BitUtils.lCircularShift(in, 13) * secondKey), 10);
        M = BitUtils.lCircularShift(in + firstKey, BitUtils.rCircularShift(R, 5) & 0x1f);
        L = BitUtils.lCircularShift(Type3FeistelNetwork.S[M & 0x1ff] ^ BitUtils.rCircularShift(R, 5) ^ R, R & 0x1f);

        return BitUtils.intArrToByteArr(new int[]{L, M, R});
    }
}
