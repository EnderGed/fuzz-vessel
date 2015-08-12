package vessel.generator.shadows;

import java.util.ArrayList;

/*
 * I have a class; I don't know what it is; I don't know what to do with it; Let the Ghost handle this.
 * (This is going to be used for Android classes.)
 */
public class ParcelableShadow {
	private String name;
	private ArrayList<Object> data;

	public ParcelableShadow(String name) {
		this.name = name;
		data = new ArrayList<Object>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void insertDatum(Object datum) {
		data.add(datum);
	}

	public void removeDatum(Object datum) {
		try {
			data.remove(datum);
		} catch (Exception e) {
			System.out.println("No such datum.");
		}
	}

	public Object getDatum(int index) {
		try {
			return data.get(index);
		} catch (Exception e) {
			return null;
		}
	}

}