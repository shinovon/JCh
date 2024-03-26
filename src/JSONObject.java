/*
Copyright (c) 2023 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


import java.util.Enumeration;
import java.util.Hashtable;

public class JSONObject {

	protected Hashtable table;

	public JSONObject() {
		table = new Hashtable();
	}

	/**
	 * @deprecated Doesn't adapt nested elements
	 */
	public JSONObject(Hashtable table) {
		this.table = table;
	}
	
	public Object get(String name) {
		try {
			if (has(name)) {
				Object o = table.get(name);
				if (o instanceof String[])
					table.put(name, o = Jch.parseJSON(((String[]) o)[0]));
				if (o == Jch.json_null)
					return null;
				return o;
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("JSON: No value for name: " + name);
	}
	
	// unused methods should be removed by proguard shrinking
	
	public Object get(String name, Object def) {
		if (!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(String name) {
		return get(name, null);
	}
	
	public String getString(String name) {
		Object o = get(name);
		if (o == null || o instanceof String)
			return (String) o;
		return String.valueOf(o);
	}
	
	public String getString(String name, String def) {
		try {
			Object o = get(name, def);
			if (o == null || o instanceof String)
				return (String) o;
			return String.valueOf(o);
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getNullableString(String name) {
		return getString(name, null);
	}
	
	public JSONObject getObject(String name) {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not object: " + name);
		}
	}
	public JSONObject getObject(String name, JSONObject def) {
		if (has(name)) {
			try {
				return (JSONObject) get(name);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	public JSONObject getNullableObject(String name) {
		return getObject(name, null);
	}
	
	public JSONArray getArray(String name) {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new RuntimeException("JSON: Not array: " + name);
		}
	}
	
	public JSONArray getArray(String name, JSONArray def) {
		if (has(name)) {
			try {
				return (JSONArray) get(name);
			} catch (Exception e) {
			}
		}
		return def;
	}
	
	
	public JSONArray getNullableArray(String name) {
		return getArray(name, null);
	}
	
	public int getInt(String name) {
		return Jch.getInt(get(name));
	}
	
	public int getInt(String name, int def) {
		if (!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) {
		return Jch.getLong(get(name));
	}

	public long getLong(String name, long def) {
		if (!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) {
		Object o = get(name);
		if (o == Jch.TRUE) return true;
		if (o == Jch.FALSE) return false;
		if (o instanceof Boolean) return ((Boolean) o).booleanValue();
		if (o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if (s.equals("true")) return true;
			if (s.equals("false")) return false;
		}
		throw new RuntimeException("JSON: Not boolean: " + o);
	}

	public boolean getBoolean(String name, boolean def) {
		if (!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean isNull(String name) {
		if (!has(name))
			throw new RuntimeException("JSON: No value for name: " + name);
		return table.get(name) == Jch.json_null;
	}
	
	public void put(String name, Object json) {
		table.put(name, json);
	}

	public void put(String name, int i) {
		table.put(name, new Integer(i));
	}

	public void put(String name, long l) {
		table.put(name, new Long(l));
	}

	public void put(String name, boolean b) {
		table.put(name, new Boolean(b));
	}
	
	public boolean hasValue(Object object) {
		return table.contains(object);
	}
	
	// hasKey
	public boolean has(String name) {
		return table.containsKey(name);
	}
	
	public void clear() {
		table.clear();
	}
	
	public void remove(String name) {
		table.remove(name);
	}
	
	public int size() {
		return table.size();
	}
	
	public boolean isEmpty() {
		return table.isEmpty();
	}
	
	public String toString() {
		return build();
	}
	
	public boolean equals(Object obj) {
		return this == obj || super.equals(obj) ;
	}
	
	public String build() {
		if (size() == 0)
			return "{}";
		StringBuffer s = new StringBuffer("{");
		Enumeration keys = table.keys();
		while (true) {
			String k = (String) keys.nextElement();
			s.append("\"").append(k).append("\":");
			Object v = table.get(k);
			if (v instanceof JSONObject) {
				s.append(((JSONObject) v).build());
			} else if (v instanceof JSONArray) {
				s.append(((JSONArray) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(Jch.escape_utf8((String) v)).append("\"");
			} else if (v == Jch.json_null) {
				s.append((String) null);
			} else {
				s.append(v);
			}
			if (!keys.hasMoreElements()) {
				break;
			}
			s.append(",");
		}
		s.append("}");
		return s.toString();
	}

	public Enumeration keys() {
		return table.keys();
	}

	public JSONArray keysAsArray() {
		JSONArray array = new JSONArray(table.size());
		Enumeration keys = table.keys();
		while (keys.hasMoreElements()) {
			array.addElement(keys.nextElement());
		}
		return array;
	}
	
	/**
	 * @deprecated Use {@link JSONObject#toTable()} instead
	 */
	public Hashtable getTable() {
		return table;
	}

	public Hashtable toTable() {
		Hashtable copy = new Hashtable(table.size());
		Enumeration keys = table.keys();
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			Object v = table.get(k);
			if (v instanceof String[])
				table.put(k, v = Jch.parseJSON(((String[]) v)[0]));
			if (v instanceof JSONObject) {
				v = ((JSONObject) v).toTable();
			} else if (v instanceof JSONArray) {
				v = ((JSONArray) v).toVector();
			}
			copy.put(k, v);
		}
		return copy;
	}
	
	void _put(String name, Object obj) {
		table.put(name, obj);
	}
	
	// TODO: Enumeration elements()
	// TODO: String keyOf(Object)

}
