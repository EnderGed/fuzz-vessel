package generator.shadows;

import java.util.HashMap;
import java.util.Set;

/*
 * A class containing some directions to create an Android Bundle.
 */
public class BundleShadow {

	private HashMap<String, Object> map;

	public BundleShadow() {
		map = new HashMap<>();
	}

	public void addElement(String key, Object content) {
		map.put(key, content);
	}

	public void removeElement(String key) {
		map.remove(key);
	}

	public Object getElement(String key) {
		return map.get(key);
	}

	public Set<String> getKeys() {
		return map.keySet();
	}

	public void clear() {
		map.clear();
	}

}