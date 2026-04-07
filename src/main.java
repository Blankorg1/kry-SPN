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

  // private static final String KEY = "00111010100101001101011000111111";
  private static final String KEY = "00010001001010001000110000000000";
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
    String chiffreText = readChiffreFile("./test.txt");
    encrypt(chiffreText);
  }

  public static String encrypt(String chiffreText) {
    String encrypted = "";

    for (int blockCount = 0; blockCount < (chiffreText.length() / BLOCK_SIZE); ++blockCount) {
      // initial step round 0
      int chiffreBlock = getChunkChiffre(chiffreText, blockCount);
      chiffreBlock = xorRoundKey(chiffreBlock, calcRoundKey(0));

      // rounds 1-3
      for (int round = 1; round < R; ++round) {
        chiffreBlock = applySBox(chiffreBlock);
        chiffreBlock = applyBitPermutation(chiffreBlock);
        chiffreBlock = xorRoundKey(chiffreBlock, calcRoundKey(round));
      }

      // final round
      chiffreBlock = applySBox(chiffreBlock);
      chiffreBlock = xorRoundKey(chiffreBlock, calcRoundKey(R));

      String bits16 = String.format("%16s", Integer.toBinaryString(chiffreBlock)).replace(' ', '0');
      encrypted += bits16;
    }

    return encrypted;
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

  public static int xorRoundKey(int chiffreBlock, int roundKey) {
    return chiffreBlock ^ roundKey;
  }

  public static int applySBox(int chiffreBlock) {
    int resultSBox = 0;
    for (int i = 0; i < M; ++i) {
      int wordChunk = chiffreBlock & 0xF;
      chiffreBlock = chiffreBlock >> N;
      int lookup = S_BOX[wordChunk];
      resultSBox = (resultSBox << N) | lookup;
    }

    return resultSBox;
  }

  public static void applyPermutation() {
  }

  public static String readChiffreFile(String path) throws IOException {
    return Files.readString(Path.of(path));
  }
}
