package vessel.generator.shadows;

/*
 * Copy-pasted from the Android implementation of SparseBooleanArray.
 */
public class SparseBooleanArrayShadow implements Cloneable {

    public SparseBooleanArrayShadow() {
        this(10);
    }

    public SparseBooleanArrayShadow(int initialCapacity) {
        initialCapacity = idealIntArraySize(initialCapacity);

        mKeys = new int[initialCapacity];
        mValues = new boolean[initialCapacity];
        mSize = 0;
    }

    @Override
    public SparseBooleanArrayShadow clone() {
        SparseBooleanArrayShadow clone = null;
        try {
            clone = (SparseBooleanArrayShadow) super.clone();
            clone.mKeys = mKeys.clone();
            clone.mValues = mValues.clone();
        } catch (CloneNotSupportedException cnse) {
            /* ignore */
        }
        return clone;
    }

    public boolean get(int key) {
        return get(key, false);
    }

    public boolean get(int key, boolean valueIfKeyNotFound) {
        int i = binarySearch(mKeys, 0, mSize, key);

        if (i < 0) {
            return valueIfKeyNotFound;
        } else {
            return mValues[i];
        }
    }

    public void delete(int key) {
        int i = binarySearch(mKeys, 0, mSize, key);

        if (i >= 0) {
            System.arraycopy(mKeys, i + 1, mKeys, i, mSize - (i + 1));
            System.arraycopy(mValues, i + 1, mValues, i, mSize - (i + 1));
            mSize--;
        }
    }

    public void put(int key, boolean value) {
        int i = binarySearch(mKeys, 0, mSize, key);

        if (i >= 0) {
            mValues[i] = value;
        } else {
            i = ~i;

            if (mSize >= mKeys.length) {
                int n = idealIntArraySize(mSize + 1);

                int[] nkeys = new int[n];
                boolean[] nvalues = new boolean[n];

                System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
                System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

                mKeys = nkeys;
                mValues = nvalues;
            }

            if (mSize - i != 0) {
                System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
                System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
            }

            mKeys[i] = key;
            mValues[i] = value;
            mSize++;
        }
    }

    public int size() {
        return mSize;
    }

    public int keyAt(int index) {
        return mKeys[index];
    }
    
    public boolean valueAt(int index) {
        return mValues[index];
    }

    public int indexOfKey(int key) {
        return binarySearch(mKeys, 0, mSize, key);
    }

    public int indexOfValue(boolean value) {
        for (int i = 0; i < mSize; i++)
            if (mValues[i] == value)
                return i;

        return -1;
    }

    public void clear() {
        mSize = 0;
    }

    public void append(int key, boolean value) {
        if (mSize != 0 && key <= mKeys[mSize - 1]) {
            put(key, value);
            return;
        }

        int pos = mSize;
        if (pos >= mKeys.length) {
            int n = idealIntArraySize(pos + 1);

            int[] nkeys = new int[n];
            boolean[] nvalues = new boolean[n];

            System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
            System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

            mKeys = nkeys;
            mValues = nvalues;
        }

        mKeys[pos] = key;
        mValues[pos] = value;
        mSize = pos + 1;
    }
    
    private static int binarySearch(int[] a, int start, int len, int key) {
        int high = start + len, low = start - 1, guess;

        while (high - low > 1) {
            guess = (high + low) / 2;

            if (a[guess] < key)
                low = guess;
            else
                high = guess;
        }

        if (high == start + len)
            return ~(start + len);
        else if (a[high] == key)
            return high;
        else
            return ~high;
    }

    private int[] mKeys;
    private boolean[] mValues;
    private int mSize;
    
	public static int idealByteArraySize(int need) {
		for (int i = 4; i < 32; i++)
			if (need <= (1 << i) - 12)
				return (1 << i) - 12;

		return need;
	}

	public static int idealIntArraySize(int need) {
		return idealByteArraySize(need * 4) / 4;
	}
}
