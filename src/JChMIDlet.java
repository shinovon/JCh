import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import me.regexp.RE;

public class JChMIDlet extends MIDlet implements CommandListener, ItemCommandListener {

	private static Command exitCmd = new Command("Выход", Command.EXIT, 0);
	private static Command backCmd = new Command("Назад", Command.BACK, 0);
	private static Command boardFieldCmd = new Command("Раздел", Command.OK, 0);
	private static Command boardCmd = new Command("Треды", Command.OK, 0);
	private static Command boardSearchCmd = new Command("Поиск", Command.OK, 0);
	private static Command openThreadCmd = new Command("Открыть тему", Command.ITEM, 0);
	private static Command fileImgItemOpenCmd = new Command("Открыть файл", Command.ITEM, 0);
	private static Command postLinkItemCmd = new Command("Открыть ссылку", Command.ITEM, 0);
	private static Object result;
	private static String version;
	private Form mainFrm;
	private Form boardFrm;
	private Form threadFrm;
	private TextField boardField;
	private TextField boardSearchField;
	
	private String currentBoard;

	private boolean running = true;
	private boolean started;
	
	private Hashtable files = new Hashtable();
	private Hashtable links = new Hashtable();
	
	private Object thumbLoadLock = new Object();
	private Vector thumbsToLoad = new Vector();
	private Thread thumbLoaderThread = new Thread() {
		public void run() {
			try {
				while(running) {
					synchronized(thumbLoadLock) {
						thumbLoadLock.wait();
					}
					int l;
					while((l = thumbsToLoad.size()) > 0) {
						int i = l - 1;
						Object[] o = (Object[]) thumbsToLoad.elementAt(i);
						thumbsToLoad.removeElementAt(i);
						String path = (String) o[0];
						ImageItem img = (ImageItem) o[1];
						img.setImage(getImg(path));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	static String version() {
		return version;
	}

	public JChMIDlet() {
		mainFrm = new Form("Jch - Главная");
		mainFrm.setCommandListener(this);
		mainFrm.addCommand(exitCmd);
		mainFrm.append(boardField = new TextField("", "", 8, TextField.ANY));
		boardField.setLabel("Раздел");
		boardField.addCommand(boardFieldCmd);
		boardField.setItemCommandListener(this);
		StringItem btn = new StringItem("", "", StringItem.BUTTON);
		btn.setText("Ввод");
		btn.setDefaultCommand(boardFieldCmd);
		btn.setItemCommandListener(this);
		mainFrm.append(btn);
	}

	protected void destroyApp(boolean b) {
		running = false;
		notifyDestroyed();
	}

	protected void pauseApp() {

	}

	protected void startApp() {
		if(started)
			return;
		if(version == null)
			version = getAppProperty("MIDlet-Version");
		started = true;
		Display.getDisplay(this).setCurrent(mainFrm);
		if(false)
		loadBoards();
		thumbLoaderThread.setPriority(2);
		thumbLoaderThread.start();
	}

	private void loadBoards() {
		new Thread() {
			public void run() {
				try {
					getResult("makaba/mobile.fcgi?task=get_boards&");
					if(!(result instanceof JSONObject))
						return;
					JSONObject result = (JSONObject)JChMIDlet.result;
					for(Enumeration en = result.keys(); en.hasMoreElements();) {
						String key = (String) en.nextElement();
						JSONArray category = (JSONArray) result.getArray(key);
						mainFrm.append("\n" + key + "\n");
						int l = category.size();
						for(int i = 0; i < l; i++) {
							JSONObject board = category.getObject(i);
							String id = board.getNullableString("id");
							String name = board.getNullableString("name");
							StringItem btn = new StringItem("", "", StringItem.BUTTON);
							btn.setText("/" + id + "/ " + name);
							btn.setDefaultCommand(boardCmd);
							btn.setItemCommandListener(JChMIDlet.this);
							btn.setLayout(Item.LAYOUT_EXPAND);
							mainFrm.append(btn);
							Thread.yield();
						}
						Thread.yield();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static void getResult(String string) throws Exception {
		System.out.println(string);
		result = null;
		result = Util.getString(prepareUrl(string));
		if(((String) result).charAt(0) == '{')
			result = JSON.getObject((String) result);
		else if(((String) result).charAt(0) == '[')
			result = JSON.getArray((String) result);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == exitCmd)
			destroyApp(false);
		else if(c == backCmd) {
			if(d == boardFrm) {
				Display.getDisplay(this).setCurrent(mainFrm);
			} else if(d == threadFrm) {
				Display.getDisplay(this).setCurrent(boardFrm);
				files.clear();
				links.clear();
				thumbsToLoad.removeAllElements();
				threadFrm = null;
			}
		}
	}

	public void commandAction(Command c, Item item) {
		if(c == boardFieldCmd) {
			board(boardField.getString());
		} else if(c == boardCmd || c.getLabel().startsWith("Треды")) {
			if(item instanceof TextField)
				board(((TextField)item).getString());
			else if(item instanceof StringItem)
				board(Util.split(((StringItem)item).getText(), '/')[1]);
		} else if(c == openThreadCmd && item.getLabel().startsWith("#")) {
			String s = item.getLabel().substring(1);
			//s = s.substring(0, s.indexOf(" "));
			final String id = s;
			System.out.println("Thread: " + id);
			new Thread() {
				public void run() {
					threadFrm = new Form("#" + id);
					threadFrm.addCommand(backCmd);
					threadFrm.setCommandListener(JChMIDlet.this);
					display(threadFrm);
					JSONObject j = null;
					try {
						getResult(currentBoard + "/res/" + id + ".json");
						if(!(result instanceof JSONObject))
							throw new RuntimeException("Result not object: " + result);
						j = (JSONObject) result;
					} catch (Exception e) {
						e.printStackTrace();
					}
					threadFrm.setTitle("/" + j.getString("Board", "?") + "/ - " + Util.htmlText(j.getString("title", "")));
					System.out.println(j);
					if(j == null || !(result instanceof JSONObject))
						return;
					JSONArray th = j.getNullableArray("threads");
					if(th == null)
						return;
					JSONArray posts = th.getObject(0).getArray("posts");
					try {
						int l = posts.size();
						for(int i = 0; i < l && i < 30; i++) {
							JSONObject post = posts.getObject(i);
							JSONArray files = post.getNullableArray("files");
							System.out.println(post);
							StringItem title = new StringItem(null, Util.htmlText(post.getString("name", "")) + " " + post.getString("date", "") + " #" + post.getString("num", ""));
							title.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL));
							title.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
							threadFrm.append(title);
							/*
							StringItem text = new StringItem(null, Util.text(post.getString("comment", "")));
							text.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
							text.setLayout(Item.LAYOUT_NEWLINE_AFTER);
							threadFrm.append(text);
							*/
							String x = post.getString("comment", "");
							RE r = new RE("(<a(.*?)>(.*?)</a>|<strong>(.*?)</strong>|<b>(.*?)</b>|<i>(.*?)</i>|<em>(.*?)</em>|<span(.*?)>(.*?)</span>)");
							RE r2 = new RE("(href=\"(.*?)\")");
							RE r3 = new RE("(class=\"(.*?)\")");
							//RE r3 = new RE("(data-num=\"(.*?)\")");
							int ti;
							int tl;
							for (ti = 0, tl = x.length(); ti < tl && r.match(x, ti); ti = r.getParenEnd(0)) {
								String o = x.substring(ti, r.getParenStart(0));
								if(o != null && !o.equals("")) {
									StringItem textitem = new StringItem(null, Util.htmlText(o));
									textitem.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
									textitem.setLayout(Item.LAYOUT_2);
									threadFrm.append(textitem);
								}
								String w = r.getParen(0);
								if(w.startsWith("<a")) {
									// ссылка
									String t = r.getParen(2);
									// адрес
									String link = null;
									//String datanum = null;
									if(r2.match(t)) {
										link = r2.getParen(2);
									}
									//if(r3.match(t)) {
									//	datanum = r3.getParen(2);
									//}
									// текст
									String c = r.getParen(3);
									StringItem linkitem = new StringItem(null, Util.htmlText(c));
									linkitem.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
									linkitem.setLayout(Item.LAYOUT_2);
									linkitem.addCommand(postLinkItemCmd);
									linkitem.setDefaultCommand(postLinkItemCmd);
									linkitem.setItemCommandListener(JChMIDlet.this);
									threadFrm.append(linkitem);
									links.put(linkitem, link);
								} else if(w.startsWith("<strong")) {
									// жирный текст
									String c = r.getParen(4);
									StringItem bolditem = new StringItem(null, Util.htmlText(c));
									bolditem.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL));
									bolditem.setLayout(Item.LAYOUT_2);
									threadFrm.append(bolditem);
								} else if(w.startsWith("<b>")) {
									// жирный текст
									String c = r.getParen(5);
									StringItem bolditem = new StringItem(null, Util.htmlText(c));
									bolditem.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL));
									bolditem.setLayout(Item.LAYOUT_2);
									threadFrm.append(bolditem);
								} else if(w.startsWith("<i>")) {
									// жирный текст
									String c = r.getParen(6);
									StringItem bolditem = new StringItem(null, Util.htmlText(c));
									bolditem.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL));
									bolditem.setLayout(Item.LAYOUT_2);
									threadFrm.append(bolditem);
								} else if(w.startsWith("<em>")) {
									// курсивный текст
									String c = r.getParen(7);
									StringItem textitem = new StringItem(null, Util.htmlText(c));
									textitem.setFont(Font.getFont(0, Font.STYLE_ITALIC, Font.SIZE_SMALL));
									textitem.setLayout(Item.LAYOUT_2);
									threadFrm.append(textitem);
								} else if(w.startsWith("<span")) {
									String t = r.getParen(8);
									String cls = null;
									if(r3.match(t)) {
										cls = r3.getParen(2);
									}
									String c = r.getParen(9);
									StringItem textitem = new StringItem(null, Util.htmlText(c));
									textitem.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
									textitem.setLayout(Item.LAYOUT_2);
									if(cls.equals("s")) {
										// зачеркнутый текст, но в lcdui такого стиля шрифта нету
									} else if(cls.equals("u")) {
										// подчеркнутый текст
										textitem.setFont(Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
									} else if(cls.equals("o")) {
										// надчеркнутый текст, но подчеркнутый из за lcdui
										textitem.setFont(Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
									} else if(cls.equals("spoiler")) {
										// спойлер
										textitem.setFont(Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
									} else if(cls.equals("unkfunc")) {
										// цитата
									}
									threadFrm.append(textitem);
								}
							}
							if (ti < tl) {
								String o = x.substring(ti);
								if(o != null && !o.equals("")) {
									StringItem textitem = new StringItem(null, Util.htmlText(o));
									textitem.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
									textitem.setLayout(Item.LAYOUT_2);
									threadFrm.append(textitem);
								}
					        }
							if(files != null) {
								int fl = files.size();
								for(int fi = 0; fi < fl && fi < 5; fi++) {
									JSONObject file = files.getObject(fi);
									String name = file.getString("displayname", file.getString("name", ""));
									ImageItem fitem = new ImageItem(name, null, 0, name);
									fitem.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
									fitem.addCommand(fileImgItemOpenCmd);
									fitem.setDefaultCommand(fileImgItemOpenCmd);
									fitem.setItemCommandListener(JChMIDlet.this);
									addFile(fitem, file.getString("path", null), file.getString("thumbnail", null));
									threadFrm.append(fitem);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else if(c == fileImgItemOpenCmd) {
			String path = (String) files.get(item);
			if(path != null) {
				try {
					if(platformRequest(prepareUrl(path, "http://nnproject.cc/glype/browse.php?u="))) {
						//notifyDestroyed();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if(c == postLinkItemCmd) {
			String path = (String) links.get(item);
			if(path != null) {
				if(path.startsWith("/")) {
					try {
						if(platformRequest(prepareUrl(path, "http://nnproject.cc/glype/browse.php?u="))) {
							//notifyDestroyed();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						if(platformRequest(path)) {
							//notifyDestroyed();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected void addFile(ImageItem img, String path, String thumb) {
		files.put(img, path);
		thumbsToLoad.addElement(new Object[] { thumb, img });
		synchronized(thumbLoadLock) {
			thumbLoadLock.notifyAll();
		}
	}

	protected void display(Displayable f) {
		Display.getDisplay(this).setCurrent(f);
	}

	private void board(String txt) {
		txt = txt.toLowerCase();
		if(txt.length() <= 0)
			return;
		if(txt.startsWith("/"))
			txt = txt.substring(1);
		if(txt.endsWith("/"))
			txt = txt.substring(0, txt.length() - 1);
		final String board = txt;
		currentBoard = board;
		System.out.println("Board: " + board);
		createBoard(board);
		Display.getDisplay(this).setCurrent(boardFrm);
	}
	
	private void createBoard(final String board) {
		boardFrm = new Form("Jch - /" + board + "/");
		boardFrm.addCommand(backCmd);
		boardFrm.setCommandListener(this);
		boardFrm.append(boardSearchField = new TextField("","", 1000, TextField.ANY));
		boardSearchField.setLabel("Поиск");
		boardSearchField.addCommand(boardSearchCmd);
		boardSearchField.setItemCommandListener(this);
		/*StringItem btn = new StringItem("Поиск", "", StringItem.BUTTON);
		btn.setDefaultCommand(boardSearchCmd);
		btn.setItemCommandListener(this);
		boardFrm.append(btn);*/
		new Thread() {
			public void run() {
				JSONObject j = null;
				try {
					getResult(board + "/threads.json");
					if(!(result instanceof JSONObject))
						throw new RuntimeException("Result not object: " + result);
					j = (JSONObject) result;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(j == null || !(result instanceof JSONObject))
					return;
				JSONArray th = j.getNullableArray("threads");
				if(th == null)
					return;
				try {
					int l = th.size();
					for(int i = 0; i < l && i < 20; i++) {
						JSONObject thread = th.getObject(i);
						System.out.println(thread);
						StringItem s = new StringItem("#" + thread.getString("num", ""),
								Util.htmlText(thread.getString("subject", "")));
						s.addCommand(openThreadCmd);
						s.setDefaultCommand(openThreadCmd);
						s.setItemCommandListener(JChMIDlet.this);
						boardFrm.append(s);
						//boardFrm.append(Util.text(comment) + "\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public static Image getImg(String url) throws IOException {
		url = prepareUrl(url);
		byte[] b = Util.get(url);
		return Image.createImage(b, 0, b.length);
	}

	public static String prepareUrl(String url) {
		return prepareUrl(url, "http://nnproject.cc/proxy.php?");
	}

	public static String prepareUrl(String url, String proxy) {
		if(url.startsWith("/")) url = url.substring(1);
		if(url.endsWith("&") || url.endsWith("?"))
			url = url.substring(0, url.length() - 1);
		if(proxy != null && proxy.length() > 1) {
			url = proxy + Util.encodeUrl("https://2ch.hk/" + url);
		} else {
			url = "https://2ch.hk/" + url;
		}
		return url;
	}

}
