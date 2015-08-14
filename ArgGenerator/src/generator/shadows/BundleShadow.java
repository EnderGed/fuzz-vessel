package generator.shadows;

import java.util.HashMap;

/*
 * A class containing some directions to create an Android Bundle.
 */
public class BundleShadow {

	public HashMap<String, Object> map;

	public BundleShadow() {
		map = new HashMap<>();
	}
	
	public void addElement(String key, Object content){
		map.put(key, content);
	}
	
	public void removeElement(String key){
		map.remove(key);
	}
	
	public void clear(){
		map.clear();
	}
	
	public Object getElement(String key){
		return map.get(key);
	}
	
}