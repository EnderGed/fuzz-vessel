package vessel.generation;

//import java.sql.*;

import vessel.utils.VesselUtils;

/**
 * Read the android sources to get some data.
 */
/*
 * TODO: A lot. Fetch me some database connection here.
 */
public class SourceReader {

	/*public static void test() {
		String dbPath = "test.db"; //DON'T LEAVE IT LIKE THIS
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stmt = c.createStatement();
			String sql = "SELECT * FROM Classes";
			ResultSet rs = stmt.executeQuery(sql);
			//Array a = rs.getArray("name");
			while(rs.next())
				System.out.println(rs.getString("name"));
			stmt.close();
			c.close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}*/

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
		return new String[0];
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
		String[] primitives = { "boolean", "byte", "char", "double", "float",
				"int", "long", "short" };
		if (VesselUtils.isInArray(primitives, className))
			return false;
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
		return new String[0];
	}

}
