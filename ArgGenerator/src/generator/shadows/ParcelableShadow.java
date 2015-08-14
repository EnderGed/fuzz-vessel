package generator.shadows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/*
 * I have a class; I don't know what it is; I don't know what to do with it; Let the Ghost handle this.
 * (This is going to be used for Android classes.)
 */
public class ParcelableShadow implements Serializable {

	private static final long serialVersionUID = -3469995294980999926L; //autogenerated
	private String name;
	private HashMap<String, Object> fieldData;
	private Object[] constructorArgs; //The arguments of a selected constructor; the Ghost will know what to do with them.

	public ParcelableShadow(String name) {
		this.name = name;
		constructorArgs = new Object[0]; //For a default constructor.
		fieldData = new HashMap<String, Object>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void insertDatum(String name, Object datum) {
		fieldData.put(name, datum);
	}

	public void removeDatum(String name) {
		try {
			fieldData.remove(name);
		} catch (Exception e) {
			System.out.println("No such datum.");
		}
	}

	public Object getDatum(String name) {
		try {
			return fieldData.get(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public Set<String> getKeys(){
		return fieldData.keySet();
	}
	
	public Object[] getConstructorArgs(){
		return constructorArgs;
	}
	
	public void setConstructorArgs(Object[] args){
		constructorArgs = args;
	}

}