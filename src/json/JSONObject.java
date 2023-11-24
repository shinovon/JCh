package json;

import java.util.Enumeration;
import java.util.Hashtable;

import Jch;

public class JSONObject extends AbstractJSON {

	private Hashtable table;

	public JSONObject() {
		this.table = new Hashtable();
	}

	public JSONObject(Hashtable table) {
		this.table = table;
	}
	
	public boolean has(String name) {
		return table.containsKey(name);
	}
	
	public Object get(String name) throws Exception {
		try {
			if (has(name)) {
				//if (JSON.parse_members) {
				//	return table.get(name);
				//} else {
					Object o = table.get(name);
					if (o instanceof String)
						table.put(name, o = Jch.parseJSON((String) o));
					return o;
				//}
			}
		} catch (Exception e) {
			if(e.getMessage() != null && e.getMessage().startsWith("JSON:")) throw e;
		}
		throw new Exception("JSON: No value for name: " + name);
	}
	
	public Object get(String name, Object def) {
		if(!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(String name) {
		return get(name, null);
	}
	
	public String getString(String name) throws Exception {
		return get(name).toString();
	}
	
	public String getString(String name, String def) {
		try {
			Object o = get(name, def);
			if(o == null || o instanceof String) {
				return (String) o;
			}
			return o.toString();
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getNullableString(String name) {
		return getString(name, null);
	}
	
	public JSONObject getObject(String name) throws Exception {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new Exception("JSON: Not object: " + name);
		}
	}
	
	public JSONObject getNullableObject(String name) {
		if(!has(name)) return null;
		try {
			return getObject(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public JSONArray getArray(String name) throws Exception {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new Exception("JSON: Not array: " + name);
		}
	}
	
	public JSONArray getNullableArray(String name) {
		if(!has(name)) return null;
		try {
			return getArray(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public int getInt(String name) throws Exception {
		return (int) Jch.getLong(get(name)).longValue();
	}
	
	public int getInt(String name, int def) {
		if(!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) throws Exception {
		return Jch.getLong(get(name)).longValue();
	}

	public long getLong(String name, long def) {
		if(!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) throws Exception {
		Object o = get(name);
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if(s.equals("true")) return true;
			if(s.equals("false")) return false;
		}
		throw new Exception("JSON: Not boolean: " + o);
	}

	public boolean getBoolean(String name, boolean def) {
		if(!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}

	public void put(String name, String s) {
		table.put(name, "\"".concat(s).concat("\""));
	}
	
	public void put(String name, Object obj) throws Exception {
		table.put(name, Jch.getJSON(obj));
	}
	
	public void clear() {
		table.clear();
	}
	
	public int size() {
		return table.size();
	}

	public String build() {
		int l = size();
		if (l == 0)
			return "{}";
		String s = "{";
		Enumeration elements = table.keys();
		while (true) {
			String k = elements.nextElement().toString();
			s += "\"" + k + "\":";
			Object v = null;
			try {
				v = table.get(k);
				if(v instanceof String) {
					v = Jch.parseJSON((String) v);
				}
			} catch (Exception e) {
			}
			if (v instanceof JSONObject) {
				s += ((JSONObject) v).build();
			} else if (v instanceof JSONArray) {
				s += "[]"; // edited
			} else if (v instanceof String) {
				s += "\"" + Jch.escape_utf8((String) v) + "\"";
			} else s += v.toString();
			if(!elements.hasMoreElements()) {
				return s + "}";
			}
			s += ",";
		}
	}

	public Enumeration keys() {
		return table.keys();
	}

}
