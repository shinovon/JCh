import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
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
	private static String platform;
	private static Object result;
	private static String version;
	private Form mainFrm;
	private Form boardFrm;
	private TextField boardField;
	private String currentBoard;
	private TextField boardSearchField;
	private boolean started;
	
	private static String version() {
		return version;
	}

	public JChMIDlet() {
		mainFrm = new Form("JCh - Главная");
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
		result = null;
		download(prepareUrl(string));
		if(((String) result).charAt(0) == '{')
			result = JSON.getObject((String) result);
		else if(((String) result).charAt(0) == '[')
			result = JSON.getArray((String) result);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == exitCmd)
			destroyApp(false);
		else if(c == backCmd)
			Display.getDisplay(this).setCurrent(mainFrm);
	}

	public void commandAction(Command c, Item item) {
		if(c == boardFieldCmd) {
			board(boardField.getString());
		} else if(c == boardCmd || c.getLabel().startsWith("Треды")) {
			if(item instanceof TextField)
				board(((TextField)item).getString());
			else if(item instanceof StringItem)
				board(StringUtils.split(((StringItem)item).getText(), '/')[1]);
		}
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
		boardFrm = new Form("JCh - /" + board + "/");
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
					for(int i = 0; i < l; i++) {
						JSONObject thread = th.getObject(i);
						String subject = thread.getNullableString("subject");
						String comment = thread.getNullableString("comment");
						String num = thread.getNullableString("num");
						boardFrm.append(subject + " #" + num + "\n");
						boardFrm.append(comment + "\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private static String platform() {
		if(platform != null)
			return platform;
		return platform = System.getProperty("microedition.platform");
	}

	public static void download(String url) throws IOException {
		System.out.println("GET " + url);
		HttpConnection con = (HttpConnection) open(url);

		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			con.setRequestMethod("GET");
			con.getResponseCode();
			is = con.openInputStream();
			o = new ByteArrayOutputStream();
			byte[] buf = new byte[256];
			int len;
			while ((len = is.read(buf)) != -1) {
               o.write(buf, 0, len);
			}
			result = new String(o.toByteArray(), "UTF-8");
		} catch (NullPointerException e) {
			throw new IOException(e.toString());
		} finally {
			if (is != null)
				is.close();
			if (con != null)
				con.close();
			if (o != null)
				o.close();
		}
	}

	public static ContentConnection open(String url) throws IOException {
		try {
			ContentConnection con = (ContentConnection) Connector.open(url, Connector.READ);
			if (con instanceof HttpConnection)
				((HttpConnection) con).setRequestProperty("User-Agent", "JCh/" + version() + " (" + platform() + ")");
			return con;
		} catch (IOException e) {
			throw new IOException(StringUtils.cut(StringUtils.cut(e.toString(), "Exception"), "java.io.") + " "
					+ url);
		}
	}

	public static String prepareUrl(String url) {
		if(url.endsWith("&") || url.endsWith("?"))
			url = url.substring(0, url.length() - 1);
		url = "http://nnproject.cc/proxy.php?" + URLEncoder.encode("https://2ch.hk/" + url);
		return url;
	}

}
