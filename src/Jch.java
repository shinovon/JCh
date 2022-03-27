import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;

import dom.NamedNodeMap;
import dom.Node;
import dom.NodeList;
import json.JSONArray;
import json.JSONObject;
import json.NullEquivalent;
import tidy.Tidy;

public class Jch implements CommandListener, ItemCommandListener, ItemStateListener, Runnable {
	public static String platform;
	private static String version;
	private static String cookie;
	
	private static final String aboutText =
			  "<h1>JCh</h1><br>"
			+ "версия <ver><br><br>"
			+ "Клиент <a href=\"https://2ch.hk\">2ch.hk</a> для Symbian/J2ME устройств<br><br>"
			+ "<b>Разработал</b><br>"
			+ "Shinovon (<a href=\"http://nnp.nnchan.ru\">nnproject.cc</a>)<br><br>"
			+ "<b>Использованые библиотеки</b><br>"
			+ "org.w3c.dom<br>"
			+ "org.w3c.tidy<br>"
			+ "cc.nnproject.json<br><br>"
			/*+ "<b>Другие программы на Java</b><br>"
			+ "<a href=\"http://nnp.nnchan.ru\">JTube</a><br>"
			+ "<a href=\"http://nnp.nnchan.ru\">Bing Translate</a><br><br>"
			+ "<a href=\"http://vk4me.curoviyx.ru\">VK4ME (curoviyxru)</a><br><br>"
			+ "<b>Лицензии</b><br>"
			+ "(c) 1998-2000 (W3C) MIT, INRIA, Keio University<br>"
			+ "See Tidy.java for the copyright notice.<br>"
			+ "Derived from <a href=\"http://www.w3.org/People/Raggett/tidy\"><br>"
			+ "HTML Tidy Release 4 Aug 2000</a><br><br>"
			+ "Copyright (c) 1998-2000 World Wide Web Consortium (Massachusetts<br>"
			+ "Institute of Technology, Institut National de Recherche en<br>"
			+ "Informatique et en Automatique, Keio University). All Rights<br>"
			+ "Reserved."*/;
	
	private static final String CONFIG_RECORD_NAME = "jchconfig";
	private static final String DEFAULT_INSTANCE_URL = "2ch.hk";
	private static final String DEFAULT_GLYPE_URL = "http://nnp.nnchan.ru/glype/browse.php?u=";

	private static Command exitCmd = new Command("Выход", Command.EXIT, 0);
	private static Command backCmd = new Command("Назад", Command.BACK, 0);
	private static Command boardFieldCmd = new Command("Открыть", Command.OK, 0);
	private static Command boardCmd = new Command("Треды", Command.OK, 0);
	private static Command boardSearchItemCmd = new Command("Поиск", Command.OK, 0);
	private static Command openThreadCmd = new Command("Открыть тред", Command.ITEM, 0);
	private static Command fileImgItemOpenCmd = new Command("Открыть файл", Command.ITEM, 0);
	private static Command postLinkItemCmd = new Command("Открыть ссылку", Command.ITEM, 0);
	private static Command postSpoilerItemCmd = new Command("Показать спойлер", Command.ITEM, 0);
	private static Command nextPostsItemCmd = new Command("След. посты", Command.ITEM, 0);
	private static Command prevPostsItemCmd = new Command("Пред. посты", Command.ITEM, 0);
	private static Command boardsItemCmd = new Command("Доски", Command.ITEM, 0);
	private static Command postTextItemCmd = new Command("Ред. текст", Command.ITEM, 0);
	private static Command postAddFileItemCmd = new Command("Добавить файл", Command.ITEM, 0);
	private static Command openByLinkItemCmd = new Command("Открыть по ссылке", Command.ITEM, 0);
	private static Command postThreadCmd = new Command("Запостить тред", Command.SCREEN, 0);
	private static Command postCommentCmd = new Command("Ответить в тред", Command.SCREEN, 0);
	private static Command aboutCmd = new Command("О программе", Command.SCREEN, 0);
	private static Command settingsCmd = new Command("Настройки", Command.SCREEN, 0);
	//private static Command agreeCmd = new Command("Да", Command.OK, 0);
	//private static Command disagreeCmd = new Command("Нет", Command.EXIT, 0);
	private static Command postCmd = new Command("Запостить", Command.OK, 0);
	private static Command captchaConfirmCmd = new Command("Подтвердить", Command.OK, 0);
	private static Command textOkCmd = new Command("Ок", Command.OK, 0);
	private static Command threadGotoStartCmd = new Command("Перейти к началу треда", Command.SCREEN, 0);
	private static Command linkOkCmd = new Command("Ок", Command.OK, 0);
	
	static Display display;
	public static JChMIDlet midlet;
	public static Jch inst;

	private static Font largeBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
	private static Font smallPlainFont = Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private static Font smallBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL);
	private static Font smallItalicFont = Font.getFont(0, Font.STYLE_ITALIC, Font.SIZE_SMALL);
	private static Font smallUnderlinedFont = Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
	
	private static Object result;

	private static Tidy tidy;
	
	private static Form mainFrm;
	private static Form boardFrm;
	private static Form threadFrm;
	private static Form aboutFrm;
	private static Form boardsFrm;
	private static Form settingsFrm;
	private static Form postingFrm;
	private static Form captchaFrm;
	private static Form searchFrm;
	
	private static TextField boardField;
	private static TextField boardSearchField;
	
	private static TextField setInstanceField;
	private static TextField setApiProxyField;
	private static TextField setPreviewProxyField;
	private static TextField setFileProxyField;
	private static ChoiceGroup setChoice;
	private static Gauge setMaxPostsGauge;
	
	private static TextField postSubjectField;
	private static TextField postTextField;
	private static StringItem postTextBtn;
	private static TextBox tempTextBox;
	private static TextField captchaField;

	private static TextField searchField;
	
	private static String currentBoard;
	private static String currentThread;

	private static String postThread;
	private static String postBoard;
	private static String currentPost;
	//private int postingFileBtnIdx;
	private static String captchaId;
	private static int postError;

	private static boolean running = true;
	
	private static StringItem tempLoadingLabel;
	private static int loadingLabelIndex = -1;
	
	private static Hashtable files = new Hashtable();
	private static Hashtable links = new Hashtable();
	private static Hashtable spoilers = new Hashtable();
	private static Hashtable comments = new Hashtable();
	private static Hashtable searchLinks = new Hashtable();
	
	private static Thread lastThread;
	
	private static Object thumbLoadLock = new Object();
	private static Vector thumbsToLoad = new Vector();
	private static Thread thumbLoaderThread = new Thread(new Jch(5));

	private static JSONArray cachedPosts;
	private static int postsCount;
	private static int currentIndex;
	
	private static String query;

	// Settings
	private static boolean direct2ch;
	private static String instanceUrl = DEFAULT_INSTANCE_URL;
	private static String apiProxyUrl = DEFAULT_GLYPE_URL;
	private static String previewProxyUrl = DEFAULT_GLYPE_URL;
	private static String fileProxyUrl = DEFAULT_GLYPE_URL;
	private static int maxPostsCount = 10;
	private static boolean time2ch;
	private static boolean filePreview = true;
	private static boolean directFile;
	
	private int func = 0;
	
	//private static final RE htmlRe = new RE("(<a(.*?)>(.*?)</a>|<strong>(.*?)</strong>|<b>(.*?)</b>|<i>(.*?)</i>|<em>(.*?)</em>|<span(.*?)>(.*?)</span>|(<h>(.*?)</h>))");
	//private static final RE hrefRe = new RE("(href=\"(.*?)\")");
	//private static final RE classRe = new RE("(class=\"(.*?)\")");
	
	static String version() {
		return version;
	}
	
	public Jch(int f) { 
		func = f;
	}

	public Jch() {
	}

	public void run() {
		//XXX
		switch(func) {
		case 1:
			_loadBoards();
			break;
		case 2:
			loadSearch(query);
			break;
		case 3:
			loadThread(currentThread, currentBoard, currentPost);
			break;
		case 4:
			loadBoard(currentBoard);
			break;
		case 5:
			thumbsLoadLoop();
			break;
		default:
			break;
		}
	}
	
	private void thumbsLoadLoop() {
		try {
			while(running) {
				synchronized(thumbLoadLock) {
					thumbLoadLock.wait();
				}
				while(thumbsToLoad.size() > 0) {
					int i = 0;
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

	protected static void startApp() {
		platform = System.getProperty("microedition.platform");
		try {
			RecordStore r = RecordStore.openRecordStore("jchcookie", false);
			byte[] b = r.getRecord(1);
			r.closeRecordStore();
			cookie = new String(b);
		} catch (Exception e) {
		}
		Jch.inst = new Jch();
		tidy = new Tidy();
		mainFrm = new Form("Jch - Главная");
		mainFrm.setCommandListener(inst);
		mainFrm.addCommand(exitCmd);
		mainFrm.addCommand(aboutCmd);
		mainFrm.addCommand(settingsCmd);
		mainFrm.append(boardField = new TextField("Доска", "", 8, TextField.ANY));
		boardField.addCommand(boardFieldCmd);
		boardField.setItemCommandListener(inst);
		StringItem btn = new StringItem(null, "Ввод", StringItem.BUTTON);
		btn.setDefaultCommand(boardFieldCmd);
		btn.setItemCommandListener(inst);
		mainFrm.append(btn);
		StringItem btn2 = new StringItem(null, "Доски", StringItem.BUTTON);
		btn2.setLayout(Item.LAYOUT_EXPAND);
		btn2.setDefaultCommand(boardsItemCmd);
		btn2.setItemCommandListener(inst);
		mainFrm.append(btn2);
		StringItem btn3 = new StringItem(null, "Открыть пост по ссылке", StringItem.BUTTON);
		btn3.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE);
		btn3.setDefaultCommand(openByLinkItemCmd);
		btn3.setItemCommandListener(inst);
		mainFrm.append(btn3);
		if(version == null)
			version = midlet.getAppProperty("MIDlet-Version");
		RecordStore r = null;
		try {
			r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, false);
		} catch (Exception e) {
		}
		if(r != null) {
			try {
				JSONObject j = getObject(new String(r.getRecord(1), "UTF-8"));
				r.closeRecordStore();
				direct2ch = j.getBoolean("direct", direct2ch);
				instanceUrl = j.getString("instance", instanceUrl);
				apiProxyUrl = j.getString("apiproxy", apiProxyUrl);
				previewProxyUrl = j.getString("previewproxy", previewProxyUrl);
				fileProxyUrl = j.getString("fileproxy", fileProxyUrl);
				time2ch = j.getBoolean("time2ch", time2ch);
				maxPostsCount = j.getInt("maxposts", maxPostsCount);
				filePreview = j.getBoolean("filepreview", filePreview);
				directFile = j.getBoolean("directfile", directFile);
			} catch (Exception e) {
			}
		}
		thumbLoaderThread.setPriority(2);
		thumbLoaderThread.start();
		String s = System.getProperty("os.name");
		if(s != null) {
			s = s.toLowerCase();
			if(s.indexOf("nux") != -1 || s.indexOf("win") != -1 || s.indexOf("mac") != -1) {
				/*
				Alert a = new Alert("");
				a.setTitle("Предупреждение");
				a.setString("В эмуляторе содержимое постов может отображаться некорректно!");
				a.addCommand(Alert.DISMISS_COMMAND);
				display.setCurrent(a, mainFrm);
				*/
				mainFrm.append("\n\nВ эмуляторах содержимое постов может отображаться некорректно!");
			}
		}
		display(mainFrm);
		
	}

	private static void loadBoards() {
		(lastThread = new Thread(new Jch(1))).start();
	}
	
	private static void _loadBoards() {
		try {
			removeLoadingLabel(boardsFrm);
			/*
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
					btn.setItemCommandListener(inst);
					btn.setLayout(Item.LAYOUT_EXPAND);
					mainFrm.append(btn);
					Thread.yield();
				}
				Thread.yield();
			}
			*/
			getResult("boards.json");
			//System.out.println(result);
			if(!(result instanceof JSONObject))
				return;
			System.gc();
			JSONArray boards = ((JSONObject)result).getArray("boards");
			Enumeration en = boards.elements();
			boards = null;
			while(en.hasMoreElements()) {
				JSONObject board = (JSONObject) en.nextElement();
				String id = board.getNullableString("id");
				String name = board.getNullableString("name");
				StringItem btn = new StringItem(null, "/".concat(id).concat("/ ").concat(name), StringItem.BUTTON);
				btn.setDefaultCommand(boardCmd);
				btn.setItemCommandListener(inst);
				btn.setLayout(Item.LAYOUT_EXPAND);
				boardsFrm.append(btn);
				Thread.yield();
			}
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			removeLoadingLabel(boardsFrm);
			addLoadingLabel(boardsFrm, "Ошибка!");
			StringItem s = new StringItem(null, e.toString());
			s.setLayout(Item.LAYOUT_LEFT);
			boardsFrm.append(s);
		}
	}

	public void commandAction(Command c, Displayable d) {
		_commandAction(c, d);
	}

	private static void _commandAction(Command c, Displayable d) {
		if(c == exitCmd)
			midlet.destroyApp(false);
		else if(c == backCmd) {
			if(d == boardFrm) {
				display.setCurrent(mainFrm);
				boardFrm.deleteAll();
				boardFrm = null;
				boardSearchField = null;
			} else if(d == threadFrm) {
				display.setCurrent(searchFrm != null ? searchFrm : boardFrm != null ? boardFrm : mainFrm);
				if(lastThread != null && lastThread.isAlive()) {
					lastThread.interrupt();
					lastThread = null;
				}
				threadFrm.deleteAll();
				clearThreadData();
				threadFrm = null;
				currentThread = null;
				System.gc();
			} else if(d == aboutFrm) {
				display.setCurrent(mainFrm);
				clearThreadData();
				aboutFrm = null;
			} else if(d == boardsFrm) {
				display.setCurrent(mainFrm);
				boardsFrm.deleteAll();
				boardsFrm = null;
			} else if(d == postingFrm) {
				if(threadFrm != null && postThread != null && !postThread.equals("0")) {
					display.setCurrent(threadFrm);
				} else if(boardFrm != null && postBoard != null) {
					display.setCurrent(boardFrm);
				} else {
					display.setCurrent(mainFrm);
				}
				postingFrm = null;
			} else if(d == settingsFrm) {
				direct2ch = setChoice.isSelected(0);
				directFile = setChoice.isSelected(1);
				time2ch = setChoice.isSelected(2);
				filePreview = setChoice.isSelected(3);
				instanceUrl = setInstanceField.getString();
				apiProxyUrl = setApiProxyField.getString();
				previewProxyUrl = setPreviewProxyField.getString();
				fileProxyUrl = setFileProxyField.getString();
				maxPostsCount = setMaxPostsGauge.getValue();
				display.setCurrent(mainFrm);
				try {
					RecordStore.deleteRecordStore(CONFIG_RECORD_NAME);
				} catch (Throwable e) {
				}
				try {
					RecordStore r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, true);
					JSONObject j = new JSONObject();
					j.put("direct", new Boolean(direct2ch));
					j.put("instance", instanceUrl);
					j.put("apiproxy", apiProxyUrl);
					j.put("previewproxy", previewProxyUrl);
					j.put("fileproxy", fileProxyUrl);
					j.put("time2ch", new Boolean(time2ch));
					j.put("maxposts", new Integer(maxPostsCount));
					j.put("filepreview", new Boolean(filePreview));
					j.put("directfile", new Boolean(directFile));
					byte[] b = j.build().getBytes("UTF-8");
					
					r.addRecord(b, 0, b.length);
					r.closeRecordStore();
				} catch (Exception e) {
				}
			} else if(d == captchaFrm) {
				if(postError == -5) {
					captchaFrm.deleteAll();
					postError = 200;
					try {
						generateCaptcha();
					} catch (Exception e) {
						e.printStackTrace();
						addLoadingLabel(captchaFrm, "Ошибка получения капчи");
						StringItem s = new StringItem(null, e.toString());
						s.setLayout(Item.LAYOUT_LEFT);
						captchaFrm.append(s);
					}
				} else if(postError == 200 || postError == -6 || postError == -7 || (postError <= -1 && postError >= -4)) {
					captchaFrm.deleteAll();
					if(threadFrm != null && postThread != null && !postThread.equals("0")) {
						display.setCurrent(threadFrm);
					} else if(boardFrm != null && postBoard != null) {
						display.setCurrent(boardFrm);
					} else {
						display.setCurrent(mainFrm);
					}
				} else {
					display.setCurrent(postingFrm);
				}
			} else if(d == searchFrm) {
				display.setCurrent(boardFrm);
				clearThreadData();
				searchLinks.clear();
				searchFrm.deleteAll();
				searchFrm = null;
				searchField = null;
			} else if(d == tempTextBox) {
				display.setCurrent(mainFrm);
				tempTextBox = null;
			}
	
			System.gc();
		} else if(c == aboutCmd) {
			aboutFrm = new Form("Jch - О программе");
			aboutFrm.addCommand(backCmd);
			aboutFrm.setCommandListener(inst);
			StringItem s = new StringItem(null, "");
			aboutFrm.append(s);
			parseHtmlText(aboutFrm, replace(aboutText, "<ver>", version));
			display(aboutFrm);
			if(platform.indexOf("S60") > -1 && platform.indexOf("=3.2") == -1)
			try {
				display.setCurrentItem(s);
			} catch (Throwable e) {
			}
		} else if(c == postThreadCmd) {
			postThread = "0";
			postBoard = currentBoard;
			createPostingForm();
		} else if(c == postCommentCmd) {
			postThread = currentThread;
			postBoard = currentBoard;
			createPostingForm();
		} else if(c == textOkCmd) {
			postTextField.setString(tempTextBox.getString());
			display(postingFrm);
			tempTextBox = null;
		} else if(c == settingsCmd) {
			if(settingsFrm == null) {
				settingsFrm = new Form("Jch - Настройки");
				settingsFrm.addCommand(backCmd);
				settingsFrm.setCommandListener(inst);
				settingsFrm.setItemStateListener(inst);
				setInstanceField = new TextField("Инстанс двача", instanceUrl, 100, TextField.URL);
				settingsFrm.append(setInstanceField);
				setChoice = new ChoiceGroup("", Choice.MULTIPLE, new String[] { "Прямое подключение", "Открывать файлы напрямую", "Дата поста с сайта", "Отображение превью" }, null);
				setChoice.setSelectedFlags(new boolean[] { direct2ch, directFile, time2ch, filePreview });
				settingsFrm.append(setChoice);
				setMaxPostsGauge = new Gauge("Кол-во постов на странице", true, 30, maxPostsCount);
				settingsFrm.append(setMaxPostsGauge);
				setApiProxyField = new TextField("Прокси для API", apiProxyUrl, 100, direct2ch ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL);
				settingsFrm.append(setApiProxyField);
				setPreviewProxyField = new TextField("Прокси для превью", previewProxyUrl, 100, direct2ch ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL);
				settingsFrm.append(setPreviewProxyField);
				setFileProxyField = new TextField("Прокси для открытия файлов", fileProxyUrl, 100, directFile ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL);
				settingsFrm.append(setFileProxyField);
			}
			display(settingsFrm);
		} else if(c == postCmd) {
			try {
				captchaFrm = new Form("Jch - Ввод капчи");
				captchaFrm.addCommand(backCmd);
				display(captchaFrm);
				generateCaptcha();
			} catch (Exception e) {
				e.printStackTrace();
				addLoadingLabel(captchaFrm, "Ошибка получения капчи");
				StringItem s = new StringItem(null, e.toString());
				s.setLayout(Item.LAYOUT_LEFT);
				captchaFrm.append(s);
				captchaFrm.addCommand(backCmd);
				captchaFrm.setCommandListener(inst);
			}
		} else if(c == captchaConfirmCmd) {
			postError = 200;
			captchaFrm.removeCommand(captchaConfirmCmd);
			captchaFrm.deleteAll();
			//captchaFrm.addCommand(backCmd);
			captchaFrm.setTitle("Jch - Отправка...");
			captchaFrm.append("Отправка...\n");
			String cid = /*captchaIdField.getString()*/ captchaId;
			String ckey = captchaField.getString();
			String subj = postSubjectField.getString();
			String comm = postTextField.getString();
			String content = "task=post"
							+ "&board=" + postBoard
							+ "&thread=" + postThread
							+ "&captcha_type=2chcaptcha"
							+ (subj != null && subj.length() > 0 ? "&subject=" + encodeUrl(subj) : "")
							+ (comm != null && comm.length() > 0 ? "&comment=" + encodeUrl(comm) : "")
							+ "&2chcaptcha_id=" + cid
							+ "&2chcaptcha_value=" + ckey
							;
			//captchaFrm.append(content);
			try {
				byte[] b = post("http://nnp.nnchan.ru:80/2chpost.php", content);
				String s = null;
				try {
					s = new String(b, "UTF-8");
				} catch (Exception e) {
					s = new String(b);
				}
				b = null;
				captchaFrm.setTitle("Jch - Отправлено");
				//System.out.println(s);
				JSONObject j = getObject(s);
				int error = j.getInt("Error", 0);
				String num = j.getString("Num", null);
				String tar = j.getString("Target", null);
				String reason = j.getString("Reason", null);
				postError = error;
				captchaFrm.deleteAll();
				if(error != 0) {
					captchaFrm.append("Ошибка: " + error + "\nПричина: " + reason);
				} else if(num != null) {
					captchaFrm.append("Пост создан\nНомер: \n");
					StringItem snum = new StringItem(null, " #" + num);
					snum.setFont(smallBoldFont);
					snum.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
					snum.addCommand(postLinkItemCmd);
					snum.setDefaultCommand(postLinkItemCmd);
					snum.setItemCommandListener(inst);
					captchaFrm.append(snum);
					links.put(snum, "/" + postBoard + "/res/" + postThread + ".html#" + num);
				} else if(tar != null) {
					captchaFrm.append("Тред создан\nНомер: \n");
					StringItem snum = new StringItem(null, " #" + tar);
					snum.setFont(smallBoldFont);
					snum.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
					snum.addCommand(postLinkItemCmd);
					snum.setDefaultCommand(postLinkItemCmd);
					snum.setItemCommandListener(inst);
					captchaFrm.append(snum);
					links.put(snum, "/" + postBoard + "/res/" + tar + ".html");
				}
				//captchaFrm.append(s);
			} catch (Exception e) {
				captchaFrm.deleteAll();
				e.printStackTrace();
				captchaFrm.append(e.toString());
			}
		} else if(c == threadGotoStartCmd) {
			openThread(currentThread, currentBoard, null);
		} else if(c == linkOkCmd) {
			String s = tempTextBox.getString();
			tempTextBox = null;
			s = sub(s, "https:");
			s = sub(s, "http:");
			s = sub(s, "//");
			s = sub(s, "2ch.");
			s = sub(s, "hk");
			s = sub(s, "life");
			s = sub(s, "/");
			if(s.indexOf("/") == -1) {
				display(mainFrm);
				return;
			}
			String brd = s.substring(0, s.indexOf('/'));
			if(brd.length() == 0 || brd.equals("res")) {
				display(mainFrm);
				return;
			}
			//System.out.println(brd);
			s = s.substring(s.indexOf('/'));
			//System.out.println(s);
			s = sub(s, "/res");
			if(s.indexOf("/") == -1) {
				display(mainFrm);
				return;
			}
			s = sub(s, "/");
			if(s.indexOf(".html") == -1) {
				display(mainFrm);
				return;
			}
			String tid = s.substring(0, s.indexOf(".html"));
			s = s.substring(s.indexOf(".html") + 5);
			String pid = null;
			if(s.indexOf("#") != -1) {
				pid = s.substring(s.indexOf('#') + 1);
			}
			//System.out.println(tid);
			//System.out.println(pid);
			openThread(tid, brd, pid);
		}
	}

	private static String sub(String s, String sub) {
		if(s.startsWith(sub)) {
			s = s.substring(sub.length());
		}
		return s;
	}

	private static void generateCaptcha() throws Exception {
		result = getObject(getString("http://nnp.nnchan.ru:80/glype/browse.php?u=https://2ch.life/api/captcha/2chcaptcha/id"));
		captchaId = ((JSONObject) result).getString("id");
		//String input = ((JSONObject) result).getString("input");
		//System.out.println(captchaId + " " + input);
	
		byte[] b = get("http://nnp.nnchan.ru:80/glype/browse.php?u=https://2ch.life/api/captcha/2chcaptcha/show?id=" + captchaId);
		captchaFrm.append(Image.createImage(b, 0, b.length));
		
		/*
		StringItem s2 = new StringItem("", "Получить ид капчи", Item.BUTTON);
		final Command c3 = new Command("Получить ид капчи", Command.ITEM, 0);
		s2.addCommand(c3);
		s2.setDefaultCommand(c3);
		s2.setItemCommandListener(new ItemCommandListener() {
	
			public void commandAction(Command c, Item paramItem) {
				if(c == c3) {
	
					captchaFrm.append(captchaIdField = new TextField("Ид капчи", "", 200, TextField.ANY));
					StringItem s = new StringItem("", "Получить картинку", Item.BUTTON);
					final Command c2 = new Command("Получить картинку", Command.ITEM, 0);
					s.addCommand(c2);
					s.setDefaultCommand(c2);
					s.setItemCommandListener(new ItemCommandListener() {
	
						public void commandAction(Command c, Item paramItem) {
							if(c == c2) {
								captchaFrm.addCommand(captchaConfirmCmd);
								captchaFrm.setCommandListener(inst);
								captchaField.setConstraints(TextField.NUMERIC);
								try {
									platformRequest(prepareUrl("api/captcha/2chcaptcha/show?id=" + captchaIdField.getString()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						
					});
					captchaFrm.append(s);
					captchaFrm.append(captchaField = new TextField("Капча", "", 100, TextField.NUMERIC | TextField.UNEDITABLE));
					try {
						platformRequest(prepareUrl("api/captcha/2chcaptcha/id"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		captchaFrm.append(s2);
		*/
		captchaFrm.append(captchaField = new TextField("Капча", "", 100, TextField.NUMERIC));
		captchaFrm.addCommand(captchaConfirmCmd);
		captchaFrm.setCommandListener(inst);
	}

	public void itemStateChanged(Item item) {
		if(item == setChoice) {
			direct2ch = setChoice.isSelected(0);
			directFile = setChoice.isSelected(1);
			time2ch = setChoice.isSelected(2);
			filePreview = setChoice.isSelected(3);
			int c = direct2ch ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL;
			setApiProxyField.setConstraints(c);
			setPreviewProxyField.setConstraints(c);
			setFileProxyField.setConstraints(directFile ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL);
		}
	}
	
	private static void createPostingForm() {
		postingFrm = new Form("Jch - Форма постинга");
		postingFrm.addCommand(backCmd);
		postingFrm.addCommand(postCmd);
		postingFrm.setCommandListener(inst);
		postSubjectField = new TextField("Тема", "", 200, TextField.ANY);
		postingFrm.append(postSubjectField);
		postTextField = new TextField("Комментарий", "", 1000, TextField.ANY);
		postTextField.setLayout(Item.LAYOUT_VEXPAND | Item.LAYOUT_EXPAND );
		postingFrm.append(postTextField);
		postTextBtn = new StringItem(null, "...", StringItem.BUTTON);
		postTextBtn.setLayout(Item.LAYOUT_RIGHT);
		postTextBtn.addCommand(postTextItemCmd);
		postTextBtn.setDefaultCommand(postTextItemCmd);
		postTextBtn.setItemCommandListener(inst);
		postingFrm.append(postTextBtn);
		
		StringItem s = new StringItem(null, "Добавить файл", Item.BUTTON);
		s.setLayout(Item.LAYOUT_CENTER);
		s.addCommand(postAddFileItemCmd);
		s.setDefaultCommand(postAddFileItemCmd);
		s.setItemCommandListener(inst);
		/*postingFileBtnIdx = */postingFrm.append(s);
		display(postingFrm);
	}

	private static void clearThreadData() {
		files.clear();
		links.clear();
		spoilers.clear();
		comments.clear();
		thumbsToLoad.removeAllElements();
		currentIndex = 0;
		postsCount = 0;
	}

	public void commandAction(Command c, Item item) {
		_commandAction(c, item);
	}

	private static void _commandAction(Command c, Item item) {
		if(c == boardFieldCmd) {
			board(boardField.getString());
		} else if(c == boardCmd || c.getLabel().startsWith("Треды")) {
			if(item instanceof TextField)
				board(((TextField)item).getString());
			else if(item instanceof StringItem)
				board(split(((StringItem)item).getText(), '/')[1]);
		} else if(c == openThreadCmd && item.getLabel().startsWith("#")) {
			String s = item.getLabel().substring(1);
			//s = s.substring(0, s.indexOf(" "));
			openThread(s);
		} else if(c == fileImgItemOpenCmd) {
			String path = (String) files.get(item);
			if(path != null) {
				try {
					if(midlet.platformRequest(prepareUrl(path, directFile ? null : fileProxyUrl, instanceUrl))) {
						//notifyDestroyed();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if(c == postLinkItemCmd) {
			String path = (String) links.get(item);
			if(path == null) {
				path = (String) searchLinks.get(item);
			}
			//System.out.println(path);
			if(path != null) {
				if(path.startsWith("/") || path.startsWith("https://2ch.hk/") || path.startsWith("https://2ch.life/") || path.startsWith("https://" + instanceUrl + "/")) {
					if(path.indexOf("/res/") != -1 && path.indexOf(".html") != -1) {
						String bd = path;
						bd = bd.substring(0, bd.indexOf("/res/"));
						bd = bd.substring(bd.lastIndexOf('/')+1);
						String tid = path;
						tid = tid.substring(tid.indexOf("/res/") + "/res/".length(), tid.indexOf(".html"));
						//System.out.println(tid);
						if(path.indexOf("html#") != -1) {
							String cid = path;
							cid = cid.substring(cid.indexOf("html#") + "html#".length());
							//System.out.println(cid);
							if(tid.equals(currentThread)) {
								Item i = (Item) comments.get(cid);
								if(i != null) {
									try {
										display.setCurrentItem(i);
									} catch(Throwable e) {
									}
								} else {
									openThread(tid, bd, cid);
								}
							} else {
								openThread(tid, bd, cid);
							}
						} else {
							if(currentThread != null && currentThread.equalsIgnoreCase(tid)) return;
							openThread(tid, bd);
						}
					} else {
						try {
							if(midlet.platformRequest(prepareUrl(path, directFile ? null : fileProxyUrl, instanceUrl))) {
								//notifyDestroyed();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					try {
						if(midlet.platformRequest(path)) {
							//notifyDestroyed();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else if(c == boardSearchItemCmd) {
			searchLinks.clear();
			clearThreadData();
			searchFrm.deleteAll();
			searchFrm = new Form("Jch - Результаты поиска");
			searchFrm.addCommand(backCmd);
			searchFrm.setCommandListener(inst);
			query = ((TextField)item).getString();
			if(boardSearchField != null)
				boardSearchField.setString(query);
			searchFrm.append(searchField = new TextField("Поиск", query, 100, TextField.ANY));
			searchField.addCommand(boardSearchItemCmd);
			searchField.setItemCommandListener(inst);
			display(searchFrm);
			(lastThread = new Thread(new Jch(2))).start();
		} else if (c == postSpoilerItemCmd) {
			String t = (String) spoilers.get(item);
			if(t != null) {
				String l = (String) links.get(item);
				StringItem s = ((StringItem) item);
				s.setText(t);
				//s.setDefaultCommand(null);
				if(l == null) {
					s.setItemCommandListener(null);
				} else {
					s.removeCommand(postSpoilerItemCmd);
					s.addCommand(postLinkItemCmd);
					s.setDefaultCommand(postLinkItemCmd);
				}
				//s.removeCommand(postSpoilerItemCmd);
			}
		} else if(c == nextPostsItemCmd) {
			if(searchFrm != null) {
				clearThreadData();
				searchFrm.deleteAll();
				currentIndex += maxPostsCount;
				parsePosts(searchFrm, cachedPosts, maxPostsCount, true);
			} else {
				clearThreadData();
				threadFrm.deleteAll();
				currentIndex += maxPostsCount;
				parsePosts(threadFrm, cachedPosts, maxPostsCount, false);
			}
		} else if(c == prevPostsItemCmd) {
			if(searchFrm != null) {
				clearThreadData();
				searchFrm.deleteAll();
				currentIndex -= maxPostsCount;
				if(currentIndex < 0) currentIndex = 0;
				parsePosts(searchFrm, cachedPosts, maxPostsCount, true);
			} else {
				clearThreadData();
				threadFrm.deleteAll();
				currentIndex -= maxPostsCount;
				if(currentIndex < 0) currentIndex = 0;
				parsePosts(threadFrm, cachedPosts, maxPostsCount, false);
			}
		} else if(c == boardsItemCmd) {
			if(boardsFrm != null) {
				display(boardsFrm);
				return;
			}
			boardsFrm = new Form("Jch - Доски");
			boardsFrm.addCommand(backCmd);
			boardsFrm.setCommandListener(inst);
			display(boardsFrm);
			addLoadingLabel(boardsFrm);
			loadBoards();
		} else if(c == postTextItemCmd) {
			tempTextBox = new TextBox("", "", 1000, TextField.ANY);
			tempTextBox.setString(postTextField.getString());
			tempTextBox.addCommand(textOkCmd);
			tempTextBox.setCommandListener(inst);
			display(tempTextBox);
		} else if(c == openByLinkItemCmd) {
			tempTextBox = new TextBox("", "", 200, TextField.URL);
			tempTextBox.setTitle("URL");
			tempTextBox.addCommand(linkOkCmd);
			tempTextBox.addCommand(backCmd);
			tempTextBox.setCommandListener(inst);
			display(tempTextBox);
		}
	}
	
	private static void loadSearch(final String q) {
		try {
			postsCount = -1;
			getResult("makaba/makaba.fcgi?json=1&task=search&board=" + currentBoard + "&find=" + encodeUrl(q), apiProxyUrl, "2ch.life");
			if (!(result instanceof JSONObject))
				throw new RuntimeException("Result not object: " + result);
			JSONObject j = (JSONObject) result;
			JSONArray posts = j.getNullableArray("posts");
			result = j = null;
			if (posts == null || posts.size() == 0) {
				StringItem s = new StringItem(null, "Ничего не найдено");
				s.setLayout(Item.LAYOUT_CENTER);
				searchFrm.append(s);
				return;
			}
			parsePosts(searchFrm, posts, 0, true);
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			removeLoadingLabel(threadFrm);
			addLoadingLabel(threadFrm, "Ошибка!");
			StringItem s = new StringItem(null, e.toString());
			s.setLayout(Item.LAYOUT_LEFT);
			searchFrm.append(s);
		}
	}

	private static void openThread(String id) {
		openThread(id, currentBoard, null);
	}
	
	private static void openThread(String id, String board) {
		openThread(id, board, null);
	}

	private static void openThread(final String id, final String bd, final String post) {
		if(lastThread != null && lastThread.isAlive()) {
			lastThread.interrupt();
			lastThread = null;
		}
		if(threadFrm != null) {
			clearThreadData();
		}
		//System.out.println(id);
		currentThread = id;
		currentBoard = bd;
		currentPost = post;
		postsCount = -1;
		(lastThread = new Thread(new Jch(3))).start();
		
	}
	
	private static void loadThread(final String id, final String bd, final String post) {
		try {
			threadFrm = new Form("#" + id);
			threadFrm.addCommand(backCmd);
			threadFrm.setCommandListener(inst);
			display(threadFrm);
			addLoadingLabel(threadFrm);
			JSONObject j = null;
			
			JSONArray posts;
			try {
				if(post != null) {
					try {
						getResult("makaba/mobile.fcgi?task=get_thread&board="+bd+"&thread="+id+"&num="+post);
					} catch (IOException e) {
						if(e.getMessage().startsWith("404")) {
							getResult(bd + "/arch/res/" + id + ".json");
						} else throw e;
					}
					if(result == null || !(result instanceof JSONArray)) {
						throw new RuntimeException("Result not array: " + result);
					}
					posts = (JSONArray) result;

					getResult("api/mobile/v2/info/"+bd+"/"+id);
					if(result == null || !(result instanceof JSONObject)) {
						throw new RuntimeException("Result not object: " + result);
					}
					j = (JSONObject) result;
					j = j.getNullableObject("thread");
				} else {
					try {
						getResult(bd + "/res/" + id + ".json");
					} catch (IOException e) {
						if(e.getMessage().startsWith("404")) {
							getResult(bd + "/arch/res/" + id + ".json");
						} else throw e;
					}
					if(!(result instanceof JSONObject)) {
						if(result instanceof String && result.toString().startsWith("<!DOCTYPE html>")) {
							getResult(currentBoard + "/arch/res/" + id + ".json");
							if(!(result instanceof JSONObject)) {
								throw new Exception("404 Тред не найден");
							}
						} else {
							throw new RuntimeException("Result not object: " + result);
						}
					}
					if(result == null || !(result instanceof JSONObject))
						return;
					j = (JSONObject) result;
					//System.out.println(j);
					JSONArray th = j.getNullableArray("threads");
					if(th == null)
						return;
					JSONObject t = th.getObject(0);
					posts = t.getArray("posts");
				}
				threadFrm.addCommand(postCommentCmd);
				threadFrm.addCommand(threadGotoStartCmd);
				if(j != null) {
					threadFrm.setTitle("/" + bd + "/ - " + htmlText(j.getString("title", "")));
					postsCount = j.getInt("posts_count", j.getInt("posts", -1));
				}
				if(threadFrm != null) removeLoadingLabel(threadFrm);
				else return;
				parsePosts(threadFrm, posts, 0, false);
			} catch (InterruptedException e) {
			} catch (Throwable e) {
				e.printStackTrace();
				if(threadFrm != null) {
					removeLoadingLabel(threadFrm);
					addLoadingLabel(threadFrm, "Ошибка!");
					StringItem s = new StringItem(null, e.toString());
					s.setLayout(Item.LAYOUT_LEFT);
					threadFrm.append(s);
				}
			}
		} catch (Exception e) {
		}
	}

	private static void parsePosts(Form f, JSONArray posts, int offset, boolean search) {
		System.gc();
		int l = posts.size();
		if(offset != 0 && currentIndex != 0) {
			StringItem s = new StringItem(null, "");
			f.append(s);
			StringItem btn = new StringItem(null, "Предыдущие посты");
			btn.setLayout(Item.LAYOUT_CENTER);
			btn.addCommand(prevPostsItemCmd);
			btn.setDefaultCommand(prevPostsItemCmd);
			btn.setItemCommandListener(inst);
			f.append(btn);
			if(platform.indexOf("S60") > -1 && platform.indexOf("=3.2") == -1)
			try {
				display.setCurrentItem(s);
			} catch (Throwable e) {
			}
		}
		for(int i = 0; i < l /*&& i < 30*/; i++) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				return;
			}
			if(i + currentIndex + offset >= l || (postsCount > 0 ? i + currentIndex + offset >= postsCount : false)) {
				//System.out.println("limit: " + (i + currentIndex) + " " + postsCount);
				return;
			}
			if(i >= maxPostsCount) {
				if(offset == 0) {
					currentIndex = 0;
					cachedPosts = posts;
				}
				StringItem btn = new StringItem(null, "Следующие посты");
				btn.setLayout(Item.LAYOUT_CENTER);
				btn.addCommand(nextPostsItemCmd);
				btn.setDefaultCommand(nextPostsItemCmd);
				btn.setItemCommandListener(inst);
				f.append(btn);
				break;
			}
			try {
				JSONObject post = posts.getObject(i + currentIndex);
				JSONArray files = post.getNullableArray("files");
				//System.out.println(post);
				String num = post.getString("num", "");
				//System.out.println(post.toString());
				if(search) {
					StringItem title = new StringItem(null, htmlText(post.getString("name", "")).concat("\n").concat(time2ch ? post.getString("date", "") : parsePostDate(post.getLong("timestamp", 0))));
					title.setFont(smallBoldFont);
					title.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
					f.append(title);
	
					StringItem snum = new StringItem(null, " #".concat(num));
					snum.setFont(smallBoldFont);
					snum.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
					f.append(snum);
					snum.addCommand(postLinkItemCmd);
					snum.setDefaultCommand(postLinkItemCmd);
					snum.setItemCommandListener(inst);
					searchLinks.put(snum, "/".concat(currentBoard).concat("/res/").concat(post.getString("parent", "")).concat(".html#").concat(num));
				} else {
					StringItem title = new StringItem(null, "\n"
							.concat(htmlText(post.getString("name", ""))).concat("\n")
							.concat(time2ch ? post.getString("date", "") : parsePostDate(post.getLong("timestamp", 0)))
							.concat(" #").concat(num));
					title.setFont(smallBoldFont);
					title.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
					
					comments.put(num, title);
					f.append(title);
				}
				/*
				StringItem text = new StringItem(null, text(post.getString("comment", "")));
				text.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
				text.setLayout(Item.LAYOUT_NEWLINE_AFTER);
				threadFrm.append(text);
				*/
				parseHtmlText(f, post.getString("comment", ""));
				
				if(files != null) {
					int fl = files.size();
					for(int fi = 0; fi < fl && fi < 5; fi++) {
						JSONObject file = files.getObject(fi);
						String name = file.getString("displayname", file.getString("name", ""));
						ImageItem fitem = new ImageItem(name, null, 0, name);
						fitem.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
						fitem.addCommand(fileImgItemOpenCmd);
						fitem.setDefaultCommand(fileImgItemOpenCmd);
						fitem.setItemCommandListener(inst);
						addFile(fitem, file.getString("path", null), file.getString("thumbnail", null));
						f.append(fitem);
					}
				}
				Spacer s = new Spacer(2, 20);
				s.setLayout(Item.LAYOUT_2);
				f.append(s);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.toString());
			}
		}
	}

	private static String parsePostDate(long time) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time*1000));
		String s = n(c.get(Calendar.DAY_OF_MONTH)).concat(".")
				.concat(n(c.get(Calendar.MONTH)+1)).concat(".")
				.concat(i(c.get(Calendar.YEAR))).concat(" ")
				.concat(n(c.get(Calendar.HOUR_OF_DAY))).concat(":")
				.concat(n(c.get(Calendar.MINUTE)));
		//System.out.println(time + " -> " + s);
		return s;
	}
	
	private static String n(int n) {
		if(n < 10) {
			return "0".concat(Integer.toString(n));
		} else return i(n);
	}
	
	private static String i(int n) {
		return Integer.toString(n);
	}
	
	protected static void parseHtmlText(Form f, String s) {
		recursionParse(f, tidy.parseDOM("<html>".concat(s).concat("</html>")).getDocumentElement().getChildNodes());
		/*
		return;
		int ti;
		int tl;
		for (ti = 0, tl = s.length(); ti < tl && htmlRe.match(s, ti); ti = htmlRe.getParenEnd(0)) {
			String o = s.substring(ti, htmlRe.getParenStart(0));
			if(o != null && o.length() > 0) {
				o = htmlText(o);
				boolean b = o.endsWith(" ");
				if(b) o = o.substring(0, o.length() - 1);
				if(o.length() > 0) {
					StringItem textitem = new StringItem(null, o);
					textitem.setFont(smallPlainFont);
					textitem.setLayout(Item.LAYOUT_2);
					f.append(textitem);
				}
				if(b) {
					Spacer s2 = new Spacer(smallPlainFont.charWidth(' ') + 1, smallPlainFont.getHeight());
					s2.setLayout(Item.LAYOUT_2);
					f.append(s2);
				}
			}
			String w = htmlRe.getParen(0);
			if(w.startsWith("<a")) {
				// ссылка
				String t = htmlRe.getParen(2);
				String link = null;
				if(hrefRe.match(t)) {
					link = hrefRe.getParen(2);
					//System.out.println(link);
					link = htmlText(link);
				}
				String c = htmlRe.getParen(3);
				c = htmlText(c);
				StringItem linkitem = new StringItem(null, c);
				linkitem.setFont(smallUnderlinedFont);
				linkitem.setLayout(Item.LAYOUT_2);
				linkitem.addCommand(postLinkItemCmd);
				linkitem.setDefaultCommand(postLinkItemCmd);
				linkitem.setItemCommandListener(inst);
				f.append(linkitem);
				links.put(linkitem, link);
			} else if(w.startsWith("<strong>")) {
				// жирный текст
				String c = htmlRe.getParen(4);
				c = htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<b>")) {
				// жирный текст
				String c = htmlRe.getParen(5);
				c = htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<i>")) {
				// жирный текст
				String c = htmlRe.getParen(6);
				c = htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<em>")) {
				// курсивный текст
				String c = htmlRe.getParen(7);
				c = htmlText(c);
				StringItem textitem = new StringItem(null, c);
				textitem.setFont(smallItalicFont);
				textitem.setLayout(Item.LAYOUT_2);
				f.append(textitem);
			} else if(w.startsWith("<span")) {
				String t = htmlRe.getParen(8);
				String cls = null;
				if(classRe.match(t)) {
					cls = classRe.getParen(2);
				}
				String c = htmlRe.getParen(9);
				c = htmlText(c);
				StringItem textitem = new StringItem(null, c);
				textitem.setFont(smallPlainFont);
				textitem.setLayout(Item.LAYOUT_2);
				if(cls.equals("s")) {
					// зачеркнутый текст, но в lcdui такого стиля шрифта нету
				} else if(cls.equals("u")) {
					// подчеркнутый текст
					textitem.setFont(smallUnderlinedFont);
				} else if(cls.equals("o")) {
					// надчеркнутый текст
					textitem.setFont(smallUnderlinedFont);
				} else if(cls.equals("spoiler")) {
					// спойлер
					textitem.setText("[спойлер]");
					textitem.addCommand(postSpoilerItemCmd);
					textitem.setDefaultCommand(postSpoilerItemCmd);
					textitem.setItemCommandListener(inst);
					spoilers.put(textitem, c);
					//textitem.setFont(Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
				} else if(cls.equals("unkfunc")) {
					// цитата
				}
				f.append(textitem);
			} else if(w.startsWith("<h1>")) {
				String c = htmlRe.getParen(11);
				System.out.println("h " + c);
				c = htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(largeBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			}
		}
		if (ti < tl) {
			String o = s.substring(ti);
			if(o != null && !o.equals("")) {
				StringItem textitem = new StringItem(null, o = htmlText(o));
				textitem.setFont(smallPlainFont);
				textitem.setLayout(Item.LAYOUT_2);
				f.append(textitem);
			}
        }
        */
	}

	private static Font getFont(int i, int j, int k) {
		if(i == 0) {
			if(k == Font.SIZE_SMALL) {
				if(j == Font.STYLE_BOLD) {
					return smallBoldFont;
				}
				if(j == Font.STYLE_ITALIC) {
					return smallItalicFont;
				}
				if(j == Font.STYLE_PLAIN) {
					return smallPlainFont;
				}
				if(j == Font.STYLE_UNDERLINED) {
					return smallUnderlinedFont;
				}
			}
			if(k == Font.SIZE_LARGE) {
				if(j == Font.STYLE_BOLD) {
					return largeBoldFont;
				}
			}
		}
		return Font.getFont(i, j, k);
	}
	
	private static void recursionParse(Form f, NodeList nl) {
		int l = nl.getLength();
		for(int i = 0; i < l; i++) {
			Node n = nl.item(i);
			
			String k = n.getNodeName();
			if(k.equals("head")) {
				continue;
			}
			String v = n.getNodeValue();
			//System.out.println(k + " " + v);
			if(k.equals("br") || k.equals("p")) { //TODO: <p> tag parsing
				f.append("\n");
			}
			if(k.equals("#text")) {
				boolean b = true;
				int fstyle = Font.STYLE_PLAIN;
				int fsize = Font.SIZE_SMALL;
				int layout = 0;
				/*if(v.startsWith("\r\n")) {
					layout |= Item.LAYOUT_NEWLINE_BEFORE;
					v = v.substring(4);
				}*/
				v = replace(v, "\\r\\n", "\n");
				StringItem st = new StringItem(null, v);
				st.setLayout(Item.LAYOUT_2);
				boolean spoil = false;
				if(n.getParentNode() != null) {
					Node pn = n;
					String pk;
					while(!((pn = pn.getParentNode()) == null || (pk = pn.getNodeName()).equals("body"))) {
						
						//System.out.println(":PARENT NODE " + pk);
						if(pk.equals("a")) {
							String link = null;
							NamedNodeMap atr = pn.getAttributes();
							if(atr.getNamedItem("href") != null) {
								link = atr.getNamedItem("href").getNodeValue();
							}
							if(!spoil) {
								st.addCommand(postLinkItemCmd);
								st.setDefaultCommand(postLinkItemCmd);
								st.setItemCommandListener(inst);
							}
							links.put(st, link);
							continue;
						}
						if(pk.equals("span")) {
							spoil = true;
							String cls = null;
							NamedNodeMap atr = pn.getAttributes();
							if(atr.getNamedItem("class") != null) {
								cls = atr.getNamedItem("class").getNodeValue();
							}
							if(cls.equals("u") || cls.equals("o")) {
								fstyle |= Font.STYLE_UNDERLINED;
							}
							if(cls.equals("spoiler")) {
								st.setText("[спойлер]");
								st.addCommand(postSpoilerItemCmd);
								st.setDefaultCommand(postSpoilerItemCmd);
								st.setItemCommandListener(inst);
								spoilers.put(st, v);
							}
							continue;
						}
						if(pk.equals("b") || pk.equals("strong") || (pk.length() == 2 && pk.startsWith("h"))) {
							fstyle |= Font.STYLE_BOLD;
							if(pk.equals("h1")) {
								fsize = Font.SIZE_LARGE;
							} else if(pk.equals("h2")) {
								fsize = Font.SIZE_MEDIUM;
							}
							continue;
						}
						if(pk.equals("em") || pk.equals("i")) {
							fstyle |= Font.STYLE_ITALIC;
							continue;
						}
						if(pk.equals("sub")) {
							fstyle |= Font.STYLE_UNDERLINED;
							continue;
						}
					}
				}
				if(v.endsWith(" ") && !spoil) {
					st.setText(v.substring(0, v.length()-1));
					f.append(st);
					b = false;
					Spacer s2 = new Spacer(smallPlainFont.charWidth(' ') + 1, smallPlainFont.getHeight());
					s2.setLayout(Item.LAYOUT_2);
					f.append(s2);
				}
				st.setLayout(Item.LAYOUT_2 | layout);
				Font font = getFont(0, fstyle, fsize);
				st.setFont(font);
				
				if(b) f.append(st);
			} else if(n.hasChildNodes()) {
				recursionParse(f, n.getChildNodes());
			}
		}
	}

	protected static void addFile(ImageItem img, String path, String thumb) {
		files.put(img, path);
		if(filePreview) {
			thumbsToLoad.addElement(new Object[] { thumb, img });
			synchronized(thumbLoadLock) {
				thumbLoadLock.notifyAll();
			}
		}
	}

	protected static void display(Displayable f) {
		display.setCurrent(f);
	}

	private static void board(String txt) {
		txt = txt.toLowerCase();
		if(txt.length() <= 0)
			return;
		if(txt.startsWith("/"))
			txt = txt.substring(1);
		if(txt.endsWith("/"))
			txt = txt.substring(0, txt.length() - 1);
		final String board = txt;
		currentBoard = board;
		//System.out.println(board);
		createBoard(board);
	}
	
	private static void createBoard(final String board) {
		boardFrm = new Form("Jch - /" + board + "/");
		boardFrm.addCommand(backCmd);
		boardFrm.addCommand(postThreadCmd);
		boardFrm.setCommandListener(inst);
		boardFrm.append(boardSearchField = new TextField("Поиск", "", 1000, TextField.ANY));
		boardSearchField.addCommand(boardSearchItemCmd);
		boardSearchField.setItemCommandListener(inst);
		addLoadingLabel(boardFrm);
		display.setCurrent(boardFrm);
		/*StringItem btn = new StringItem("Поиск", "", StringItem.BUTTON);
		btn.setDefaultCommand(boardSearchCmd);
		btn.setItemCommandListener(inst);
		boardFrm.append(btn);*/
		currentBoard = board;
		(lastThread = new Thread(new Jch(4))).start();
	}
	
	private static void loadBoard(final String board) {
		JSONObject j = null;
		try {
			getResult(board + "/threads.json");
			if(!(result instanceof JSONObject))
				throw new RuntimeException("Result not object: " + result);
			j = (JSONObject) result;
			if(j == null || !(result instanceof JSONObject))
				return;
			JSONArray th = j.getNullableArray("threads");
			if(th == null)
				return;
			j = null;
			int l = th.size();
			removeLoadingLabel(boardFrm);
			for(int i = 0; i < l && i < 20; i++) {
				JSONObject thread = th.getObject(i);
				//System.out.println(thread);
				StringItem s = new StringItem("#" + thread.getString("num", ""),
						htmlText(thread.getString("subject", "")));
				s.addCommand(openThreadCmd);
				s.setDefaultCommand(openThreadCmd);
				s.setItemCommandListener(inst);
				boardFrm.append(s);
				//boardFrm.append(text(comment) + "\n");
				Thread.yield();
			}
			th = null;
			System.gc();
		} catch (InterruptedException e) {
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				removeLoadingLabel(boardFrm);
				addLoadingLabel(boardFrm, "Ошибка!");
				StringItem s = new StringItem(null, e.toString());
				s.setLayout(Item.LAYOUT_LEFT);
				boardFrm.append(s);
			} catch (NullPointerException e2) {
			}
		}
	}

	private static void addLoadingLabel(Form f) {
		addLoadingLabel(f, "Загрузка...");
	}

	private static void addLoadingLabel(Form f, String s) {
		if(f == null) return;
		tempLoadingLabel = new StringItem(null, s);
		tempLoadingLabel.setFont(smallPlainFont);
		tempLoadingLabel.setLayout(Item.LAYOUT_CENTER);
		loadingLabelIndex = f.append(tempLoadingLabel);
	}

	private static void removeLoadingLabel(Form f) {
		if(tempLoadingLabel != null && loadingLabelIndex != -1) {
			f.delete(loadingLabelIndex);
			tempLoadingLabel = null;
			loadingLabelIndex = -1;
		}
	}

	public static Image getImg(String url) throws IOException {
		try {
			return getImg(url, direct2ch ? null : previewProxyUrl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Image getImg(String url, String proxy) throws IOException {
		url = prepareUrl(url, proxy, instanceUrl);
		byte[] b = get(url);
		return Image.createImage(b, 0, b.length);
	}

	public static void getResult(String s, String proxy, String inst) throws Exception {
		//System.out.println(s);
		result = null;
		String s2 = s;
		result = getString(prepareUrl(s, proxy, inst));
		if(result.toString().length() == 0) throw new IOException("Empty response: " + s2);
		char c = ((String) result).charAt(0);
		if(c == '{')
			result = getObject((String) result);
		else if(c == '[')
			result = getArray((String) result);
	}

	public static void getResult(String s) throws Exception {
		getResult(s, direct2ch ? null : apiProxyUrl, instanceUrl);
	}

	public static String prepareUrl(String url, String proxy, String inst) {
		if(url.startsWith("/")) url = url.substring(1);
		if(url.endsWith("&") || url.endsWith("?"))
			url = url.substring(0, url.length() - 1);
		if(proxy != null && proxy.length() > 1) {
			url = proxy + encodeUrl("https://" + inst + "/" + url);
		} else {
			url = "https://" + inst + "/" +url;
		}
		return url;
	}

	public static void destroyApp() {
		running = false;
		midlet.notifyDestroyed();
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
			} else if (ch == '/') {
				sbuf.append("%2F");
			} else if (ch == ':') {
				sbuf.append("%3A");
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
		return sbuf.toString();
	}

	private static String hex(int ch) {
		String s = Integer.toHexString(ch);
		return "%" + (s.length() < 2 ? "0" : "") + s;
	}

	public static byte[] get(String url) throws IOException {
		//System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) open(url, Connector.READ);

		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			if(cookie != null) {
				hc.setRequestProperty("Cookie", cookie);
			}
			hc.setRequestProperty("User-Agent", "JCh/" + version() + " (" + platform + ")");
			//hc.setRequestProperty("Accept-Encoding", "identity");
			int r = hc.getResponseCode();
			if(r >= 400 && r != 401) throw new IOException(r + " " + hc.getResponseMessage());
			int redirects = 0;
			while (r == 301 || r == 302 || r == 303) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String tmp = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + tmp.substring(0, tmp.indexOf("/"));
					redir = host + redir;
				}
				hc.close();
				hc = (HttpConnection) open(redir, Connector.READ);
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
						//System.out.println(k + ": " + v);
						//if(v.indexOf("code_auth=") != -1) {
						String[] f = split(v, ';');
						String s = f[0];
						if(s.startsWith(" ")) s = s.substring(1);
						addCookie(s);
						//}
					}
				}
			}
			if(r == 401) throw new IOException(r + " " + hc.getResponseMessage());
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
	
	private static void addCookie(String s) {
		//System.out.println("addCookie " + s);
		
	}

	public static byte[] post(String url, String content) throws IOException {
		//System.out.println("POST " + url);
		//System.out.println(content);
		HttpConnection hc = (HttpConnection) open(url, Connector.READ_WRITE);

		InputStream is = null;
		OutputStream os = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("POST");
			if(cookie != null) {
				hc.setRequestProperty("Cookie", cookie);
			}
			//hc.setRequestProperty("Accept-Encoding", "identity");
			byte[] b = content.getBytes("UTF-8");
			int l = b.length;
			hc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			hc.setRequestProperty("Content-Length", "" + l);
			os = hc.openOutputStream();
			os.write(b);
			os.close();
			//os.flush();
			int r = hc.getResponseCode();
			if(r >= 400) throw new IOException(r + " " + hc.getResponseMessage());
			int redirects = 0;
			while (r == 301 || r == 302 || r == 303) {
				String redir = hc.getHeaderField("Location");
				if (redir.startsWith("/")) {
					String tmp = url.substring(url.indexOf("//") + 2);
					String host = url.substring(0, url.indexOf("//")) + "//" + tmp.substring(0, tmp.indexOf("/"));
					redir = host + redir;
				}
				hc.close();
				//System.out.println("redirect: " + r + " " + redir);
				hc = (HttpConnection) open(redir, Connector.READ);
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
					//System.out.println(k + ": " + v);
					if(k.equalsIgnoreCase("set-cookie")) {
						if(v.indexOf("code_auth=") != -1) {
							String[] f = split(v, ';');
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
	
	public static ContentConnection open(String url, int i) throws IOException {
		try {
			ContentConnection con = (ContentConnection) Connector.open(url, i);
			//if (con instanceof HttpConnection)
			//	((HttpConnection) con).setRequestProperty("User-Agent", "JCh/" + JChMIDlet.version() + " (" + platform + ")");
			return con;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
			//throw new IOException(cut(cut(e.toString(), "Exception"), "java.io.") + " " + url);
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
	
	public final static Object null_equivalent = new NullEquivalent();
	
	private static final Boolean TRUE = new Boolean(true);
	private static final Boolean FALSE = new Boolean(false);

	public static JSONObject getObject(String string) throws Exception {
		if (string == null || string.length() <= 1)
			throw new Exception("JSON: Empty string");
		if (string.charAt(0) != '{')
			throw new Exception("JSON: Not JSON object");
		return (JSONObject) parseJSON(string);
	}

	public static JSONArray getArray(String string) throws Exception {
		if (string == null || string.length() <= 1)
			throw new Exception("JSON: Empty string");
		if (string.charAt(0) != '[')
			throw new Exception("JSON: Not JSON array");
		return (JSONArray) parseJSON(string);
	}

	public static Object getJSON(Object obj) throws Exception {
		if (obj instanceof Hashtable) {
			return new JSONObject((Hashtable) obj);
		} else if (obj instanceof Vector) {
			return new JSONArray((Vector) obj);
		} else {
			return obj;
		}
	}

	public static Object parseJSON(String str) throws Exception {
		if (str == null || str.equals(""))
			throw new Exception("JSON: Empty string");
		if (str.length() < 2) {
			return str;
		} else {
			str = str.trim();
			char first = str.charAt(0);
			char last = str.charAt(str.length() - 1);
			if (first == '{' && last != '}' || first == '[' && last != ']' || first == '"' && last != '"') {
				throw new Exception("JSON: Unexpected end of text");
			} else if (first == '"') {
				// String
				str = str.substring(1, str.length() - 1);
				char[] chars = str.toCharArray();
				str = null;
				try {
					int l = chars.length;
					StringBuffer sb = new StringBuffer();
					int i = 0;
					// Parse string escape chars
					loop: {
						while (i < l) {
							char c = chars[i];
							switch (c) {
							case '\\': {
								next: {
									replaced: {
										if(l < i + 1) {
											sb.append(c);
											break loop;
										}
										char c1 = chars[i + 1];
										switch (c1) {
										case 'u':
											i+=2;
											String u = "" + chars[i++] + chars[i++] + chars[i++] + chars[i++];
											sb.append((char) Integer.parseInt(u, 16));
											break replaced;
										case 'x':
											i+=2;
											String x = "" + chars[i++] + chars[i++];
											sb.append((char) Integer.parseInt(x, 16));
											break replaced;
										case 'n':
											sb.append('\n');
											i+=2;
											break replaced;
										case 'r':
											sb.append('\r');
											i+=2;
											break replaced;
										case 't':
											sb.append('\t');
											i+=2;
											break replaced;
										case 'f':
											sb.append('\f');
											i+=2;
											break replaced;
										case 'b':
											sb.append('\b');
											i+=2;
											break replaced;
										case '\"':
										case '\'':
										case '\\':
										case '/':
											i+=2;
											sb.append((char) c1);
											break replaced;
										default:
											break next;
										}
									}
									break;
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
			} else if (first != '{' && first != '[') {
				if (str.equals("null"))
					return null_equivalent;
				if (str.equals("true"))
					return TRUE;
				if (str.equals("false"))
					return FALSE;
				if(str.charAt(0) == '0' && str.charAt(1) == 'x') {
					try {
						return new Integer(Integer.parseInt(str.substring(2), 16));
					} catch (Exception e) {
						try {
							return new Long(Long.parseLong(str.substring(2), 16));
						} catch (Exception e2) {
							// Skip
						}
					}
				}
				/*
				try {
					return Integer.valueOf(str);
				} catch (Exception e) {
					try {
						return new Long(Long.parseLong(str));
					} catch (Exception e2) {
						try {
							return Double.valueOf(str);
						} catch (Exception e3) {
						}
					}
				}*/
				/*
				if(str.length() == 0 || str.equals("") || str.equals(" "))
					throw new Exception("JSON: Empty value");
				throw new Exception("JSON: Unknown value: " + str);
				*/
				return str;
			} else {
				// Parse json object or array
				int unclosed = 0;
				boolean object = first == '{';
				int i = 1;
				int length = str.length() - 1;
				char nextDelimiter = object ? ':' : ',';
				boolean escape = false;
				String key = null;
				Object res = null;
				if (object) res = new Hashtable();
				else res = new Vector();
				
				for (int splIndex; i < length; i = splIndex + 1) {
					// skip all spaces
					for (; i < length - 1 && str.charAt(i) <= ' '; i++);

					splIndex = i;
					boolean quotes = false;
					for (; splIndex < length && (quotes || unclosed > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
						char c = str.charAt(splIndex);
						if (!escape) {
							if (c == '\\') {
								escape = true;
							} else if (c == '"') {
								quotes = !quotes;
							}
						} else escape = false;
		
						if (!quotes) {
							if (c == '{' || c == '[') {
								unclosed++;
							} else if (c == '}' || c == ']') {
								unclosed--;
							}
						}
					}

					if (quotes || unclosed > 0) {
						throw new Exception("JSON: Corrupted JSON");
					}

					if (object && key == null) {
						key = str.substring(i, splIndex);
						//while(n.startsWith("\r") || n.startsWith("\n")) {
						//	n = n.substring(1);
						//}
						//while(n.endsWith("\r") || n.endsWith("\n") || n.endsWith(" ")) {
						//	n = n.substring(0, n.length() - 1);
						//}
						key = key.substring(1, key.length() - 1);
						nextDelimiter = ',';
					} else {
						String s = str.substring(i, splIndex);
						while (s.endsWith("\r") || s.endsWith("\n")) {
							s = s.substring(0, s.length() - 1);
						}
						Object value = s.trim();
						//if (parse_members) value = parseJSON(value.toString());
						if (object) {
							((Hashtable) res).put(key, value);
							key = null;
							nextDelimiter = ':';
						} else if (splIndex > i) ((Vector) res).addElement(value);
					}
				}
				return getJSON(res);
			}
		}
	}
	
	public static boolean isNull(Object obj) {
		return null_equivalent.equals(obj);
	}

	public static String escape_utf8(String s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < len) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				sb.append("\\" + c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				if (c < 32 || c > 1103) {
					String u = Integer.toHexString(c);
					for (int z = u.length(); z < 4; z++) {
						u = "0" + u;
					}
					sb.append("\\u" + u);
				} else {
					sb.append(c);
				}
			}
			i++;
		}
		return sb.toString();
	}

	public static Double getDouble(Object o) throws Exception {
		try {
			if (o instanceof Short)
				return new Double(((Short)o).shortValue());
			else if (o instanceof Integer)
				return new Double(((Integer)o).intValue());
			else if (o instanceof Long)
				return new Double(((Long)o).longValue());
			else if (o instanceof Double)
				return (Double) o;
			//else if (o instanceof Float)
			//	return new Double(((Float)o).doubleValue());
			else if (o instanceof String)
				return Double.valueOf((String) o);
		} catch (Throwable e) {
		}
		throw new Exception("JSON: Value cast failed: " + o);
	}
}
