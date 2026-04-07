import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  // r = 4
  // n = 4 wordsize
  // m = 4 wordcount
  // s = 32 keylength
  //
  // S-Box: x | 0 1 2 3 4 5 6 7 8 9 A B C D E F
  // S(x) | E 4 D 1 2 F B 8 3 A 6 C 5 9 0 7
  //
  // Bitpermutation:
  // x | 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
  // S(x) | 0 4 8 12 1 5 9 13 2 6 10 14 3 7 11 15
  //
  // K(k, i) bestehe aus den 16 aufeinanderfolgenden Bits von k beginnend bei
  // Position 4i
  // k = 0011 1010 1001 0100 1101 0110 0011 1111
  //
  // k0 = 0011 1010 1001 0100
  // k1 = 1010 1001 0100 1101
  // k2 = 1001 0100 1101 0110
  // k3 = 0100 1101 0110 0011
  //
  // Beispiel:
  // x = 0001 0010 1000 1111
  // k = 0001 0001 0010 1000 1000 1100 0000 0000
  // y = 1010 1110 1011 0100

  // r = 4
  // n = 4 wordsize
  // m = 4 wordcount
  // s = 32 keylength

  private static final String KEY = "00111010100101001101011000111111";
  // test key
  // private static final String KEY = "00010001001010001000110000000000";
  // private static final int R = 4; // number of rounds
  private static final int R = 4; // number of rounds
  private static final int N = 4; // bits per S-box input/output
  private static final int M = 4; // number of S-box chunks per block
  private static final int S = 32;
  private static final int BLOCK_SIZE = N * M; // spn block size

  private static final int[] S_BOX = {
      0xE, 0x4, 0xD, 0x1,
      0x2, 0xF, 0xB, 0x8,
      0x3, 0xA, 0x6, 0xC,
      0x5, 0x9, 0x0, 0x7
  };

  private static final int[] BIT_PERMUTATION = {
      0, 4, 8, 12,
      1, 5, 9, 13,
      2, 6, 10, 14,
      3, 7, 11, 15
  };

  public static void main(String[] args) throws IOException {
    String chiffreText = readChiffreFile("./chiffre.txt");
    String decryptedBits = decryptCTR(chiffreText);
    String unpaddedChiffre = removePadding(decryptedBits);
    System.out.println(bitsToAscii(unpaddedChiffre));
  }

  public static int encryptBlock(int block) {
    block = xor(block, calcRoundKey(0));

    // rounds 1-3
    for (int round = 1; round < R; ++round) {
      block = applySBox(block);
      block = applyBitPermutation(block);
      block = xor(block, calcRoundKey(round));
    }

    // final round
    block = applySBox(block);
    block = xor(block, calcRoundKey(R));

    return block;
  }

  public static String decryptCTR(String chiffreText) {
    String bitsResult = "";

    int initialCounter = getChunkChiffre(chiffreText, 0);

    int blockCount = (chiffreText.length() / BLOCK_SIZE);

    for (int blockIndex = 1; blockIndex < blockCount; ++blockIndex) {
      int chiffreBlock = getChunkChiffre(chiffreText, blockIndex);
      int counterBlock = initialCounter + (blockIndex - 1);

      int keyStreamBlock = encryptBlock(counterBlock);
      int textBlock = xor(chiffreBlock, keyStreamBlock);
      String bits16 = String.format("%16s", Integer.toBinaryString(textBlock)).replace(' ', '0');
      bitsResult += bits16;
    }

    return bitsResult;
  }

  private static String bitsToAscii(String bits) {
    if (bits.length() % 8 != 0) {
      throw new IllegalArgumentException("Yo this aint ascii");
    }

    String sb = "";
    for (int i = 0; i < bits.length(); i += 8) {
      int value = Integer.parseInt(bits.substring(i, i + 8), 2);
      sb += ((char) value);
    }

    return sb;
  }

  // encrypts a chiffretext spn
  private static String encrypt(String chiffreText) {
    String encrypted = "";

    for (int blockCount = 0; blockCount < (chiffreText.length() / BLOCK_SIZE); ++blockCount) {
      // initial step round 0
      int chiffreBlock = getChunkChiffre(chiffreText, blockCount);
      chiffreBlock = encryptBlock(chiffreBlock);

      String bits16 = String.format("%16s", Integer.toBinaryString(chiffreBlock)).replace(' ', '0');
      encrypted += bits16;
    }

    return encrypted;
  }

  // removes until 1 found
  private static String removePadding(String cipherText) {
    int i = cipherText.length() - 1;
    while (cipherText.charAt(i) == '0') {
      i--;
    }

    return cipherText.substring(0, i);
  }

  private static int applyBitPermutation(int chiffreBlock) {
    int result = 0;

    for (int i = 0; i < BLOCK_SIZE; ++i) {
      int bit = (chiffreBlock >> i) & 0b1;
      result |= (bit << BIT_PERMUTATION[i]);
    }

    return result;
  }

  public static int getChunkChiffre(String chiffreText, int round) {
    int start = round * BLOCK_SIZE;
    String chunkBlock = chiffreText.substring(start, start + BLOCK_SIZE);
    return Integer.parseUnsignedInt(chunkBlock, 2);
  }

  public static int calcRoundKey(int round) {
    int start = round * N;
    return Integer.parseUnsignedInt(KEY.substring(start, start + 16), 2);
  }

  public static int xor(int block, int key) {
    return block ^ key;
  }

  public static int applySBox(int chiffreBlock) {
    int resultSBox = 0;
    for (int i = M - 1; i >= 0; --i) {
      int shift = i * N;
      int wordChunk = (chiffreBlock >> shift) & 0xF;
      int lookup = S_BOX[wordChunk];
      resultSBox |= (lookup << shift);
    }

    return resultSBox;
  }

  public static String readChiffreFile(String path) throws IOException {
    return Files.readString(Path.of(path));
  }
}
