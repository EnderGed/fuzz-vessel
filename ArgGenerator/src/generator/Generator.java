package generator;

import generator.shadows.BundleShadow;
import generator.shadows.FileDescriptorShadow;
import generator.shadows.ParcelableShadow;
import generator.shadows.SparseArrayShadow;
import generator.shadows.SparseBooleanArrayShadow;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A massive class for generating some values. Some parts of it are going to be
 * replaced by the generator written by people who live downstairs.
 */
// TODO: CLEAN THIS UP. Remove redundant stuff. Make it work in accordance to
// some consistent logic.
public class Generator {

	private int defaultQuantity = 64;
	private int defaultStringLength = 128;
	private Class<?>[] simpleClasses = { boolean.class, byte.class, char.class,
			double.class, float.class, int.class, long.class, short.class,
			java.lang.String.class };

	private Random rand;

	public Generator() {
		rand = new Random();
	}

	/**
	 * Populates all arguments given by the Vessel with generated values.
	 * 
	 * @param rawArgs
	 * @return
	 */
	public Object[] generateValuesFromRaw(Object[] rawArgs) {
		for (int i = 0; i < rawArgs.length; ++i)
			rawArgs[i] = generateValueFromRaw(rawArgs[i]);
		return rawArgs;
	}

	/**
	 * Populates a single raw object with a random value.
	 * 
	 * @param raw
	 * @return
	 */
	public Object generateValueFromRaw(Object raw) {
		if (raw instanceof String)
			return getRandomArgFromClassName((String) raw);
		else if (isListMapOrSparseArray(raw))
			return generateCollection(raw);
		else if (raw instanceof FileDescriptorShadow)
			fillFileDescriptor((FileDescriptorShadow) raw);
		else if (raw instanceof SparseBooleanArrayShadow)
			fillSparseBooleanArray((SparseBooleanArrayShadow) raw);
		else if (raw instanceof BundleShadow)
			fillBundle((BundleShadow) raw);
		else if (raw instanceof ParcelableShadow)
			fillShadow((ParcelableShadow) raw);
		else
			System.err.println("Generator says: WTF? Leaving raw.");
		return raw;
	}

	/**
	 * Gets random object of a class based on its full name.
	 * 
	 * @param name
	 * @return
	 */
	public Object getRandomArgFromClassName(String name) {
		Class<?> c;
		try {
			c = getClassFromName(name);
		} catch (ClassNotFoundException cnfe) {
			System.err.println("No such class found: " + name + ".");
			return null;
		}
		return getRandomObjectFromClass(c);
	}

	/**
	 * Prepares some pretty name for the file descriptor. The Ghost will
	 * recreate it using Object Input/Output Stream.
	 * 
	 * @param fds
	 */
	private void fillFileDescriptor(FileDescriptorShadow fds) {
		fds.setFileName((String) getRandomObjectFromClass(String.class));
	}

	/**
	 * Populates the Parcelable Shadow's field names and constructor args.
	 * 
	 * @param ps
	 */
	private void fillShadow(ParcelableShadow ps) {
		ps.setConstructorArgs(generateValuesFromRaw(ps.getConstructorArgs()));
		for (String key : ps.getKeys())
			ps.insertDatum(key, generateValueFromRaw(ps.getDatum(key)));
	}

	/**
	 * Fill Sparse Boolean Array with default number of values.
	 * 
	 * @param sbas
	 */
	private void fillSparseBooleanArray(SparseBooleanArrayShadow sbas) {
		fillSparseBooleanArray(sbas, defaultQuantity);
	}

	/**
	 * Fills Sparse Boolean Array with a given quantity of values.
	 * 
	 * @param sbas
	 * @param quantity
	 */
	private void fillSparseBooleanArray(SparseBooleanArrayShadow sbas,
			int quantity) {
		for (int i = 0; i < quantity; ++i)
			sbas.append(rand.nextInt(), rand.nextInt(2) == 0);
	}

	/**
	 * Populates the weaved Bundle Shadow.
	 * 
	 * @param bs
	 */
	private void fillBundle(BundleShadow bs) {
		for (String key : bs.getKeys())
			bs.addElement(key, generateValueFromRaw(bs.getElement(key)));
	}

	/**
	 * Lists, Maps and Sparse Arrays are the only parcelable from-generic
	 * collections which we can parcelize.
	 * 
	 * @param obj
	 * @return
	 */
	private boolean isListMapOrSparseArray(Object obj) {
		return obj instanceof List || obj instanceof Map
				|| obj instanceof SparseArrayShadow;
	}

	/**
	 * Generates a specified collection with default quantity.
	 * 
	 * @param coll
	 * @return
	 */
	private Object generateCollection(Object coll) {
		return generateCollection(coll, defaultQuantity);
	}

	/**
	 * Populate a typed collection of a supported type, i.e. a Sparse Array
	 * Shadow, a List (as an Array List) or a Map (as a Hash Map).
	 * 
	 * @param coll
	 * @param quantity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object generateCollection(Object coll, int quantity) {
		if (coll instanceof SparseArrayShadow) {
			SparseArrayShadow<Object> collection = (SparseArrayShadow<Object>) coll;
			Object sampleContent = (collection).valueAt(0);
			for (int i = 0; i < quantity; ++i)
				collection.append(rand.nextInt(),
						generateValueFromRaw(sampleContent));
			return collection;
		}
		if (coll instanceof List) {
			ArrayList<Object> collection = (ArrayList<Object>) coll;
			Object sampleContent = (collection).get(0);
			for (int i = 0; i < quantity; ++i)
				collection.add(generateValueFromRaw(sampleContent));
			return collection;
		}
		if (coll instanceof Map) {
			HashMap<Object, Object> collection = (HashMap<Object, Object>) coll;
			Object sampleKey = collection.keySet().iterator().next();
			Object sampleContent = (collection).get(sampleKey);
			for (int i = 0; i < quantity; ++i)
				collection.put(generateValueFromRaw(sampleKey),
						generateValueFromRaw(sampleContent));
			return collection;
		}
		System.err.println("WTF is this collection? "
				+ coll.getClass().getName() + "; returning null.");
		return null;
	}

	/**
	 * Returns "Class" objects for a given class name array. Does NOT handle
	 * Android classes (inserts Object.class wherever it finds an unknown
	 * class).
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?>[] getClassesFromClassNames(String[] names) {
		Class<?>[] result = new Class<?>[names.length];
		for (int i = 0; i < names.length; ++i) {
			try {
				result[i] = getClassFromName(names[i]);
			} catch (ClassNotFoundException cnfe) {
				result[i] = Object.class;
			}
		}
		return result;
	}

	/**
	 * Returns a "Class" object from a given class name. Enables the Generator
	 * to treat primitives and objects equally. Does NOT handle Android classes.
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getClassFromName(String className)
			throws ClassNotFoundException {
		switch (className) {
		case "boolean":
			return boolean.class;
		case "byte":
			return byte.class;
		case "char":
			return char.class;
		case "double":
			return double.class;
		case "float":
			return float.class;
		case "int":
			return int.class;
		case "long":
			return long.class;
		case "short":
			return short.class;
		default:
			try {
				return ClassLoader.getSystemClassLoader().loadClass(className);
			} catch (ClassNotFoundException cnfe) {
				System.err.println(cnfe.getMessage());
				throw cnfe;
			}
		}
	}

	private boolean generatedSomewhereElse(Class<?> cl) {
		return cl == FileDescriptor.class || List.class.isAssignableFrom(cl)
				|| Map.class.isAssignableFrom(cl);
	}

	private Object generateSomewhereElse(Class<?> cl, int quantity) {
		if (cl == FileDescriptor.class)
			return new FileDescriptorShadow(getRandomCharSequence(cl));
		if (List.class.isAssignableFrom(cl)) {
			ArrayList<Object> al = new ArrayList<>();
			Class<?> listType = getRandomSimpleType();
			for (int i = 0; i < quantity; ++i)
				al.add(getRandomObjectFromClass(listType, quantity));
			return al;
		}
		if (Map.class.isAssignableFrom(cl)) {
			HashMap<Object, Object> hm = new HashMap<>();
			Class<?> keyType = getRandomSimpleType();
			Class<?> valueType = getRandomSimpleType();
			for (int i = 0; i < quantity; ++i)
				hm.put(getRandomObjectFromClass(keyType),
						getRandomObjectFromClass(valueType));
			return hm;
		}
		return null;
	}

	/**
	 * Returns a simple class. At this time, return either some primitive or a
	 * string because strings are special you know.
	 * 
	 * @return
	 */
	private Class<?> getRandomSimpleType() {
		return simpleClasses[rand.nextInt(simpleClasses.length)];
	}

	/**
	 * Returns a random object from class using default array length.
	 * 
	 * @param cl
	 * @return
	 */
	public Object getRandomObjectFromClass(Class<?> cl) {
		return getRandomObjectFromClass(cl, defaultQuantity);
	}

	/**
	 * Use a "Class" object to generate everything which isn't generated by the
	 * other methods: primitives, arrays and some hopefully-collection-unrelated
	 * Java classes. No, it cannot generate Android classes.
	 * 
	 * TODO: Handle recursion. Lots of ugly stuff there.
	 * 
	 * @param cl
	 * @param quantity
	 * @return
	 */
	public Object getRandomObjectFromClass(Class<?> cl, int quantity) {
		if (generatedSomewhereElse(cl))
			return generateSomewhereElse(cl, quantity);
		if (cl.equals(Boolean.class) || cl.equals(boolean.class))
			return rand.nextInt(2) == 0;
		if (cl.equals(Byte.class) || cl.equals(byte.class))
			return getRandomBytes(1)[0];
		if (cl.equals(Character.class) || cl.equals(char.class))
			return (char) (getRandomBytes(1)[0]);
		if (cl.equals(Double.class) || cl.equals(double.class))
			return rand.nextDouble();
		if (cl.equals(Float.class) || cl.equals(float.class))
			return rand.nextFloat();
		if (cl.equals(Integer.class) || cl.equals(int.class))
			return rand.nextInt();
		if (cl.equals(Long.class) || cl.equals(long.class))
			return rand.nextLong();
		if (cl.equals(Short.class) || cl.equals(short.class))
			return (short) (rand.nextInt());
		if (CharSequence.class.isAssignableFrom(cl))
			return getRandomCharSequence(cl);
		if (Serializable.class.isAssignableFrom(cl))
			return getRandomSerializable(cl);
		if (cl.isArray()) {
			Object[] objs = new Object[(new Random()).nextInt(quantity)];
			for (int i = 0; i < objs.length; ++i)
				objs[i] = getRandomObjectFromClass(cl.getComponentType());
			return objs;
		}
		// HELL. Will do this later.
		try {
			Constructor<?>[] constructors = cl.getDeclaredConstructors();
			if (constructors.length == 0) {
				System.out.println("No public constructor for class "
						+ cl.getName() + ".");
				if (cl.isInterface())
					return null;
				else
					return null;
			}
			// favor default constructors //no better idea at this time.
			for (Constructor<?> c : constructors)
				if (c.getParameterTypes().length == 0)
					return c.newInstance();
			int index = rand.nextInt(constructors.length);
			Class<?>[] cTypes = constructors[index].getParameterTypes();
			Object[] cArgs = new Object[cTypes.length];
			// printConstructorInfo(cl, index, cTypes);
			for (int i = 0; i < cTypes.length; ++i)
				cArgs[i] = getRandomObjectFromClass(cTypes[i]);
			return constructors[index].newInstance(cArgs);
		} catch (Exception e) {
			System.out.println("Could not create object of class "
					+ cl.getName() + " due to an \"" + e.getMessage()
					+ "\" exception; returning null");
			return null;
		}
	}

	/*
	 * TODO: Implement this.
	 */
	private Serializable getRandomSerializable(Class<?> cl) {
		return null;
	}

	private String getRandomCharSequence(Class<?> cl) {
		return getRandomCharSequence(cl, defaultStringLength);
	}

	private String getRandomCharSequence(Class<?> cl, int strLen) {
		byte[] buffer = getRandomBytes(strLen);
		rand.nextBytes(buffer);
		try {
			return new String(buffer, "UTF-16");
		} catch (UnsupportedEncodingException uee) {
			System.out.println("ERROR: UTF-16 is unsupported here!");
			return new String(buffer);
		}
	}

	private byte[] getRandomBytes(int length) {
		byte[] buffer = new byte[length];
		rand.nextBytes(buffer);
		return buffer;
	}

	/*
	 * A logging method. Does not exactly print constructor types; just names of
	 * some classes stored in variables. TODO: Delete this before release.
	 * 
	 * @hide
	 * 
	 * @param cl
	 * 
	 * @param index
	 * 
	 * @param cTypes
	 */
	@SuppressWarnings("unused")
	private void printConstructorInfo(Class<?> cl, int index, Class<?>[] cTypes) {
		StringBuilder info = new StringBuilder(
				"Attempting to create new instance of class ");
		info.append(cl.getName());
		info.append(" from constructor no. ");
		info.append(index);
		info.append(": ");
		for (Class<?> cType : cTypes) {
			info.append(cType.getName());
			info.append(" ");
		}
		System.out.println(info.toString());
	}

	public int getDefaultQuantity() {
		return defaultQuantity;
	}

	public void setDefaultQuantity(int defaultQuantity) {
		this.defaultQuantity = defaultQuantity;
	}

	public int getDefaultStringLength() {
		return defaultStringLength;
	}

	public void setDefaultStringLength(int defaultStringLength) {
		this.defaultStringLength = defaultStringLength;
	}

}
