package vessel.utils;

public class VesselUtils {
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
	
	public static Object[] joinArrays(Object[] a1, Object[] a2){
		int middle = a1.length;
		Object[] result = new Object[middle + a2.length];
		for(int i=0; i<middle; ++i)
			result[i] = a1[i];
		for(int j=middle; j<middle+a2.length; ++j)
			result[j] = a2[j-middle];
		return result;
	}
}
