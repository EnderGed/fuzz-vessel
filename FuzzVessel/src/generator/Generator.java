package generator;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

//TODO: CLEAN THIS UP. Remove some redundant stuff. In some cases, replace Class with Type.
public class Generator {

	public Object[] getRandomArgsFromClassNames(String[] names) {
		Object[] objs = new Object[names.length];
		for (int i = 0; i < names.length; ++i) {
			Pattern p = Pattern.compile("*<*>");
			try {
				if (p.matcher(names[i]).matches())
					objs[i] = generateCollectionFromName(names[i], 100);
				else
					objs[i] = generateFromClassName(names[i]);
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
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
		default:
			try {
				return ClassLoader.getSystemClassLoader().loadClass(className);
			} catch (ClassNotFoundException cnfe) {
				throw cnfe;
			}
		}
	}

	// for lists, sparse arrays and stuff
	public Object generateCollectionFromName(String colName, int quantity)
			throws ClassNotFoundException {
		try {
			Object collection = generateFromClassName(colName.substring(0,
					colName.indexOf('<')));
			Class<?> type = ClassLoader.getSystemClassLoader().loadClass(
					colName.substring(colName.indexOf('<') + 1,
							colName.indexOf('>')));
			if (collection instanceof List) {

			} else if (collection instanceof Map) {

			} else if (collection instanceof SparseArray) {

			} else {
				throw new ClassNotFoundException();
			}
			return collection;
		} catch (ClassNotFoundException cnfe) {
			throw cnfe;
		}
	}

	public Object generateFromClassName(String className)
			throws ClassNotFoundException {
		try {
			return getRandomObjectFromClass(ClassLoader.getSystemClassLoader()
					.loadClass(className));
		} catch (ClassNotFoundException cnfe) {
			if (className == "android.os.Bundle") {
				return new BundleMaker();
			}
			if (lookupInSourceIfValid(className))
				return new Unknown(className);
			else
				throw cnfe;
		}
	}

	public boolean lookupInSourceIfValid(String className) {
		// TODO: Check if the class is a valid parcelable Android class; i.e.
		// implements Parcelable.
		// Is it a Binder? Or an Interface?
		// Is it a Bundle?
		return true;
	}

	public Object getRandomObjectFromClass(Class<?> cl) {
		return getRandomObjectFromClass(cl, 11, 512);
	}

	public Object getRandomObjectFromClass(Class<?> cl, int arrSize, int strLen) {
		if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
			return (new Random()).nextInt(2) == 0;
		}
		if (cl.isPrimitive()) {
			return (new Random()).nextInt();
		}
		if (cl.equals(Byte.class)) {
			byte[] buffer = new byte[1];
			(new Random()).nextBytes(buffer);
			return buffer[0];
		}
		if (cl.equals(Character.class)) {
			byte[] buffer = new byte[1];
			(new Random()).nextBytes(buffer);
			return (char) buffer[0];
		}
		if (cl.equals(Double.class)) {
			return (new Random()).nextDouble();
		}
		if (cl.equals(Float.class)) {
			return (new Random()).nextFloat();
		}
		if (cl.equals(Integer.class)) {
			return (new Random()).nextInt();
		}
		if (cl.equals(Long.class)) {
			return (new Random()).nextLong();
		}
		if (cl.equals(Short.class)) {
			return (short) ((new Random()).nextInt());
		}
		// no need to check if it's string here (?)
		if (cl.equals(CharSequence.class) || cl.equals(String.class)) {
			byte[] buffer = new byte[(new Random()).nextInt(512)];
			(new Random()).nextBytes(buffer);
			return buffer.toString();
		}
		if (cl.equals(FileDescriptor.class)) {
			return null; // TODO
		}
		if (cl.equals(Serializable.class)) {
			return null; // TODO
		}
		if (cl.equals(Map.class)) {
			return null; // TODO: check if <String, Object of supported type>
		}
		if (cl.equals(List.class)) {
			return new ArrayList<>();
		}
		if (cl.equals(SparseArray.class)) {
			return new SparseArray<>();
		}
		if (cl.equals(SparseBooleanArray.class)) {
			return new SparseBooleanArray();
		}
		if (cl.isArray()) {
			Object[] objs = new Object[(new Random()).nextInt(11)];
			for (int i = 0; i < objs.length; ++i)
				objs[i] = getRandomObjectFromClass(cl.getComponentType());
			return objs;
		}
		try {
			/*
			 * TODO: Check which constructors are made of parcelable classes
			 */
			Constructor<?>[] constructors = cl.getConstructors();
			if (constructors.length == 0) {
				System.out.println("No public constructor for class "
						+ cl.getName() + "; returning null");
				return null;
			}
			int index = (new Random()).nextInt(constructors.length);
			Class<?>[] cTypes = constructors[index].getParameterTypes();
			Object[] cArgs = new Object[cTypes.length];
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

}
