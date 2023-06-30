package Utils;

import java.util.BitSet;

public class BitsManipulation {
        public static BitSet bitsetFromBytes(byte[] byteArr) {
        BitSet res = new BitSet(byteArr.length * Byte.SIZE);
        for (int i = byteArr.length - 1; i >= 0; --i) {
            for (int j = 0; j < Byte.SIZE; ++j) {
                boolean bit = ((byteArr[i] >> j) & 1) == 1;
                res.set((byteArr.length - 1 - i) * Byte.SIZE + j, bit);
            }
        }

        return res;    
    }

    public static int intFromBitSet(BitSet bs, int startIx, int endIx) {
        int value = 0;
        for (int i = startIx; i < endIx; ++i) {
            int bit = bs.get(i) ? 1 : 0;
            value |= (bit << (i - startIx));
        }

        return value;
    }

    public static void setBitsetFromByte(BitSet bs, byte val, int from, int count) {
        for (int i = 0; i < count; ++i) {
            bs.set(from + i, getNthBit(val, i));
        }
    }
        
    public static boolean getNthBit(int num, int n) {
        while (n-- != 0) {
            num = num >> 1;
        }
        return (num & 1) == 1;
    }
}
