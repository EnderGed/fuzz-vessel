package vessel.generator;


import java.io.FileDescriptor;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import vessel.generator.shadows.BundleShadow;
import vessel.generator.shadows.FileDescriptorShadow;
import vessel.generator.shadows.ParcelableShadow;
import vessel.generator.shadows.SparseArrayShadow;
import vessel.generator.shadows.SparseBooleanArrayShadow;

/*
 * A massive class for generating some values. Some parts of it are going to be replaced by
 * the generator written by people who live downstairs.
 */
//TODO: CLEAN THIS UP. Remove redundant stuff. Make it work in accordance to some consistent logic.
public class Generator {

	private Class<?>[] parcelableTypes;
	private Random rand;

	public Generator() {
		parcelableTypes = readParcelableTypes();
		rand = new Random();
	}

	public Object[] getRandomArgsFromClassNames(String[] names) {
		Object[] objs = new Object[names.length];
		for (int i = 0; i < names.length; ++i) {
			Pattern p = Pattern.compile(".*<.*>");
			if (p.matcher(names[i]).matches()) {
				try {
					objs[i] = generateFromGenericCollectionName(names[i], 100);
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				}
			} else {
				try {
					objs[i] = getRandomObjectFromClass(getClassFromName(names[i]));
				} catch (ClassNotFoundException cnfe) {
					if (lookupSourceIsParcelable(names[i]))
						objs[i] = new ParcelableShadow(names[i]);
					else {
						cnfe.printStackTrace();
						objs[i] = null;
					}
				}
			}
		}
		return objs;
	}

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
		case "android.os.Bundle":
			return BundleShadow.class;
		case "android.util.SparseBooleanArray":
			return SparseBooleanArrayShadow.class;
		case "java.io.FileDescriptor":
			return FileDescriptorShadow.class;
		default:
			try {
				return ClassLoader.getSystemClassLoader().loadClass(className);
			} catch (ClassNotFoundException cnfe) {
				throw cnfe;
			}
		}
	}
	
	public Class<?>[] getClassesFromClassNames(String[] names){
		Class<?>[] result = new Class<?>[names.length];
		for(int i=0; i<names.length; ++i){
			try{
			result[i] = getClassFromName(names[i]);
			}catch(ClassNotFoundException cnfe){
				result[i] = Object.class;
			}
		}
		return result;
	}

	// for lists, sparse arrays and stuff
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object generateFromGenericCollectionName(String colName, int quantity)
			throws ClassNotFoundException {
		try {
			Object collection = getRandomObjectFromClass(getClassFromName(colName
					.substring(0, colName.indexOf('<'))));
			String[] typeNames = colName.substring(colName.indexOf('<') + 1,
					colName.indexOf('>')).split(",");

			if (collection instanceof List && typeNames.length == 1) {
				Class<?> type = ClassLoader.getSystemClassLoader().loadClass(
						typeNames[0]);
				for (int i = 0; i < quantity; ++i)
					((ArrayList) collection).add(type
							.cast(getRandomObjectFromClass(type)));
			}

			else if (collection instanceof Map && typeNames.length == 2) {
				Class<?> keyType = ClassLoader.getSystemClassLoader()
						.loadClass(typeNames[0]);
				Class<?> elemType = ClassLoader.getSystemClassLoader()
						.loadClass(typeNames[0]);
				for (int i = 0; i < quantity; ++i)
					((Map) collection).put(
							keyType.cast(getRandomObjectFromClass(keyType)),
							elemType.cast(getRandomObjectFromClass(elemType)));

			}

			else if (collection instanceof SparseArrayShadow
					&& typeNames.length == 1) {
				Class<?> type = ClassLoader.getSystemClassLoader().loadClass(
						typeNames[0]);
				for (int i = 0; i < quantity; ++i)
					((SparseArrayShadow) collection).put(
							(int) getRandomObjectFromClass(int.class),
							type.cast(getRandomObjectFromClass(type)));
			}

			else {
				throw new ClassNotFoundException();
			}

			return collection;
		} catch (ClassNotFoundException cnfe) {
			throw cnfe;
		}
	}

	public Object getRandomObjectFromClass(Class<?> cl) {
		return getRandomObjectFromClass(cl, 10, 512);
	}

	public Object getRandomObjectFromClass(Class<?> cl, int quantity, int strLen) {
		if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
			return rand.nextInt(2) == 0;
		}
		if (cl.equals(Byte.class) || cl.equals(byte.class)) {
			byte[] buffer = new byte[1];
			rand.nextBytes(buffer);
			return buffer[0];
		}
		if (cl.equals(Character.class) || cl.equals(char.class)) {
			byte[] buffer = new byte[1];
			rand.nextBytes(buffer);
			return (char) buffer[0];
		}
		if (cl.equals(Double.class) || cl.equals(double.class)) {
			return rand.nextDouble();
		}
		if (cl.equals(Float.class) || cl.equals(float.class)) {
			return rand.nextFloat();
		}
		if (cl.equals(Integer.class) || cl.equals(int.class)) {
			return rand.nextInt();
		}
		if (cl.equals(Long.class) || cl.equals(long.class)) {
			return rand.nextLong();
		}
		if (cl.equals(Short.class) || cl.equals(short.class)) {
			return (short) (rand.nextInt());
		}
		// no need to check if it's string here (?)
		if (CharSequence.class.isAssignableFrom(cl)) {
			byte[] buffer = new byte[rand.nextInt(strLen)];
			rand.nextBytes(buffer);
			try {
				return new String(buffer, "UTF-16");
			} catch (UnsupportedEncodingException uee) {
				System.out.println("ERROR: UTF-16 is unsupported here!");
				return new String(buffer);
			}
		}
		if (FileDescriptor.class.isAssignableFrom(cl)) {
			return new FileDescriptorShadow(
					(String) getRandomObjectFromClass(String.class));
		}
		if (Serializable.class.isAssignableFrom(cl)) {
			return null; // TODO: Create an object and populate its components.
		}
		// Parcels reconstruct all Maps as HashMaps.
		if (Map.class.isAssignableFrom(cl)) {
			return new HashMap<>();
		}
		// Parcels reconstruct all Lists as ArrayLists.
		if (List.class.isAssignableFrom(cl)) {
			return new ArrayList<>();
		}
		if (cl.equals(SparseArrayShadow.class)) {
			return new SparseArrayShadow<>();
		}
		// SparseBooleanArrays don't use generic types: they can be constructed
		// here.
		if (cl.equals(SparseBooleanArrayShadow.class)) {
			SparseBooleanArrayShadow sba = new SparseBooleanArrayShadow();
			for (int i = 0; i < quantity; ++i)
				sba.put((int) getRandomObjectFromClass(int.class),
						(boolean) getRandomObjectFromClass(boolean.class));
			return sba;
		}
		if (cl.equals(BundleShadow.class)) {
			BundleShadow bs = new BundleShadow();
			for (int i = 0; i < quantity; ++i) {
				bs.addElement((String) getRandomObjectFromClass(String.class),
						generateRandomParcelableObject());
			}
			return bs;
		}
		if (cl.isArray()) {
			Object[] objs = new Object[(new Random()).nextInt(quantity)];
			for (int i = 0; i < objs.length; ++i)
				objs[i] = getRandomObjectFromClass(cl.getComponentType());
			return objs;
		}
		try {
			Constructor<?>[] constructors = cl.getConstructors();
			if (constructors.length == 0) {
				System.out.println("No public constructor for class "
						+ cl.getName() + "; returning null");
				return null;
			}
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

	public Object generateRandomParcelableObject() {
		return getRandomObjectFromClass(parcelableTypes[rand
				.nextInt(parcelableTypes.length)]);
	}

	/*
	 * TODO: This array is a placeholder. Normally, some array of possible
	 * parcelable types should be generated from the Android sources.
	 */
	private Class<?>[] readParcelableTypes() {
		Class<?>[] c = { String.class, Integer.class, Double.class };
		return c;
	}

	public boolean lookupSourceIsParcelable(String className) {
		return lookupSourceIsInstance("android.os.Parcelable");
	}

	/*
	 * TODO: Check if the class is an instance of some Android class. This needs
	 * to be done by checking the source code.
	 */
	public boolean lookupSourceIsInstance(String className) {
		return true;
	}

	public void printConstructorInfo(Class<?> cl, int index, Class<?>[] cTypes) {
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

}
