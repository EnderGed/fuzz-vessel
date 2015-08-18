package vessel.generation;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import generator.shadows.*;

/**
 * The Weaver knows the Android sources, but cannot create actual objects, so it
 * weaves their shadows. Also, it weaves file descriptor shadows. Regular, known
 * java objects are represented by their names.
 */
public class ShadowWeaver {

	private int bundleSize = 64;
	private SourceReader reader;

	public ShadowWeaver() {
		reader = new SourceReader();
	}

	/**
	 * Weave an array of shadows.
	 * 
	 * @param classNames
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InvalidClassException
	 */
	public Object[] weaveArray(String[] classNames)
			throws ClassNotFoundException, InvalidClassException {
		Object[] weaved = new Object[classNames.length];
		for (int i = 0; i < classNames.length; ++i)
			weaved[i] = weave(classNames[i]);
		return weaved;
	}

	/**
	 * Handle all possible cases of weaving. If needed, weave an appropriate
	 * object. If not, return the String containing its name.
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InvalidClassException
	 */
	public Object weave(String className) throws ClassNotFoundException,
			InvalidClassException {
		Matcher matcher = Pattern.compile("(.*)<(.*)>").matcher(className);
		if (matcher.matches())
			return weaveCollection(matcher.group(1),
					matcher.group(2).split(", "));
		if (reader.isAndroid(className)) {
			if (className.equals("android.os.Bundle"))
				return weaveBundle();
			if (className.equals("android.util.SparseBooleanArray"))
				return weaveSparseBooleanArray();
			return weaveParcelable(className);
		}
		if (className.equals("java.io.FileDescriptor"))
			return weaveDescriptor();
		return className;
	}

	/**
	 * Creates a shadow before inserting it into a generator. The weaver checks
	 * which fields and constructor arguments are Android. For these, it weaves
	 * more shadows. For all other values, it writes strings with their class
	 * names which will be later replaced by actual objects by the Generator.
	 * 
	 * Special cases are: List, Map and Sparse Array. In this case, an instance
	 * of such object is created with a single type name or ParcelableShadow of
	 * its type inside so that the Generator will know how to handle its
	 * creation.
	 * 
	 * TODO: Handle bundles and sparse boolean arrays.
	 * 
	 * @param className
	 * @return An shadow with dummy field values.
	 */
	public ParcelableShadow weaveParcelable(String className)
			throws ClassNotFoundException, InvalidClassException {
		ParcelableShadow ps = new ParcelableShadow(className);
		String[] parcelableFields = reader.getParcelableFields(className);
		if (parcelableFields.length % 2 == 0) {
			System.err.println("Data format error.");
			return null;
		}
		for (int i = 0; i < parcelableFields.length; i += 2)
			ps.insertDatum(parcelableFields[i], weave(parcelableFields[i + 1]));
		String[] paramNames = reader.readConstructorParams(className);
		Object[] params = new Object[paramNames.length];
		for (int i = 0; i < paramNames.length; ++i)
			params[i] = weave(paramNames[i]);
		ps.setConstructorArgs(params);
		return ps;
	}

	/**
	 * File descriptor shadows are kind of a special case since they don't
	 * actually correspond to Android classes.
	 */
	public FileDescriptorShadow weaveDescriptor() {
		return new FileDescriptorShadow("java.lang.String");
	}

	/**
	 * Creates an empty sparse boolean array shadow.
	 * 
	 * @return
	 */
	public SparseBooleanArrayShadow weaveSparseBooleanArray() {
		return new SparseBooleanArrayShadow();
	}

	/**
	 * Creates a bundle shadow and populates it with random trash. This is not
	 * handled only by the generator as it does not know parcelable Android
	 * classes.
	 * 
	 * @return
	 */
	public BundleShadow weaveBundle() {
		Random rand = new Random();
		BundleShadow bs = new BundleShadow();
		Class<?>[] parcelableTypes = reader.readAllParcelableTypes();
		for (int i = 0; i < bundleSize; ++i)
			try {
				bs.addElement(i + "", weave(parcelableTypes[rand
						.nextInt(parcelableTypes.length)].getName()));
			} catch (Exception e) {
				System.err.println("Bundle creation error: " + e.getMessage()
						+ "; inserting int.");
				bs.addElement(i + "", "int");
			}
		return bs;
	}

	/**
	 * Creates a collection containing a single raw element from the given
	 * collection class and content types names (one content type for list and
	 * sparse array, two for hash map).
	 * 
	 * @param collType
	 * @param contentTypes
	 * @return
	 * @throws InvalidClassException
	 *             If something's wrong (bad class or inadequate content types
	 *             length).
	 * @throws ClassNotFoundException
	 *             If getting content class fails.
	 */
	private Object weaveCollection(String collName, String[] contentTypes)
			throws InvalidClassException, ClassNotFoundException {
		Class<?> collType = reader.isAndroid(collName) ? weaveClass(collName)
				: ClassLoader.getSystemClassLoader().loadClass(collName);
		if (collType == SparseArrayShadow.class && contentTypes.length == 1) {
			Object content = weave(contentTypes[0]);
			SparseArrayShadow<Object> result = new SparseArrayShadow<>();
			result.append(0, content);
			return result;
		} else if (List.class.isAssignableFrom(collType)
				&& contentTypes.length == 1) {
			Object content = weave(contentTypes[0]);
			ArrayList<Object> result = new ArrayList<>();
			result.add(content);
			return result;
		} else if (Map.class.isAssignableFrom(collType)
				&& contentTypes.length == 2) {
			Object key = weave(contentTypes[0]);
			Object content = weave(contentTypes[1]);
			HashMap<Object, Object> result = new HashMap<>();
			result.put(key, content);
			return result;
		}
		throw new InvalidClassException(
				"Collection must be either a Sparse Array, a List or a Map.");
	}

	/**
	 * A small auxiliary method for getting a right shadow class from the name.
	 * 
	 * @param name
	 * @return
	 */
	private Class<?> weaveClass(String name) {
		switch (name) {
		case "android.os.Bundle":
			return BundleShadow.class;
		case "android.util.SparseArray":
			return SparseArrayShadow.class;
		case "android.util.SparseBooleanArray":
			return SparseBooleanArrayShadow.class;
		default:
			return ParcelableShadow.class;
		}
	}

	public int getBundleSize() {
		return bundleSize;
	}

	public void setBundleSize(int bundleSize) {
		this.bundleSize = bundleSize;
	}

}