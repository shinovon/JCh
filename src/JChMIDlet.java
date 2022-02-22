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

public class JChMIDlet extends MIDlet implements CommandListener, ItemCommandListener {

	private static Command exitCmd = new Command("Выход", Command.EXIT, 0);
	private static Command backCmd = new Command("Назад", Command.BACK, 0);
	private static Command boardFieldCmd = new Command("Раздел", Command.OK, 0);
	private static Command boardCmd = new Command("Треды", Command.OK, 0);
	private static Command boardSearchCmd = new Command("Поиск", Command.OK, 0);
	private static Object result;
	private static String version;
	private Form mainFrm;
	private Form boardFrm;
	private TextField boardField;
	private String currentBoard;
	private TextField boardSearchField;
	private boolean started;
	protected Command openThreadCmd = new Command("Открыть тему", Command.ITEM, 0);
	protected Form threadFrm;
	protected Command fileImgItemOpenCmd = new Command("Открыть файл", Command.ITEM, 0);
	private Hashtable files = new Hashtable();
	private Object thumbLoadLock = new Object();
	protected boolean running = true;
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
					threadFrm.setTitle("/" + j.getString("Board", "?") + "/ " + j.getString("title", ""));
					System.out.println(j);
					if(j == null || !(result instanceof JSONObject))
						return;
					JSONArray th = j.getNullableArray("threads");
					if(th == null)
						return;
					JSONArray posts = th.getObject(0).getArray("posts");
					try {
						int l = posts.size();
						for(int i = 0; i < l && i < 20; i++) {
							JSONObject post = posts.getObject(i);
							JSONArray files = post.getNullableArray("files");
							System.out.println(post);
							StringItem ts = new StringItem(post.getString("name", "") + " " + post.getString("date", "") + " #" + post.getString("num", ""), "");
							ts.setFont(Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL));
							threadFrm.append(ts);
							threadFrm.append(post.getString("comment", "") + "\n");
							if(files != null) {
								int fl = files.size();
								for(int fi = 0; fi < fl && fi < 5; fi++) {
									JSONObject file = files.getObject(fi);
									String name = file.getString("displayname", file.getString("name", ""));
									ImageItem fitem = new ImageItem(name, null, 0, name);
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
		StringItem btn = new StringItem("Поиск", "", StringItem.BUTTON);
		btn.setDefaultCommand(boardSearchCmd);
		btn.setItemCommandListener(this);
		mainFrm.append(btn);
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
								Util.text(thread.getString("subject", "")));
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
		if(url.startsWith("/")) url = url.substring(1);
		url = prepareUrl(url);
		byte[] b = Util.get(url);
		return Image.createImage(b, 0, b.length);
	}

	public static String prepareUrl(String url) {
		if(url.endsWith("&") || url.endsWith("?"))
			url = url.substring(0, url.length() - 1);
		url = "http://nnproject.cc/proxy.php?" + Util.encodeUrl("https://2ch.hk/" + url);
		return url;
	}

}
