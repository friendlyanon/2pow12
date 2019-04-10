package map.intint;

import lombok.val;

public class IntIntMap {
    public static final int NO_VALUE = 0;
    private static final int FREE_KEY = 0;
    private final float fillFactor;
    public int[] data;
    private boolean hasFreeKey;
    private int freeValue;
    private int threshold;
    private int size;
    private int mask;
    private int mask2;

    public IntIntMap(final int size, final float fillFactor) {
        val capacity = Tools.arraySize(size, fillFactor);
        mask = capacity - 1;
        mask2 = capacity * 2 - 1;
        this.fillFactor = fillFactor;

        data = new int[capacity * 2];
        threshold = (int) (capacity * fillFactor);
    }

    public int get(final int key) {
        var ptr = (Tools.phiMix(key) & mask) << 1;

        if (key == FREE_KEY) {
            return hasFreeKey ? freeValue : NO_VALUE;
        }

        var k = data[ptr];

        if (k == FREE_KEY) {
            return NO_VALUE;
        }
        if (k == key) {
            return data[ptr + 1];
        }

        while (true) {
            ptr = (ptr + 2) & mask2;
            k = data[ptr];
            if (k == FREE_KEY) {
                return NO_VALUE;
            }
            if (k == key) {
                return data[ptr + 1];
            }
        }
    }

    public int put(final int key, final int value) {
        if (key == FREE_KEY) {
            val ret = freeValue;
            if (!hasFreeKey) {
                ++size;
            }
            hasFreeKey = true;
            freeValue = value;
            return ret;
        }

        var ptr = (Tools.phiMix(key) & mask) << 1;
        var k = data[ptr];
        if (k == FREE_KEY) {
            data[ptr] = key;
            data[ptr + 1] = value;
            if (size >= threshold) {
                rehash(data.length * 2);
            }
            else {
                ++size;
            }
            return NO_VALUE;
        }
        else if (k == key) {
            val ret = data[ptr + 1];
            data[ptr + 1] = value;
            return ret;
        }

        while (true) {
            ptr = (ptr + 2) & mask2;
            k = data[ptr];
            if (k == FREE_KEY) {
                data[ptr] = key;
                data[ptr + 1] = value;
                if (size >= threshold) {
                    rehash(data.length * 2);
                }
                else {
                    ++size;
                }
                return NO_VALUE;
            }
            else if (k == key) {
                val ret = data[ptr + 1];
                data[ptr + 1] = value;
                return ret;
            }
        }
    }

    public int remove(final int key) {
        if (key == FREE_KEY) {
            if (!hasFreeKey) {
                return NO_VALUE;
            }
            hasFreeKey = false;
            --size;
            return freeValue;
        }

        int ptr = (Tools.phiMix(key) & mask) << 1;
        int k = data[ptr];
        if (k == key) {
            val res = data[ptr + 1];
            shiftKeys(ptr);
            --size;
            return res;
        }
        else if (k == FREE_KEY) {
            return NO_VALUE;
        }

        while (true) {
            ptr = (ptr + 2) & mask2;
            k = data[ptr];
            if (k == key) {
                val res = data[ptr + 1];
                shiftKeys(ptr);
                --size;
                return res;
            }
            else if (k == FREE_KEY) {
                return NO_VALUE;
            }
        }
    }

    private void shiftKeys(int pos) {
        int last, slot;
        int k;
        val data = this.data;
        while (true) {
            pos = ((last = pos) + 2) & mask2;
            while (true) {
                if ((k = data[pos]) == FREE_KEY) {
                    data[last] = FREE_KEY;
                    return;
                }
                slot = (Tools.phiMix(k) & mask) << 1;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }
                pos = (pos + 2) & mask2;
            }
            data[last] = k;
            data[last + 1] = data[pos + 1];
        }
    }


    public int size() {
        return size;
    }

    private void rehash(int newCapacity) {
        data = new int[newCapacity];
        mask2 = newCapacity - 1;
        threshold = (int) ((newCapacity >>= 1) * fillFactor);
        mask = newCapacity - 1;

        val oldCapacity = data.length;
        val oldData = data;

        size = hasFreeKey ? 1 : 0;

        for (int i = 0; i < oldCapacity; i += 2) {
            val oldKey = oldData[i];
            if (oldKey != FREE_KEY) {
                put(oldKey, oldData[i + 1]);
            }
        }
    }
}

