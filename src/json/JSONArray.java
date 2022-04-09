package json;

import java.util.Enumeration;
import java.util.Vector;

import Jch;

public class JSONArray extends AbstractJSON {

	private Vector vector;

	public JSONArray() {
		this.vector = new Vector();
	}

	public JSONArray(Vector vector) {
		this.vector = vector;
	}
	
	public Object get(int index) throws Exception {
		try {
			//if (JSON.parse_members)
			//	return vector.elementAt(index);
			//else {
				Object o = vector.elementAt(index);
				if (o instanceof String)
					vector.setElementAt(o = Jch.parseJSON((String) o), index);
				return o;
			//}
		} catch (Exception e) {
		}
		throw new Exception("JSON: No value at " + index);
	}
	
	public Object get(int index, Object def) {
		try {
			return get(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getString(int index) throws Exception {
		return get(index).toString();
	}
	
	public String getString(int index, String def) {
		try {
			return get(index).toString();
		} catch (Exception e) {
			return def;
		}
	}
	
	public JSONObject getObject(int index) throws Exception {
		try {
			return (JSONObject) get(index);
		} catch (ClassCastException e) {
			throw new Exception("JSON: Not object at " + index);
		}
	}
	
	public JSONObject getNullableObject(int index) {
		try {
			return getObject(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	public JSONArray getArray(int index) throws Exception {
		try {
			return (JSONArray) get(index);
		} catch (ClassCastException e) {
			throw new Exception("JSON: Not array at " + index);
		}
	}
	
	public JSONArray getNullableArray(int index) {
		try {
			return getArray(index);
		} catch (Exception e) {
			return null;
		}
	}
	
	public int getInt(int index) throws Exception {
		return (int) Jch.getLong(get(index)).longValue();
	}
	
	public int getInt(int index, int def) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(int index) throws Exception {
		return Jch.getLong(get(index)).longValue();
	}

	public long getLong(int index, long def) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(int index) throws Exception {
		return Jch.getDouble(get(index)).doubleValue();
	}
	
	public double getDouble(int index, double def) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(int index) throws Exception {
		Object o = get(index);
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if(s.equals("true")) return true;
			if(s.equals("false")) return false;
		}
		throw new Exception("JSON: Not boolean: " + o + " (" + index + ")");
	}

	public boolean getBoolean(int index, boolean def) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return def;
		}
	}
	
	public void clear() {
		vector.removeAllElements();
	}
	
	public int size() {
		return vector.size();
	}

	public Enumeration elements() {
		return new Enumeration() {
			int i = 0;
			public boolean hasMoreElements() {
				return i < vector.size();
			}
			public Object nextElement() {
				try {
					return get(i++);
				} catch (Exception e) {
					return null;
				}
			}
		};
	}
}
