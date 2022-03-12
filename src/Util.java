import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordStore;

public class Util {
	public static String platform = System.getProperty("microedition.platform");
	private static String cookie;
	
	static {
		try {
			RecordStore r = RecordStore.openRecordStore("jch_cookie", false);
			byte[] b = r.getRecord(1);
			r.closeRecordStore();
			cookie = new String(b);
		} catch (Exception e) {
		}
	}

	public static String replace(String str, String from, String to) {
		int j = str.indexOf(from);
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = str.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static String replaceIgnoreCase(String str, String from, String to) {
		String low = str.toLowerCase();
		int j = low.indexOf(from = from.toLowerCase());
		if (j == -1)
			return str;
		final StringBuffer sb = new StringBuffer();
		int k = 0;
		for (int i = from.length(); j != -1; j = low.indexOf(from, k)) {
			sb.append(str.substring(k, j)).append(to);
			k = j + i;
		}
		sb.append(str.substring(k, str.length()));
		return sb.toString();
	}

	public static String cut(String str, String find) {
		return replace(str, find, "");
	}

	public static String cut(String str, String[] cl) {
		for (int i = 0; i < cl.length; i++) {
			str = cut(str, cl[i]);
		}
		return str;
	}

	public static boolean contains(String str, String find) {
		return str.indexOf(find) != -1;
	}

	public static boolean containsIgnoreCase(String str, String find) {
		return str.toLowerCase().indexOf(find.toLowerCase()) != -1;
	}

	public static boolean safeEquals(String str, String str2) {
		return str.length() == str2.length() && str.indexOf(str2) == 0;
	}

	public static boolean equalsIgnoreCase(String str, String str2) {
		return safeEquals(str.toLowerCase(), str2.toLowerCase());
	}

	public static String cutIgnoreCase(String str, String cl) {
		return replaceIgnoreCase(str, cl, "");
	}

	public static boolean startsWithIgnoreCase(String str, String need) {
		return equalsIgnoreCase(str.substring(0, need.length()), need);
	}
	
	public static String[] split(String str, char d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + 1);
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}
	
	public static String[] split(String str, String d) {
		int i = str.indexOf(d);
		if(i == -1)
			return new String[] {str};
		Vector v = new Vector();
		v.addElement(str.substring(0, i));
		while(i != -1) {
			str = str.substring(i + d.length());
			if((i = str.indexOf(d)) != -1)
				v.addElement(str.substring(0, i));
			i = str.indexOf(d);
		}
		v.addElement(str);
		String[] r = new String[v.size()];
		v.copyInto(r);
		return r;
	}

	public static int count(String in, char t) {
		int r = 0;
		char[] c = in.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == t) r++;
		}
		return r;
	}

	public static int count(String str, String f) {
		int i = str.indexOf(f);
		int c = 0;
		while (i != -1) {
			str = str.substring(i + f.length());
			c++;
			i = str.indexOf(f);
		}
		return c;
	}

	public static String[] splitSingle(String str, String d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + d.length()) };
	}

	public static String[] splitSingle(String str, char d) {
		int i = str.indexOf(d);
		return new String[] { str.substring(0, i), str.substring(i + 1) };
	}

	public static String encodeUrl(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if ((65 <= ch) && (ch <= 90)) {
				sbuf.append((char) ch);
			} else if ((97 <= ch) && (ch <= 122)) {
				sbuf.append((char) ch);
			} else if ((48 <= ch) && (ch <= 57)) {
				sbuf.append((char) ch);
			} else if (ch == 32) {
				sbuf.append("%20");
			} else if ((ch == 45) || (ch == 95) || (ch == 46) || (ch == 33) || (ch == 126) || (ch == 42) || (ch == 39)
					|| (ch == 40) || (ch == 41) || (ch == 58) || (ch == 47)) {
				sbuf.append((char) ch);
			} else if (ch <= 127) {
				sbuf.append(hex(ch));
			} else if (ch <= 2047) {
				sbuf.append(hex(0xC0 | ch >> 6));
				sbuf.append(hex(0x80 | ch & 0x3F));
			} else {
				sbuf.append(hex(0xE0 | ch >> 12));
				sbuf.append(hex(0x80 | ch >> 6 & 0x3F));
				sbuf.append(hex(0x80 | ch & 0x3F));
			}
		}
		return Util.replace(Util.replace(sbuf.toString(), "/", "%2F"), ":", "%3A");
	}

	private static String hex(int ch) {
		String s = Integer.toHexString(ch);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}

	public static byte[] get(String url) throws IOException {
		System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) open(url);

		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			if(cookie != null) {
				hc.setRequestProperty("Cookie", cookie);
			}
			//hc.setRequestProperty("Accept-Encoding", "identity");
			int r = hc.getResponseCode();
			if(r >= 400) throw new IOException(r + " " + hc.getResponseMessage());
			int redirects = 0;
			while (r == 301 || r == 302) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String tmp = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + tmp.substring(0, tmp.indexOf("/"));
					redir = host + redir;
				}
				hc.close();
				hc = (HttpConnection) open(redir);
				hc.setRequestMethod("GET");
				if(cookie != null) {
					hc.setRequestProperty("Cookie", cookie);
				}
				if(redirects++ > 3) {
					throw new IOException("Too many redirects!");
				}
			}
			if (hc.getHeaderField("Set-Cookie") != null && !url.endsWith(".jpg")) {
				for (int i = 0;; i++) {
					String k = hc.getHeaderFieldKey(i);
					if (k == null)
						break;
					String v = hc.getHeaderField(i);
					if(k.equalsIgnoreCase("set-cookie")) {
						if(v.indexOf("code_auth=") != -1) {
							String[] f = Util.split(v, ';');
							for(int j = 0; j < f.length; j++) {
								if(f[i].indexOf("code_auth=") != -1) {
									String s = f[i];
									if(s.startsWith(" ")) s = s.substring(1);
									cookie = s;
									break;
								}
							}
						}
					}
				}
			}
			is = hc.openInputStream();
			o = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
               o.write(buf, 0, len);
			}
			return o.toByteArray();
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		} finally {
			if (is != null)
				is.close();
			if (hc != null)
				hc.close();
			if (o != null)
				o.close();
		}
	}

	public static String getString(String url) throws IOException {
		byte[] b = get(url);
		try {
			return new String(b, "UTF-8");
		} catch (Exception e) {
			return new String(b);
		}
	}
	
	public static ContentConnection open(String url) throws IOException {
		try {
			ContentConnection con = (ContentConnection) Connector.open(url, Connector.READ);
			if (con instanceof HttpConnection)
				((HttpConnection) con).setRequestProperty("User-Agent", "JCh/" + JChMIDlet.version() + " (" + platform + ")");
			return con;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
			//throw new IOException(Util.cut(Util.cut(e.toString(), "Exception"), "java.io.") + " " + url);
		}
	}

	public static String htmlText(String str) {
		char[] chars = str.toCharArray();
		str = null;
		try {
			int l = chars.length;
			StringBuffer sb = new StringBuffer();
			int i = 0;
			loop: {
				while (i < l) {
					char c = chars[i];
					switch (c) {
					case '&': {
						next: {
							replaced: {
								if(l < i + 1) {
									sb.append(c);
									break loop;
								}
								try {
									switch (chars[i + 1]) {
									case 'a':
										if(chars[i + 2] == 'm' && chars[i + 3] == 'p' && chars[i + 4] == ';') {
											i += 5;
											sb.append('&');
											break replaced;
										}
										break next;
									case 'l':
										if(chars[i + 2] == 't' && chars[i + 3] == ';') {
											i += 4;
											sb.append('<');
											break replaced;
										}
										break next;
									case 'g':
										if(chars[i + 2] == 't' && chars[i + 3] == ';') {
											i += 4;
											sb.append('>');
											break replaced;
										}
										break next;
									case 'q':
										if(chars[i + 2] == 'u' && chars[i + 3] == 'o' && chars[i + 4] == 't' && chars[i + 5] == ';') {
											i += 6;
											sb.append('\"');
											break replaced;
										}
										break next;
									case '#':
										try {
											if(chars[i + 4] == ';') {
												String s = chars[i + 2] + "" + chars[i + 3];
												sb.append((char)Integer.parseInt(s));
												i += 5;
												break replaced;
											}
										} catch (Exception e) {
										}
										break next;
									default:
										break next;
									}
								} catch (Exception e) {
									break next;
								}
							}
							break;
						}
						sb.append(c);
						i++;
						break;
					}
					case '<' : {
						if(l < i + 1) {
							sb.append(c);
							break loop;
						}
						try {
							if(chars[i + 1] == 'b' && chars[i + 2] == 'r' && chars[i + 3] == '>') {
								i += 4;
								sb.append("\n");
								break;
							}
						} catch (Exception e) {
						}
						sb.append(c);
						i++;
						break;
					}
					default:
						sb.append(c);
						i++;
					}
				}
			}
			str = sb.toString();
			sb = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

}
