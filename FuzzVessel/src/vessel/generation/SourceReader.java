package vessel.generation;

/**
 * Read the android sources to get some data.
 */
/*
 * TODO: A lot. Fetch me some database connection here.
 */
public class SourceReader {

	/*
	 * TODO: This array is a placeholder. Normally, some array of possible
	 * parcelable types should be generated from the Android sources.
	 */
	public Class<?>[] readAllParcelableTypes() {
		Class<?>[] c = { String.class, Integer.class, Double.class };
		return c;
	}

	/*
	 * TODO: Check if the class is an instance of some Android class. This needs
	 * to be done by checking the source code.
	 */
	public boolean lookupSourceIsInstance(String className) {
		return true;
	}

	/**
	 * Is the object of specified class parcelable? If not, we are crewed.
	 * 
	 * @param className
	 * @return
	 */
	public boolean lookupSourceIsParcelable(String className) {
		return lookupSourceIsInstance("android.os.Parcelable");
	}

	/*
	 * TODO: Read class constructors and get its params. This should return
	 * names of param classes.
	 */
	public String[] readConstructorParams(String className) {
		return null;
	}

	/**
	 * Is the class even Android? Try to load the class and see what happens.
	 * This might not guarantee that the class is Android, but there's no better
	 * way at present.
	 * 
	 * @param className
	 * @return
	 */
	public boolean isAndroid(String className) {
		try {
			ClassLoader.getSystemClassLoader().loadClass(className);
			return false;
		} catch (ClassNotFoundException cnfe) {
			return true;
		}
	}
	
	/*
	 * TODO: Connect to some database to find this stuff. This should come as a
	 * "2xn array" of type names and field names.
	 */
	public String[] getParcelableFields(String className) {
		return null;
	}

}
