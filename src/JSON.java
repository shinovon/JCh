import java.util.Hashtable;
import java.util.Vector;

public class JSON {

	public static Object parseJSON(String var0) throws Exception {
		if (var0.length() < 2) {
			return var0;
		} else {
			char var2 = var0.charAt(0);
			char var3 = var0.charAt(var0.length() - 1);
			if (var2 == 123 && var3 != 125 || var2 == 91 && var3 != 93 || var2 == 34 && var3 != 34) {
				throw new Exception("Wrong JSON delimiters " + var0);
			} else {
				int var4;
				if (var2 == 34) {
					var0 = var0.substring(1, var0.length() - 1);
					var0 = StringUtils.replace(var0, "\\u003c", "<");
					var0 = StringUtils.replace(var0, "\\u003e", ">");
					var0 = StringUtils.replace(var0, "<br>", "\n");
					var0 = StringUtils.replace(var0, "\\n", "\n");
					var0 = StringUtils.replace(var0, "&amp;", "&");
					var0 = StringUtils.replace(var0, "&lt;", "<");
					var0 = StringUtils.replace(var0, "&gt;", ">");
					var0 = StringUtils.replace(var0, "&quot;", "\"");
					var0 = StringUtils.replace(var0, "&#39;", "\'");

					for (var4 = 0; var4 < var0.length(); ++var4) {
						if (var0.charAt(var4) == 92) {
							var0 = var0.substring(0, var4) + var0.substring(var4 + 1);
						}
					}

					return var0;
				} else if (var2 != 123 && var2 != 91) {
					return var0;
				} else {
					var4 = 0;
					boolean var5 = var2 == 123;
					int var6 = 1;
					int var7 = var0.length() - 1;
					int var8 = var5 ? 58 : 44;
					boolean var10 = false;
					String var11 = null;
					Vector var12 = null;
					Hashtable var13 = null;
					if (var5) {
						var13 = new Hashtable();
					} else {
						var12 = new Vector();
					}

					int var14;
					for (; var6 < var7; var6 = var14 + 1) {
						while (var6 < var7 - 1 && var0.charAt(var6) == 32) {
							++var6;
						}

						var14 = var6;

						boolean var9;
						for (var9 = false; var14 < var7 && (var9 || var4 > 0 || var0.charAt(var14) != var8); ++var14) {
							char var15 = var0.charAt(var14);
							if (!var10) {
								if (var15 == 92) {
									var10 = true;
								}

								if (var15 == 34) {
									var9 = !var9;
								}
							} else {
								var10 = false;
							}

							if (!var9) {
								if (var15 != 123 && var15 != 91) {
									if (var15 == 125 || var15 == 93) {
										--var4;
									}
								} else {
									++var4;
								}
							}
						}

						if (var9 || var4 > 0) {
							throw new Exception("Wrong JSON content " + var0.substring(var6, var14));
						}

						if (var5 && var11 == null) {
							var11 = var0.substring(var6 + 1, var14 - 1);
							var8 = 44;
						} else {
							Object var16 = parseJSON(var0.substring(var6, var14));
							if (var5) {
								var13.put(var11, var16);
								var11 = null;
								var8 = 58;
							} else if (var14 > var6) {
								var12.addElement(var16);
							}
						}
					}

					if (var5) {
						return var13;
					} else {
						return var12;
					}
				}
			}
		}
	}

	public static boolean isArray(Object obj) {
		return obj instanceof Vector;
	}

	public static boolean isObject(Object obj) {
		return obj instanceof Hashtable;
	}

	public static boolean isValue(Object obj) {
		return obj instanceof String;
	}

	public static int optInt(Object obj, String key) {
		Object get = optObject(obj, key);
		if (!(get instanceof String))
			return 0;
		try {
			return Integer.parseInt((String) get);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static String optValue(Object obj, String key) {
		Object get = optObject(obj, key);
		if (!(get instanceof String))
			return null;
		return (String) get;
	}

	public static boolean has(Object obj, String key) {
		if (isValue(obj))
			return false;
		if (isArray(obj))
			return false;
		if (optObject(obj, key) != null)
			return true;
		return false;
	}

	public static Object optObject(Object obj, String key) {
		if (isValue(obj))
			return null;
		if (isArray(obj))
			return ((Vector) obj).elementAt(Integer.parseInt(key));
		if (!((Hashtable) obj).containsKey(key))
			return null;
		return ((Hashtable) obj).get(key);
	}

}
