package mai.cryptography.cw.ChatCryptography.crypto;

import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
public class DiffieHellmanProtocol {

    private static final Random randGenerator = new SecureRandom();

    public static BigInteger[] generateParameters(int bitSize) {
        BigInteger p = BigInteger.probablePrime(bitSize, randGenerator);
        BigInteger g = getPrimitiveRoot(p);
        return new BigInteger[]{g, p};
    }

    private static BigInteger getPrimitiveRoot(BigInteger p) {
        BigInteger pMinusOne = p.subtract(BigInteger.ONE);
        List<BigInteger> factors = primeFactors(pMinusOne);

        for (BigInteger g = BigInteger.TWO; g.compareTo(p) < 0; g = g.add(BigInteger.ONE)) {
            boolean isPrimitive = true;

            for (BigInteger factor : factors) {
                BigInteger exponent = pMinusOne.divide(factor);
                if (g.modPow(exponent, p).equals(BigInteger.ONE)) {
                    isPrimitive = false;
                    break;
                }
            }
            if (isPrimitive) {
                return g;
            }
        }

        return null;
    }

    private static List<BigInteger> primeFactors(BigInteger num) {
        List<BigInteger> factors = new ArrayList<>();
        BigInteger two = BigInteger.valueOf(2);

        while (num.mod(two).equals(BigInteger.ZERO)) {
            if (!factors.contains(two)) {
                factors.add(two);
            }
            num = num.divide(two);
        }
        for (BigInteger i = BigInteger.valueOf(3); i.compareTo(num.sqrt()) <= 0; i = i.add(two)) {
            while (num.mod(i).equals(BigInteger.ZERO)) {
                if (!factors.contains(i)) {
                    factors.add(i);
                }
                num = num.divide(i);
            }
        }
        if (num.compareTo(BigInteger.ONE) > 0) {
            factors.add(num);
        }

        return factors;
    }

    public static byte[] generateOwnPrivateKey() {
        return new BigInteger(32, randGenerator).toByteArray();
    }

    public static byte[] generateOwnPublicKey(byte[] privateKey, byte[] g, byte[] p) {
        BigInteger G = new BigInteger(g);
        BigInteger P = new BigInteger(p);
        BigInteger ownPrivateKey = new BigInteger(privateKey);
        return G.modPow(ownPrivateKey, P).toByteArray();
    }

    public static byte[] generateSharedPrivateKey(byte[] otherUserPublicKey, byte[] ownPrivateKey, byte[] p) {
        BigInteger publicKey = new BigInteger(otherUserPublicKey);
        BigInteger privateKey = new BigInteger(ownPrivateKey);
        BigInteger P = new BigInteger(p);
        BigInteger key = publicKey.modPow(privateKey, P);

        byte[] sharedPrivateKey = new byte[16];
        int length = Math.min(key.toByteArray().length, sharedPrivateKey.length);
        System.arraycopy(key.toByteArray(), 0, sharedPrivateKey, 0, length);
        return sharedPrivateKey;
    }

}
