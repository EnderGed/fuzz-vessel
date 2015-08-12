package vessel.generator.shadows;

/*
 * Copy-pasted from the Android implementation of SparseArray.
 */
public class SparseArrayShadow<E> implements Cloneable {

	private static final Object DELETED = new Object();
	private boolean mGarbage = false;

	private int[] mKeys;
	private Object[] mValues;
	private int mSize;

	public SparseArrayShadow() {
		this(10);
	}

	public SparseArrayShadow(int initialCapacity) {
		initialCapacity = idealIntArraySize(initialCapacity);

		mKeys = new int[initialCapacity];
		mValues = new Object[initialCapacity];
		mSize = 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SparseArrayShadow<E> clone() {
		SparseArrayShadow<E> clone = null;
		try {
			clone = (SparseArrayShadow<E>) super.clone();
			clone.mKeys = mKeys.clone();
			clone.mValues = mValues.clone();
		} catch (CloneNotSupportedException cnse) {
		}
		return clone;
	}

	public E get(int key) {
		return get(key, null);
	}

	@SuppressWarnings("unchecked")
	public E get(int key, E valueIfKeyNotFound) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i < 0 || mValues[i] == DELETED) {
			return valueIfKeyNotFound;
		} else {
			return (E) mValues[i];
		}
	}

	public void delete(int key) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			if (mValues[i] != DELETED) {
				mValues[i] = DELETED;
				mGarbage = true;
			}
		}
	}

	public void remove(int key) {
		delete(key);
	}

	public void removeAt(int index) {
		if (mValues[index] != DELETED) {
			mValues[index] = DELETED;
			mGarbage = true;
		}
	}

	private void gc() {
		int n = mSize;
		int o = 0;
		int[] keys = mKeys;
		Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			Object val = values[i];

			if (val != DELETED) {
				if (i != o) {
					keys[o] = keys[i];
					values[o] = val;
					values[i] = null;
				}

				o++;
			}
		}

		mGarbage = false;
		mSize = o;
	}

	public void put(int key, E value) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			mValues[i] = value;
		} else {
			i = ~i;

			if (i < mSize && mValues[i] == DELETED) {
				mKeys[i] = key;
				mValues[i] = value;
				return;
			}

			if (mGarbage && mSize >= mKeys.length) {
				gc();

				i = ~binarySearch(mKeys, 0, mSize, key);
			}

			if (mSize >= mKeys.length) {
				int n = idealIntArraySize(mSize + 1);

				int[] nkeys = new int[n];
				Object[] nvalues = new Object[n];

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
		if (mGarbage) {
			gc();
		}

		return mSize;
	}

	public int keyAt(int index) {
		if (mGarbage) {
			gc();
		}

		return mKeys[index];
	}

	@SuppressWarnings("unchecked")
	public E valueAt(int index) {
		if (mGarbage) {
			gc();
		}

		return (E) mValues[index];
	}

	public void setValueAt(int index, E value) {
		if (mGarbage) {
			gc();
		}

		mValues[index] = value;
	}

	public int indexOfKey(int key) {
		if (mGarbage) {
			gc();
		}

		return binarySearch(mKeys, 0, mSize, key);
	}

	public int indexOfValue(E value) {
		if (mGarbage) {
			gc();
		}

		for (int i = 0; i < mSize; i++)
			if (mValues[i] == value)
				return i;

		return -1;
	}

	public void clear() {
		int n = mSize;
		Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			values[i] = null;
		}

		mSize = 0;
		mGarbage = false;
	}

	public void append(int key, E value) {
		if (mSize != 0 && key <= mKeys[mSize - 1]) {
			put(key, value);
			return;
		}

		if (mGarbage && mSize >= mKeys.length) {
			gc();
		}

		int pos = mSize;
		if (pos >= mKeys.length) {
			int n = idealIntArraySize(pos + 1);

			int[] nkeys = new int[n];
			Object[] nvalues = new Object[n];

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