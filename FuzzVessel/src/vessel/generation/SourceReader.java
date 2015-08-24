package vessel.generation;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

import vessel.utils.VesselUtils;

/**
 * Read the android sources to get some data.
 */
/*
 * TODO: A lot. Fetch me some database connection here.
 */
public class SourceReader {

	public static final String dbPath = "vessel_objects.db";

	public static void test() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stmt = c.createStatement();
			String sql = "SELECT * FROM Classes";
			ResultSet rs = stmt.executeQuery(sql);
			// Array a = rs.getArray("name");
			while (rs.next())
				System.out.println(rs.getString("name"));
			stmt.close();
			c.close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	/*
	 * TODO: This array is a placeholder. Normally, some array of possible
	 * parcelable types should be generated from the Android sources.
	 */
	public Class<?>[] readAllParcelableTypes() {
		Class<?>[] c = { boolean.class, byte.class, char.class, double.class,
				float.class, int.class, long.class, short.class,
				java.lang.String.class };
		return c;
	}

	/**
	 * Connect to the database and check if the class is an instance of some
	 * Android class. "implementing" a parent class or a parent interface. On
	 * fail, returns false.
	 * 
	 * At present, always returns true since the database is not complete yet.
	 */
	public boolean lookupSourceIsInstance(String child, String parent) {
		if (parent.equals(child))
			return true;
		Connection c = null;
		Statement stmt = null;
		String foundParent = child;
		try {
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stmt = c.createStatement();
			while (!foundParent.equals(parent)) {
				child = foundParent;
				String sql = "SELECT * FROM Extends WHERE child_class_name = '"
						+ child + "';";
				ResultSet rs = stmt.executeQuery(sql);
				if (!rs.next())
					//return false;
					return true;
				foundParent = rs.getString("parent_name");
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return false;
		return true;
	}

	/**
	 * Is the object of specified class parcelable? If not, we are screwed.
	 * 
	 * @param className
	 * @return
	 */
	public boolean lookupSourceIsParcelable(String className) {
		return lookupSourceIsInstance(className, "android.os.Parcelable");
	}

	/**
	 * Connect to database and retrieve information about some class'
	 * constructor param classes represented as an array of names. Currently,
	 * random constructor is selected. On error, returns an empty array.
	 * 
	 * @param className
	 * @return
	 */
	public String[] readConstructorParams(String className) {
		Connection c = null;
		Statement stmt = null;
		Random rand = new Random();
		try {
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stmt = c.createStatement();
			String sql = "SELECT * FROM Constructors WHERE class_name = '"
					+ className + "';";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> list = new ArrayList<String>();
			while (rs.next())
				list.add(rs.getString("id"));
			String[] ids = new String[list.size()];
			list.toArray(ids);
			if (ids.length == 0)
				return new String[0];
			String selectedId = ids[rand.nextInt(ids.length)];
			sql = "SELECT * FROM Args WHERE constructor_id = " + selectedId
					+ ";";
			rs = stmt.executeQuery(sql);
			list.clear();
			while (rs.next())
				list.add(rs.getString("class_name"));
			stmt.close();
			c.close();
			String[] result = new String[list.size()];
			list.toArray(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/**
	 * Connect to database and obtain a string array of (name-type name) pairs
	 * representing the class' fields. Thus, the array size should always divide
	 * by 2. Returns an empty array on failure.
	 * 
	 * @param className
	 * @return
	 */
	public String[] getParcelableFields(String className) {
		Connection c = null;
		Statement stmt = null;
		try {
			c = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stmt = c.createStatement();
			String sql = "SELECT * FROM Fields WHERE declaring_class_name = '"
					+ className + "';";
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> list = new ArrayList<String>();
			while (rs.next()) {
				list.add(rs.getString("name"));
				list.add(rs.getString("type_class_name"));
			}
			stmt.close();
			c.close();
			String[] result = new String[list.size()];
			list.toArray(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[0];
	}

}
