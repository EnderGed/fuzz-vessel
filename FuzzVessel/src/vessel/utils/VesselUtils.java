package vessel.utils;

/**
 * Bunch of static methods and values shared between various Vessel classes.
 * 
 */
public class VesselUtils {

	public static final String DISCONNECTED = "Ghost application has disconnected.";
	public static final String NOT_CONNECTED = "No ghost connected!";
	public static final String INCORRECT_COMMAND = "Incorrect command. Type \"help\" for hints.";

	/**
	 * Join an array of strings and return is as a single string.
	 * 
	 * @param separator
	 * @param array
	 * @return
	 */
	public static String joinStringArray(String separator, String[] array) {
		if (array.length == 0)
			return "";
		StringBuilder sb = new StringBuilder(array[0]);
		for (int i = 1; i < array.length; ++i) {
			sb.append(separator);
			sb.append(array[i]);
		}
		return sb.toString();
	}

	/**
	 * Join two arrays. Return their concatenation.
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static Object[] joinArrays(Object[] a1, Object[] a2) {
		int middle = a1.length;
		Object[] result = new Object[middle + a2.length];
		for (int i = 0; i < middle; ++i)
			result[i] = a1[i];
		for (int j = middle; j < middle + a2.length; ++j)
			result[j] = a2[j - middle];
		return result;
	}

	/**
	 * Is the given object inside the given array? (AKA contains)
	 * 
	 * @param array
	 * @param o
	 * @return
	 */
	public static boolean isInArray(Object[] array, Object o) {
		for (Object elem : array)
			if (elem.equals(o))
				return true;
		return false;
	}
}
