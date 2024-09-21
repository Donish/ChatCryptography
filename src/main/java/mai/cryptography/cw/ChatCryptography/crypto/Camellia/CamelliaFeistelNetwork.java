package mai.cryptography.cw.ChatCryptography.crypto.Camellia;

import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipher;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.ICipherConversion;
import mai.cryptography.cw.ChatCryptography.crypto.interfaces.IRoundKeyGenerator;
import mai.cryptography.cw.ChatCryptography.crypto.utils.BitUtils;
import oshi.util.tuples.Pair;

import java.util.Arrays;

public class CamelliaFeistelNetwork implements ICipher {

    private byte[] sbox1 = {
            0x70, (byte) (0x82 & 0xff), 0x2c, (byte) (0xec & 0xff), (byte) (0xb3 & 0xff), 0x27, (byte) (0xc0 & 0xff), (byte) (0xe5 & 0xff), (byte) (0xe4 & 0xff), (byte) (0x85 & 0xff), 0x57, 0x35, (byte) (0xea & 0xff), 0x0c, (byte) (0xae & 0xff), 0x41,
            0x23, (byte) (0xef & 0xff), 0x6b, (byte) (0x93 & 0xff), 0x45, 0x19, (byte) (0xa5 & 0xff), 0x21, (byte) (0xed & 0xff), 0x0e, 0x4f, 0x4e, 0x1d, 0x65, (byte) (0x92 & 0xff), (byte) (0xbd & 0xff),
            (byte) (0x86 & 0xff), (byte) (0xb8 & 0xff), (byte) (0xaf & 0xff), (byte) (0x8f & 0xff), 0x7c, (byte) (0xeb & 0xff), 0x1f, (byte) (0xce & 0xff), 0x3e, 0x30, (byte) (0xdc & 0xff), 0x5f, 0x5e, (byte) (0xc5 & 0xff), 0x0b, 0x1a,
            (byte) (0xa6 & 0xff), (byte) (0xe1 & 0xff), 0x39, (byte) (0xca & 0xff), (byte) (0xd5 & 0xff), 0x47, 0x5d, 0x3d, (byte) (0xd9 & 0xff), 0x01, 0x5a, (byte) (0xd6 & 0xff), 0x51, 0x56, 0x6c, 0x4d,
            (byte) (0x8b & 0xff), 0x0d, (byte) (0x9a & 0xff), 0x66, (byte) (0xfb & 0xff), (byte) (0xcc & 0xff), (byte) (0xb0 & 0xff), 0x2d, 0x74, 0x12, 0x2b, 0x20, (byte) (0xf0 & 0xff), (byte) (0xb1 & 0xff), (byte) (0x84 & 0xff), (byte) (0x99 & 0xff),
            (byte) (0xdf & 0xff), 0x4c, (byte) (0xcb & 0xff), (byte) (0xc2 & 0xff), 0x34, 0x7e, 0x76, 0x05, 0x6d, (byte) (0xb7 & 0xff), (byte) (0xa9 & 0xff), 0x31, (byte) (0xd1 & 0xff), 0x17, 0x04, (byte) (0xd7 & 0xff),
            0x14, 0x58, 0x3a, 0x61, (byte) (0xde & 0xff), 0x1b, 0x11, 0x1c, 0x32, 0x0f, (byte) (0x9c & 0xff), 0x16, 0x53, 0x18, (byte) (0xf2 & 0xff), 0x22,
            (byte) 0xfe, 0x44, (byte) 0xcf, (byte) 0xb2, (byte) 0xc3, (byte) 0xb5, 0x7a, (byte) 0x91, 0x24, 0x08, (byte) 0xe8, (byte) 0xa8, 0x60, (byte) 0xfc, 0x69, 0x50,
            (byte) 0xaa, (byte) 0xd0, (byte) 0xa0, 0x7d, (byte) 0xa1, (byte) 0x89, 0x62, (byte) 0x97, 0x54, 0x5b, 0x1e, (byte) 0x95, (byte) 0xe0, (byte) 0xff, 0x64, (byte) 0xd2,
            0x10, (byte) 0xc4, 0x00, 0x48, (byte) 0xa3, (byte) 0xf7, 0x75, (byte) 0xdb, (byte) 0x8a, 0x03, (byte) 0xe6, (byte) 0xda, 0x09, 0x3f, (byte) 0xdd, (byte) 0x94,
            (byte) 0x87, 0x5c, (byte) 0x83, 0x02, (byte) 0xcd, 0x4a, (byte) 0x90, 0x33, 0x73, 0x67, (byte) 0xf6, (byte) 0xf3, (byte) 0x9d, 0x7f, (byte) 0xbf, (byte) 0xe2,
            0x52, (byte) 0x9b, (byte) 0xd8, 0x26, (byte) 0xc8, 0x37, (byte) 0xc6, 0x3b, (byte) 0x81, (byte) 0x96, 0x6f, 0x4b, 0x13, (byte) 0xbe, 0x63, 0x2e,
            (byte) 0xe9, 0x79, (byte) 0xa7, (byte) 0x8c, (byte) 0x9f, 0x6e, (byte) 0xbc, (byte) 0x8e, 0x29, (byte) 0xf5, (byte) 0xf9, (byte) 0xb6, 0x2f, (byte) 0xfd, (byte) 0xb4, 0x59,
            0x78, (byte) 0x98, 0x06, 0x6a, (byte) 0xe7, 0x46, 0x71, (byte) 0xba, (byte) 0xd4, 0x25, (byte) 0xab, 0x42, (byte) 0x88, (byte) 0xa2, (byte) 0x8d, (byte) 0xfa,
            0x72, 0x07, (byte) 0xb9, 0x55, (byte) 0xf8, (byte) 0xee, (byte) 0xac, 0x0a, 0x36, 0x49, 0x2a, 0x68, 0x3c, 0x38, (byte) 0xf1, (byte) 0xa4,
            0x40, 0x28, (byte) 0xd3, 0x7b, (byte) 0xbb, (byte) 0xc9, 0x43, (byte) 0xc1, 0x15, (byte) 0xe3, (byte) 0xad, (byte) 0xf4, 0x77, (byte) 0xc7, (byte) 0x80, (byte) 0x9e
    };

    private byte[] sbox2 = new byte[256];
    private byte[] sbox3 = new byte[256];
    private byte[] sbox4 = new byte[256];

    private final IRoundKeyGenerator roundKeyGenerator;
    private final ICipherConversion cipherConversion;
    private long[] kw = new long[5];
    private long[] k = new long[25];
    private long[] ke = new long[7];
    private int kLength;
    private long[] C = {
            0xA09E667F3BCC908BL,
            0xB67AE8584CAA73B2L,
            0xC6EF372FE94F82BEL,
            0x54FF53A5F1D36F1CL,
            0x10E527FADE682D1DL,
            0xB05688C2B3E6C1FDL
    };

    public CamelliaFeistelNetwork(ICipherConversion cipherConversion, IRoundKeyGenerator roundKeyGenerator) {
        this.cipherConversion = cipherConversion;
        this.roundKeyGenerator = roundKeyGenerator;
        for (int i = 0; i < sbox1.length; i++) {
            sbox2[i] = BitUtils.rotateLeft8(sbox1[1], 1);
            sbox3[i] = BitUtils.rotateLeft8(sbox1[i], 7);
            sbox4[i] = sbox1[BitUtils.rotateLeft8((byte) i, 1) & 0xff];
        }
    }

    @Override
    public byte[] encrypt(byte[] text) {
        byte[] res = new byte[text.length];

        long D1 = BitUtils.getLongFromBytes(text, 0);
        long D2 = BitUtils.getLongFromBytes(text, 8);

        D1 ^= kw[1];
        D2 ^= kw[2];

        D2 ^= f(D1, k[1]);
        D1 ^= f(D2, k[2]);
        D2 ^= f(D1, k[3]);
        D1 ^= f(D2, k[4]);
        D2 ^= f(D1, k[5]);
        D1 ^= f(D2, k[6]);

        D1 = fl(D1, ke[1]);
        D2 = flinv(D2, ke[2]);

        D2 ^= f(D1, k[7]);
        D1 ^= f(D2, k[8]);
        D2 ^= f(D1, k[9]);
        D1 ^= f(D2, k[10]);
        D2 ^= f(D1, k[11]);
        D1 ^= f(D2, k[12]);

        D1 = fl(D1, ke[3]);
        D2 = flinv(D2, ke[4]);

        D2 ^= f(D1, k[13]);
        D1 ^= f(D2, k[14]);
        D2 ^= f(D1, k[15]);
        D1 ^= f(D2, k[16]);
        D2 ^= f(D1, k[17]);
        D1 ^= f(D2, k[18]);

        if (kLength > 16) {
            D1 = fl(D1, ke[5]);
            D2 = flinv(D2, ke[6]);

            D2 ^= f(D1, k[19]);
            D1 ^= f(D2, k[20]);
            D2 ^= f(D1, k[21]);
            D1 ^= f(D2, k[22]);
            D2 ^= f(D1, k[23]);
            D1 ^= f(D2, k[24]);
        }

        D2 ^= kw[3];
        D1 ^= kw[4];

        BitUtils.putLongToBytes(res, 0, D2);
        BitUtils.putLongToBytes(res, 8, D1);

        return res;
    }

    @Override
    public byte[] decrypt(byte[] text) {
        byte[] res = new byte[text.length];

        long D2 = BitUtils.getLongFromBytes(text, 0);
        long D1 = BitUtils.getLongFromBytes(text, 8);

        D1 ^= kw[4];
        D2 ^= kw[3];

        if (kLength > 16) {
            D1 ^= f(D2, k[24]);
            D2 ^= f(D1, k[23]);
            D1 ^= f(D2, k[22]);
            D2 ^= f(D1, k[21]);
            D1 ^= f(D2, k[20]);
            D2 ^= f(D1, k[19]);

            D2 = fl(D2, ke[6]);
            D1 = flinv(D1, ke[5]);
        }

        D1 ^= f(D2, k[18]);
        D2 ^= f(D1, k[17]);
        D1 ^= f(D2, k[16]);
        D2 ^= f(D1, k[15]);
        D1 ^= f(D2, k[14]);
        D2 ^= f(D1, k[13]);

        D2 = fl(D2, ke[4]);
        D1 = flinv(D1, ke[3]);

        D1 ^= f(D2, k[12]);
        D2 ^= f(D1, k[11]);
        D1 ^= f(D2, k[10]);
        D2 ^= f(D1, k[9]);
        D1 ^= f(D2, k[8]);
        D2 ^= f(D1, k[7]);

        D2 = fl(D2, ke[2]);
        D1 = flinv(D1, ke[1]);

        D1 ^= f(D2, k[6]);
        D2 ^= f(D1, k[5]);
        D1 ^= f(D2, k[4]);
        D2 ^= f(D1, k[3]);
        D1 ^= f(D2, k[2]);
        D2 ^= f(D1, k[1]);

        D2 ^= kw[2];
        D1 ^= kw[1];

        BitUtils.putLongToBytes(res, 0, D1);
        BitUtils.putLongToBytes(res, 8, D2);

        return res;
    }

    @Override
    public void setRKeys(byte[] key) {
        kLength = key.length;
        long D1, D2;
        long[] KA = new long[2];
        long[] KB = new long[2];
        long[] KL = new long[2];
        long[] KR = new long[2];

        KL[0] = BitUtils.getLongFromBytes(key, 0);
        KL[1] = BitUtils.getLongFromBytes(key, 8);

        if (kLength == 24) {
            KR[0] = BitUtils.getLongFromBytes(key, 16);
            KR[1] = ~KR[0];
        } else if (kLength == 32) {
            KR[0] = BitUtils.getLongFromBytes(key, 16);
            KR[1] = BitUtils.getLongFromBytes(key, 24);
        }

        D1 = KL[0] ^ KR[0];
        D2 = KL[1] ^ KR[1];

        D2 = D2 ^ f(D1, C[0]);
        D1 = D1 ^ f(D2, C[1]);

        D1 = D1 ^ KL[0];
        D2 = D2 ^ KL[1];

        D2 = D2 ^ f(D1, C[2]);
        D1 = D1 ^ f(D2, C[3]);
        KA[0] = D1;
        KA[1] = D2;
        D1 = (KA[0] ^ KR[0]);
        D2 = (KA[1] ^ KR[1]);
        D2 = D2 ^ f(D1, C[4]);
        D1 = D1 ^ f(D2, C[5]);
        KB[0] = D1;
        KB[1] = D2;

        if (kLength == 16) {
            kw[1] = rotl128(KL, 0).getA();
            kw[2] = rotl128(KL, 0).getB();

            k[1] = rotl128(KA, 0).getA();
            k[2] = rotl128(KA, 0).getB();

            k[3] = rotl128(KL, 15).getA();
            k[4] = rotl128(KL, 15).getB();

            k[5] = rotl128(KA, 15).getA();
            k[6] = rotl128(KA, 15).getB();


            ke[1] = rotl128(KA, 30).getA();
            ke[2] = rotl128(KA, 30).getB();

            k[7] = rotl128(KL, 45).getA();
            k[8] = rotl128(KL, 45).getB();

            k[9] = rotl128(KA, 45).getA();

            k[10] = rotl128(KL, 60).getB();

            k[11] = rotl128(KA, 60).getA();
            k[12] = rotl128(KA, 60).getB();

            ke[3] = rotl128(KL, 77).getA();
            ke[4] = rotl128(KL, 77).getB();

            k[13] = rotl128(KL, 94).getA();
            k[14] = rotl128(KL, 94).getB();

            k[15] = rotl128(KA, 94).getA();
            k[16] = rotl128(KA, 94).getB();

            k[17] = rotl128(KL, 111).getA();
            k[18] = rotl128(KL, 111).getB();

            kw[3] = rotl128(KA, 111).getA();
            kw[4] = rotl128(KA, 111).getB();
        } else {
            kw[1] = rotl128(KL, 0).getA();
            kw[2] = rotl128(KL, 0).getB();

            k[1] = rotl128(KB, 0).getA();
            k[2] = rotl128(KB, 0).getB();

            k[3] = rotl128(KR, 15).getA();
            k[4] = rotl128(KR, 15).getB();

            k[5] = rotl128(KA, 15).getA();
            k[6] = rotl128(KA, 15).getB();

            ke[1] = rotl128(KR, 30).getA();
            ke[2] = rotl128(KR, 30).getB();

            k[7] = rotl128(KB, 30).getA();
            k[8] = rotl128(KB, 30).getB();

            k[9] = rotl128(KL, 45).getA();
            k[10] = rotl128(KL, 45).getB();

            k[11] = rotl128(KA, 45).getA();
            k[12] = rotl128(KA, 45).getB();

            ke[3] = rotl128(KL, 60).getA();
            ke[4] = rotl128(KL, 60).getB();

            k[13] = rotl128(KR, 60).getA();
            k[14] = rotl128(KR, 60).getB();

            k[15] = rotl128(KB, 60).getA();
            k[16] = rotl128(KB, 60).getB();

            k[17] = rotl128(KL, 77).getA();
            k[18] = rotl128(KL, 77).getB();

            ke[5] = rotl128(KA, 77).getA();
            ke[6] = rotl128(KA, 77).getB();

            k[19] = rotl128(KR, 94).getA();
            k[20] = rotl128(KR, 94).getB();

            k[21] = rotl128(KA, 94).getA();
            k[22] = rotl128(KA, 94).getB();

            k[23] = rotl128(KL, 111).getA();
            k[24] = rotl128(KL, 111).getB();

            kw[3] = rotl128(KB, 111).getA();
            kw[4] = rotl128(KB, 111).getB();
        }
    }

    private Pair<Long, Long> rotl128(long[] k, int rot) {
        if (rot > 64) {
            rot -= 64;
            long tmp = k[0];
            k[0] = k[1];
            k[1] = tmp;
        }

        long t = k[0] >>> (64 - rot);
        long hi = (k[0] << rot) | (k[1] >>> (64 - rot));
        long lo = (k[1] << rot) | t;

        return new Pair<>(hi, lo);
    }

    private long f(long fin, long KE) {
        long x;
        x = fin ^ KE;
        byte t1 = sbox1[(int) ((x >> 56) & 0xFF)];
        byte t2 = sbox2[(int) ((x >> 48) & 0xFF)];
        byte t3 = sbox3[(int) ((x >> 40) & 0xFF)];
        byte t4 = sbox4[(int) ((x >> 32) & 0xFF)];
        byte t5 = sbox2[(int) ((x >> 24) & 0xFF)];
        byte t6 = sbox3[(int) ((x >> 16) & 0xFF)];
        byte t7 = sbox4[(int) ((x >> 8) & 0xFF)];
        byte t8 = sbox1[(int) (x & 0xFF)];

        byte y1 = (byte) (t1 ^ t3 ^ t4 ^ t6 ^ t7 ^ t8);
        byte y2 = (byte) (t1 ^ t2 ^ t4 ^ t5 ^ t7 ^ t8);
        byte y3 = (byte) (t1 ^ t2 ^ t3 ^ t5 ^ t6 ^ t8);
        byte y4 = (byte) (t2 ^ t3 ^ t4 ^ t5 ^ t6 ^ t7);
        byte y5 = (byte) (t1 ^ t2 ^ t6 ^ t7 ^ t8);
        byte y6 = (byte) (t2 ^ t3 ^ t5 ^ t7 ^ t8);
        byte y7 = (byte) (t3 ^ t4 ^ t5 ^ t6 ^ t8);
        byte y8 = (byte) (t1 ^ t4 ^ t5 ^ t6 ^ t7);

        return ((long) (y1 & 0xFF) << 56) |
                ((long) (y2 & 0xFF) << 48) |
                ((long) (y3 & 0xFF) << 40) |
                ((long) (y4 & 0xFF) << 32) |
                ((long) (y5 & 0xFF) << 24) |
                ((long) (y6 & 0xFF) << 16) |
                ((long) (y7 & 0xFF) << 8) |
                ((long) (y8 & 0xFF));
    }

    private long fl(long flin, long KE) {
        int x1 = (int) (flin >> 32);
        int x2 = (int) (flin & 0xFFFFFFFFL);
        int k1 = (int) (KE >> 32);
        int k2 = (int) (KE & 0xFFFFFFFFL);

        x2 = x2 ^ Integer.rotateLeft(x1 & k1, 1);

        x1 = x1 ^ (x2 | k2);

        return ((long) x1 << 32) | (x2 & 0xFFFFFFFFL);
    }

    private long flinv(long in, long KE) {
        int y1 = (int) (in >> 32);
        int y2 = (int) (in & 0xFFFFFFFFL);
        int k1 = (int) (KE >> 32);
        int k2 = (int) (KE & 0xFFFFFFFFL);

        y1 = y1 ^ (y2 | k2);

        y2 = y2 ^ Integer.rotateLeft(y1 & k1, 1);

        return ((long) y1 << 32) | (y2 & 0xFFFFFFFFL);
    }

    @Override
    public int getBlockLength() {
        return 16;
    }
}
