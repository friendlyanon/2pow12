package map.intint;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Tools {
    private final int INT_PHI = 0x9E3779B9;

    public long nextPowerOfTwo(long x) {
        if (x == 0) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return (x | x >> 32) + 1;
    }

    public int arraySize(final int expected, final float f) {
        val s = Math.max(2, nextPowerOfTwo((long) Math.ceil(expected / f)));
        return (int) s;
    }

    public int phiMix(final int x) {
        val h = x * INT_PHI;
        return h ^ (h >> 16);
    }
}
