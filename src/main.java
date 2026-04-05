public class main {

    /*
    r = 4
    n = 4
    m = 4

    S-Box: x    | 0 1 2 3 4 5 6 7 8 9 A B C D E F
           S(x) | E 4 D 1 2 F B 8 3 A 6 C 5 9 0 7

    Bitpermutation: 
            0 1 2 3  4 5 6 7  8 9 10 11 12 13 14 15
            0 4 8 12 1 5 9 13 2 6 10 14 3  7  11 15

    K(k, i) bestehe aus den 16 aufeinanderfolgenden Bits von k beginnend bei Position 4i
    k = 0011 1010 1001 0100 1101 0110 0011 1111

    k0 = 0011 1010 1001 0100
    k1 = 1010 1001 0100 1101
    k2 = 1001 0100 1101 0110
    k3 = 0100 1101 0110 0011
     */
    public void main(String arcs[]) {
        decrypt("10000001 01101011 00000001");
    }

    public void encrypt(String plain) {

    }


    public void decrypt(String binary) {
        // delete all spaces
        String formatted = binary.replace(" ", "");
        int len = formatted.length();

        // check that binary are hole bytes
        if (len % 8 != 0) {
            throw new IllegalArgumentException("Binary string length must be multiple of 8");
        }

        byte[] be = new byte[len / 8];

        // filles bytearray with the to decode bytes one byte at a time 
        for (int i = 0; i < be.length; i++) {
            String oneByte = formatted.substring(i * 8, i * 8 + 8);
            be[i] = (byte) Integer.parseInt(oneByte, 2);
        }

        be[0] = (byte) (be[0] ^ 0b01001101);

        
        for (byte bi : be) {
            System.out.println(bi);
        }
    }
}
