/*
Copyright (c) 2021-2024 Arman Jussupgaliyev
*/
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
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
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

import dom.NamedNodeMap;
import dom.Node;
import dom.NodeList;
import tidy.Tidy;

public class Jch extends MIDlet implements CommandListener, ItemCommandListener, ItemStateListener, Runnable {
	
	private static final String aboutText =
			  "<h1>JCh</h1><br>"
			+ "версия <ver><br><br>"
			+ "Клиент <a href=\"https://2ch.hk\">2ch.hk</a> для Symbian/J2ME устройств<br><br>"
			+ "<b>Разработал</b><br>"
			+ "Shinovon (<a href=\"http://nnp.nnchan.ru\">nnproject</a>)<br><br>"
			+ "<b>Использованые библиотеки</b><br>"
			+ "org.w3c.dom<br>"
			+ "org.w3c.tidy<br>"
			+ "cc.nnproject.json<br><br>";
	
	private static final String CONFIG_RECORD_NAME = "jchconfig";
	private static final String DEFAULT_INSTANCE_URL = "2ch.hk";
	private static final String DEFAULT_PROXY_URL = "http://nnp.nnchan.ru:80/2chproxy.php";

	private static final Command exitCmd = new Command("Выход", Command.EXIT, 0);
	private static final Command backCmd = new Command("Назад", Command.BACK, 0);
	private static final Command boardFieldCmd = new Command("Открыть", Command.OK, 0);
	private static final Command openThreadCmd = new Command("Открыть тред", Command.ITEM, 0);
	private static final Command fileImgItemOpenCmd = new Command("Открыть файл", Command.ITEM, 0);
	private static final Command postLinkItemCmd = new Command("Открыть ссылку", Command.ITEM, 0);
	private static final Command postSpoilerItemCmd = new Command("Показать спойлер", Command.ITEM, 0);
	private static final Command nextPostsItemCmd = new Command("След. посты", Command.ITEM, 0);
	private static final Command prevPostsItemCmd = new Command("Пред. посты", Command.ITEM, 0);
	private static final Command boardsItemCmd = new Command("Борды", Command.ITEM, 0);
	private static final Command openByLinkItemCmd = new Command("Открыть по ссылке", Command.ITEM, 0);
	private static final Command threadGotoStartCmd = new Command("Перейти к началу треда", Command.SCREEN, 0);
	private static final Command nextThreadsItemCmd = new Command("След. треды", Command.ITEM, 0);
	private static final Command prevThreadsItemCmd = new Command("Пред. треды", Command.ITEM, 0);
	private static final Command aboutCmd = new Command("О программе", Command.SCREEN, 0);
	private static final Command settingsCmd = new Command("Настройки", Command.SCREEN, 0);
	private static final Command textOkCmd = new Command("Ок", Command.OK, 0);
	private static final Command linkOkCmd = new Command("Ок", Command.OK, 0);
	private static final Command importUsercodeCmd = new Command("Импортировать юзеркод", Command.SCREEN, 0);
	
	private static final int RUN_BOARDS = 1;
	private static final int RUN_THREAD = 3;
	private static final int RUN_BOARD = 4;
	private static final int RUN_THUMBNAILS = 5;
	
	private static Display display;
	private static Jch midlet;

	private static boolean started;

	private static Font largeBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
	private static Font smallPlainFont = Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private static Font smallBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL);
	private static Font smallItalicFont = Font.getFont(0, Font.STYLE_ITALIC, Font.SIZE_SMALL);
	private static Font smallUnderlinedFont = Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);

	private static Tidy tidy;
	
	private static Form mainFrm;
	private static Form boardFrm;
	private static Form threadFrm;
	private static Form aboutFrm;
	private static List boardsList;
	private static Form settingsFrm;
	
	private static TextField boardField;
	
	private static TextField setInstanceField;
	private static TextField setApiProxyField;
	private static TextField setPreviewProxyField;
	private static TextField setFileProxyField;
	private static ChoiceGroup setChoice;
	private static Gauge setMaxPostsGauge;
	
	private static String currentBoard;
	private static String currentThread;

	private static String currentPost;

	private static boolean running = true;
	
	private static StringItem tempLoadingLabel;
	private static int loadingLabelIndex = -1;
	
	private static Hashtable files = new Hashtable();
	private static Hashtable links = new Hashtable();
	private static Hashtable spoilers = new Hashtable();
	private static Hashtable comments = new Hashtable();
	
	private static Thread lastThread;
	
	private static Object thumbLoadLock = new Object();
	private static Vector thumbsToLoad = new Vector();

	private static JSONArray cachedPosts;
	private static int postsCount;
	private static int currentIndex;
	
	private static JSONArray cachedThreads;
	private static int threadsCount;
	private static int catalogIndex;

	// Settings
	private static boolean direct2ch;
	private static String instanceUrl = DEFAULT_INSTANCE_URL;
	private static String apiProxyUrl = DEFAULT_PROXY_URL;
	private static int maxPostsCount = 10;
	private static boolean time2ch;
	private static boolean filePreview = true;
	private static boolean directFile;
	private static boolean simpleThreads = false;

	private static String platform;
	private static String version;
	
	private static Hashtable cookies;
	private static String cookiesStr;
	
	private int run = 0;

	public Jch() {
		midlet = this;
	}

	public void run() {
		int run;
		synchronized(this) {
			run = this.run;
			notify();
		}
		switch(run) {
		case RUN_BOARDS: {
			List list = boardsList;
			boardsList = null;
			try {
				list.setTitle("Загрузка");
				Object result = getResult("api/mobile/v2/boards");
				if(!(result instanceof JSONArray)) {
					throw new RuntimeException("Result not array: " + result);
				}
				list.setTitle("Борды");
				int i = 0;
				int l = ((JSONArray)result).size();
				while(i < l) {
					JSONObject board = ((JSONArray)result).getObject(i++);
					String id = board.getNullableString("id");
					String name = board.getNullableString("name");
					list.append("/".concat(id).concat("/ ").concat(name), null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				list.setTitle("Ошибка");
				display(warningAlert(e.toString()));
			}
			return;
		}
		case RUN_THREAD:
			String id = currentThread;
			String bd = currentBoard;
			String post = currentPost;
			try {
				Form f = new Form("#" + id);
				f.addCommand(backCmd);
				f.setCommandListener(midlet);
				display(threadFrm = f);
				addLoadingLabel(f, "Загрузка...");
				JSONObject j = null;
				
				JSONArray posts;
				try {
					Object result;
					if(post != null) {
						try {
							result = getResult("makaba/mobile.fcgi?task=get_thread&board="+bd+"&thread="+id+"&num="+post);
						} catch (IOException e) {
							if(e.getMessage().startsWith("404")) {
								result = getResult(bd + "/arch/res/" + id + ".json");
							} else throw e;
						}
						if(result == null || !(result instanceof JSONArray)) {
							throw new RuntimeException("Result not array: " + result);
						}
						posts = (JSONArray) result;

						result = getResult("api/mobile/v2/info/"+bd+"/"+id);
						if(result == null || !(result instanceof JSONObject)) {
							throw new RuntimeException("Result not object: " + result);
						}
						j = (JSONObject) result;
						j = j.getNullableObject("thread");
					} else {
						try {
							result = getResult(bd + "/res/" + id + ".json");
						} catch (IOException e) {
							if(e.getMessage().startsWith("404")) {
								result = getResult(bd + "/arch/res/" + id + ".json");
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
					//threadFrm.addCommand(postCommentCmd);
					f.addCommand(threadGotoStartCmd);
					if(j != null) {
						f.setTitle("/".concat(bd).concat("/ - ").concat(htmlText(j.getString("title", ""))));
						postsCount = j.getInt("posts_count", j.getInt("posts", -1));
					}
					removeLoadingLabel(f);
					if(threadFrm == null)
						return;
					parsePosts(f, posts, false, false, false);
				} catch (InterruptedException e) {
				} catch (Throwable e) {
					e.printStackTrace();
					if(f != null) {
						removeLoadingLabel(f);
						addLoadingLabel(f, "Ошибка!");
						StringItem s = new StringItem(null, e.toString());
						s.setLayout(Item.LAYOUT_LEFT);
						f.append(s);
					}
				}
			} catch (Exception e) {
			}
			return;
		case RUN_BOARD: {
			JSONObject j = null;
			Form f = boardFrm;
			try {
				Object result = getResult(currentBoard + (simpleThreads ? "/threads.json" : "/catalog.json"));
				if(!(result instanceof JSONObject))
					throw new RuntimeException("Result not object: " + result);
				j = (JSONObject) result;
				if(j == null || !(result instanceof JSONObject))
					return;
				JSONArray th = j.getNullableArray("threads");
				if(th == null)
					return;
				j = null;
				removeLoadingLabel(f);
				if(boardFrm == null) return;
				if(simpleThreads) {
					int l = th.size();
					for(int i = 0; i < l && i < 20; i++) {
						JSONObject thread = th.getObject(i);
						StringItem s = new StringItem("#" + thread.getString("num", ""),
								htmlText(thread.getString("subject", "")));
						s.addCommand(openThreadCmd);
						s.setDefaultCommand(openThreadCmd);
						s.setItemCommandListener(midlet);
						f.append(s);
					}
				} else {
					threadsCount = -1;
					catalogIndex = 0;
					parsePosts(f, th, false, false, true);	
				}
			} catch (InterruptedException e) {
			} catch (Throwable e) {
				e.printStackTrace();
				try {
					removeLoadingLabel(f);
					addLoadingLabel(f, "Ошибка!");
					StringItem s = new StringItem(null, e.toString());
					s.setLayout(Item.LAYOUT_LEFT);
					f.append(s);
				} catch (NullPointerException e2) {
				}
			}
			return;
		}
		case RUN_THUMBNAILS: {
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
			return;
		}
		default:
			break;
		}
	}
	
	private static void start(int i) {
		try {
			Jch j = midlet;
			synchronized(j) {
				j.run = i;
				(lastThread = new Thread(j)).start();
				j.wait();
			}
		} catch (Exception e) {}
	}
	
	protected void destroyApp(boolean b) {
		Jch.destroyApp();
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		if(started)
			return;
		started = true;
		display = Display.getDisplay(this);
		platform = System.getProperty("microedition.platform");
		try {
			RecordStore r = RecordStore.openRecordStore("jchcookie", false);
			byte[] b = r.getRecord(1);
			r.closeRecordStore();
			cookies = new Hashtable();
			String str = new String(b);
			int index = str.indexOf(';');
			boolean n = true;
			while (n) {
				if (index == -1) {
					n = false;
					index = str.length();
				}
				String token = str.substring(0, index).trim();
				int index2 = token.indexOf('=');
				cookies.put(token.substring(0, index2).trim(), token.substring(index2 + 1));
				if (n) {
					str = str.substring(index + 1);
					index = str.indexOf(';');
				}
			}
		} catch (Exception e) {
		}
		cookCookies();
		tidy = new Tidy();
		Form f = new Form("Jch - Главная");
		f.setCommandListener(midlet);
		f.addCommand(exitCmd);
		f.addCommand(aboutCmd);
		f.addCommand(settingsCmd);
		f.append(boardField = new TextField("Доска", "", 5, TextField.ANY));
		boardField.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_2);
		boardField.addCommand(boardFieldCmd);
		boardField.setItemCommandListener(midlet);
		StringItem btn = new StringItem(null, "Ввод", StringItem.BUTTON);
		btn.setDefaultCommand(boardFieldCmd);
		btn.setItemCommandListener(midlet);
		f.append(btn);
		StringItem btn2 = new StringItem(null, "Борды", StringItem.BUTTON);
		btn2.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_2);
		btn2.setDefaultCommand(boardsItemCmd);
		btn2.setItemCommandListener(midlet);
		f.append(btn2);
		StringItem btn3 = new StringItem(null, "Открыть пост по ссылке", StringItem.BUTTON);
		btn3.setLayout(Item.LAYOUT_EXPAND | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_2);
		btn3.setDefaultCommand(openByLinkItemCmd);
		btn3.setItemCommandListener(midlet);
		f.append(btn3);
		if(version == null)
			version = midlet.getAppProperty("MIDlet-Version");
		try {
			RecordStore r = RecordStore.openRecordStore(CONFIG_RECORD_NAME, false);
			JSONObject j = getObject(new String(r.getRecord(1), "UTF-8"));
			r.closeRecordStore();
			direct2ch = j.getBoolean("direct", direct2ch);
			instanceUrl = j.getString("instance", instanceUrl);
			apiProxyUrl = j.getString("apiproxy", apiProxyUrl);
			time2ch = j.getBoolean("time2ch", time2ch);
			maxPostsCount = j.getInt("maxposts", maxPostsCount);
			filePreview = j.getBoolean("filepreview", filePreview);
			directFile = j.getBoolean("directfile", directFile);
			simpleThreads = j.getBoolean("simplethreads", simpleThreads);
		} catch (Exception e) {}
		// авто замена прокси для старых дев билдов
		if(apiProxyUrl == null || apiProxyUrl.length() < 2 || apiProxyUrl.endsWith("=")) {
			apiProxyUrl = DEFAULT_PROXY_URL;
		}
		start(RUN_THUMBNAILS);
		// предупреждалка для эмуляторов
		// потому что в эмуляторах оно действительно выглядит как говно
		String s = System.getProperty("os.name");
		if(s != null) {
			s = s.toLowerCase();
			if(s.indexOf("nux") != -1 || s.indexOf("win") != -1 || s.indexOf("mac") != -1) {
				f.append("\n\nВ эмуляторах содержимое постов может отображаться некорректно!");
			}
		}
		display(mainFrm = f);
		
	}

	public void commandAction(Command c, Displayable d) {
		if(c == exitCmd) {
			destroyApp();
			return;
		}
		if(c == backCmd) {
			if(d == boardFrm) {
				display.setCurrent(mainFrm);
				boardFrm.deleteAll();
				boardFrm = null;
				return;
			}
			if(d == threadFrm) {
				display.setCurrent(boardFrm != null ? boardFrm : mainFrm);
				if(lastThread != null && lastThread.isAlive()) {
					lastThread.interrupt();
					lastThread = null;
				}
				threadFrm.deleteAll();
				clearThreadData();
				threadFrm = null;
				currentThread = null;
				System.gc();
				return;
			}
			if(d == aboutFrm) {
				display.setCurrent(mainFrm);
				clearThreadData();
				aboutFrm = null;
				return;
			}
			if(d instanceof List) {
				display.setCurrent(mainFrm);
				boardsList = null;
				return;
			}
			if(d == settingsFrm) {
				direct2ch = setChoice.isSelected(0);
				directFile = setChoice.isSelected(1);
				time2ch = setChoice.isSelected(2);
				filePreview = setChoice.isSelected(3);
				simpleThreads = setChoice.isSelected(4);
				instanceUrl = setInstanceField.getString();
				apiProxyUrl = setApiProxyField.getString();
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
					j.put("time2ch", new Boolean(time2ch));
					j.put("maxposts", new Integer(maxPostsCount));
					j.put("filepreview", new Boolean(filePreview));
					j.put("directfile", new Boolean(directFile));
					j.put("simplethreads", new Boolean(simpleThreads));
					byte[] b = j.build().getBytes("UTF-8");
					
					r.addRecord(b, 0, b.length);
					r.closeRecordStore();
				} catch (Exception e) {
				}
				return;
			}
			if(d instanceof TextBox) {
				display.setCurrent(mainFrm);
				return;
			}
			display.setCurrent(settingsFrm);
			return;
		}
		if(c == aboutCmd) {
			Form f = new Form("Jch - О программе");
			f.addCommand(backCmd);
			f.setCommandListener(midlet);
			StringItem s;
			f.append(s = new StringItem(null, ""));
			parseHtmlText(f, replace(aboutText, "<ver>", version));
			display(aboutFrm = f);
			if(platform.indexOf("S60") > -1 && platform.indexOf("=3.2") == -1)
			try {
				display.setCurrentItem(s);
			} catch (Throwable e) {
			}
			return;
		}
		if(c == List.SELECT_COMMAND) {
			int i = ((List)d).getSelectedIndex();
			if(i == -1) return;
			board(split(((List)d).getString(i), '/')[1]);
			return;
		}
		if(c == settingsCmd) {
			if(settingsFrm == null) {
				Form f = new Form("Jch - Настройки");
				f.addCommand(backCmd);
				f.addCommand(importUsercodeCmd);
				f.setCommandListener(midlet);
				f.setItemStateListener(midlet);
				f.append(setInstanceField = new TextField("Инстанс двача", instanceUrl, 100, TextField.URL));
				(setChoice = new ChoiceGroup("", Choice.MULTIPLE,
						new String[] { "Прямое подключение", "Открывать файлы напрямую", "Дата поста с сайта", "Отображение превью", "Простой каталог" },
						null))
				.setSelectedFlags(new boolean[] { direct2ch, directFile, time2ch, filePreview, simpleThreads });
				f.append(setChoice);
				f.append(setMaxPostsGauge = new Gauge("Кол-во постов на странице", true, 30, maxPostsCount));
				f.append(setApiProxyField = new TextField("Прокси", apiProxyUrl, 100, direct2ch ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL));
				settingsFrm = f;
			}
			display(settingsFrm);
			return;
		}
		if(c == textOkCmd && d instanceof TextBox) {
			if(cookies == null)
				cookies = new Hashtable();
			cookies.put("usercode_auth", ((TextBox)d).getString());
			saveCookies();
			return;
		}
		if(c == threadGotoStartCmd) {
			openThread(currentThread, currentBoard, null);
			return;
		}
		if(c == linkOkCmd) {
			String s = ((TextBox)d).getString();
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
			return;
		}
		if(c == importUsercodeCmd) {
			final TextBox t = new TextBox("Импорт юзеркода", "", 100, TextField.ANY);
			t.addCommand(backCmd);
			t.addCommand(textOkCmd);
			t.setCommandListener(midlet);
			display(t);
			return;
		}
	}

	private static String sub(String s, String sub) {
		if(s.startsWith(sub)) {
			s = s.substring(sub.length());
		}
		return s;
	}

	public void itemStateChanged(Item item) {
		if(item == setChoice) {
			direct2ch = setChoice.isSelected(0);
			directFile = setChoice.isSelected(1);
			time2ch = setChoice.isSelected(2);
			filePreview = setChoice.isSelected(3);
			simpleThreads = setChoice.isSelected(4);
			int c = direct2ch ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL;
			setApiProxyField.setConstraints(c);
			setPreviewProxyField.setConstraints(c);
			setFileProxyField.setConstraints(directFile ? (TextField.URL | TextField.UNEDITABLE) : TextField.URL);
		}
	}
	
	// отчистка всего говна от тредов и "хтмл парса"
	private static void clearThreadData() {
		files.clear();
		links.clear();
		spoilers.clear();
		comments.clear();
		thumbsToLoad.removeAllElements();
	}

	public void commandAction(Command c, Item item) {
		if(c == boardFieldCmd) {
			board(boardField.getString());
			return;
		}
		if(c == openThreadCmd) {
			// открытие треда
			openThread(item.getLabel() == null ? ((StringItem)item).getText().substring(1) : item.getLabel().substring(1));
			return;
		}
		if(c == fileImgItemOpenCmd) {
			// открытие файла
			String path = (String) files.get(item);
			if(path != null) {
				try {
					if(midlet.platformRequest(prepareUrl(path, directFile ? null : apiProxyUrl, instanceUrl, true))) {
						//notifyDestroyed();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return;
		}
		if(c == postLinkItemCmd) {
			String path = (String) links.get(item);
			//System.out.println(path);
			if(path != null) {
				// парс внутренней ссылки
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
							// прокси теперь не глайп поэтому не проксируем
							if(midlet.platformRequest(path)) {
								//notifyDestroyed();
							}
							/*
							if(midlet.platformRequest(prepareUrl(path, directFile ? null : fileProxyUrl, instanceUrl))) {
								//notifyDestroyed();
							}
							*/
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
			return;
		}
		if (c == postSpoilerItemCmd) {
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
			return;
		}
		if(c == nextPostsItemCmd) {
			clearThreadData();
			currentIndex += maxPostsCount;
			threadFrm.deleteAll();
			parsePosts(threadFrm, cachedPosts, true, false, false);
			return;
		}
		if(c == prevPostsItemCmd) {
			clearThreadData();
			currentIndex -= maxPostsCount;
			if(currentIndex < 0) currentIndex = 0;
			threadFrm.deleteAll();
			parsePosts(threadFrm, cachedPosts, true, false, false);
			return;
		}
		if(c == nextThreadsItemCmd) {
			clearThreadData();
			catalogIndex += maxPostsCount;
			boardFrm.deleteAll();
			parsePosts(boardFrm, cachedThreads, true, false, true);
			return;
		}
		if(c == prevThreadsItemCmd) {
			clearThreadData();
			catalogIndex -= maxPostsCount;
			boardFrm.deleteAll();
			parsePosts(boardFrm, cachedThreads, true, false, true);
			return;
		}
		if(c == boardsItemCmd) {
			if(boardsList != null) {
				display(boardsList);
				return;
			}
			List l = new List("Борды", List.IMPLICIT);
			l.addCommand(List.SELECT_COMMAND);
			l.addCommand(backCmd);
			l.setCommandListener(midlet);
			display(boardsList = l);
			start(RUN_BOARDS);
			return;
		}
		if(c == openByLinkItemCmd) {
			TextBox t = new TextBox("", "", 200, TextField.URL);
			t.setTitle("URL");
			t.addCommand(linkOkCmd);
			t.addCommand(backCmd);
			t.setCommandListener(midlet);
			display(t);
			return;
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
		currentIndex = 0;
		start(RUN_THREAD);
	}

	// оффсет это если прокручено
	private static void parsePosts(Form f, JSONArray posts, boolean offset, boolean search, boolean catalog) {
		// внезапная очистка
		System.gc();
		int l = posts.size();
		int count = catalog ? threadsCount : postsCount;
		int idx = catalog ? catalogIndex : currentIndex;
		if(offset && idx != 0) {
			StringItem s = new StringItem(null, "");
			f.append(s);
			StringItem btn = new StringItem(null, catalog ? "Предыдущие треды" : "Предыдущие посты");
			btn.setLayout(Item.LAYOUT_CENTER);
			if(catalog) {
				btn.addCommand(prevThreadsItemCmd);
				btn.setDefaultCommand(prevThreadsItemCmd);
			} else {
				btn.addCommand(prevPostsItemCmd);
				btn.setDefaultCommand(prevPostsItemCmd);
			}
			btn.setItemCommandListener(midlet);
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
			if(i + idx >= l || (count > 0 ? i + idx >= count : false)) {
				//System.out.println("limit: " + (i + currentIndex) + " " + postsCount);
				return;
			}
			if(i >= maxPostsCount) {
				if(!offset) {
					if(catalog) {
						catalogIndex = 0;
						cachedThreads = posts;
					} else {
						currentIndex = 0;
						cachedPosts = posts;
					}
				}
				StringItem btn = new StringItem(null, catalog ? "Следующие треды" : "Следующие посты");
				btn.setLayout(Item.LAYOUT_CENTER);
				if(catalog) {
					btn.addCommand(nextThreadsItemCmd);
					btn.setDefaultCommand(nextThreadsItemCmd);
				} else {
					btn.addCommand(nextPostsItemCmd);
					btn.setDefaultCommand(nextPostsItemCmd);
				}
				btn.setItemCommandListener(midlet);
				f.append(btn);
				break;
			}
			try {
				JSONObject post = posts.getObject(i + idx);
				JSONArray files = post.getNullableArray("files");
				//System.out.println(post);
				String num = post.getString("num", "");
				//System.out.println(post.toString());
				if(search || catalog) {
					// заголовок поста для формы поиска
					StringItem title = new StringItem(null, htmlText(post.getString("name", "")).concat("\n").concat(time2ch ? post.getString("date", "") : parsePostDate(post.getLong("timestamp", 0))));
					title.setFont(smallBoldFont);
					title.setLayout(Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
					f.append(title);
					f.append(new Spacer(smallBoldFont.charWidth(' ')+2, smallBoldFont.getHeight()));
					StringItem snum = new StringItem(null, "#".concat(num));
					snum.setFont(smallBoldFont);
					snum.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_LEFT);
					f.append(snum);
					snum.addCommand(openThreadCmd);
					snum.setDefaultCommand(openThreadCmd);
					snum.setItemCommandListener(midlet);
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
						String name = catalog ? null : file.getString("displayname", file.getString("name", ""));
						ImageItem fitem = new ImageItem(name, null, 0, name);
						fitem.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
						fitem.addCommand(fileImgItemOpenCmd);
						fitem.setDefaultCommand(fileImgItemOpenCmd);
						fitem.setItemCommandListener(midlet);
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

	// парсер дат постов
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
	
	// зачемто надо
	private static String i(int n) {
		return Integer.toString(n);
	}
	
	private static void parseHtmlText(Form f, String s) {
		// новый парс
		recursionParse(f, tidy.parseDOM("<html>".concat(s).concat("</html>")).getDocumentElement().getChildNodes());
		// здесь покоится старый парс на регексах
	}
	
	private static Alert warningAlert(String text) {
		Alert a = new Alert("");
		a.setType(AlertType.ERROR);
		a.setString(text);
		a.setTimeout(3000);
		return a;
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
			if(k == Font.SIZE_LARGE && j == Font.STYLE_BOLD) {
				return largeBoldFont;
			}
		}
		return Font.getFont(i, j, k);
	}
	
	// парс в рекурсии
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
				// костыль, попалось один раз
				v = replace(v, "\\r\\n", "\n");
				StringItem st = new StringItem(null, v);
				st.setLayout(Item.LAYOUT_2);
				boolean spoil = false;
				if(n.getParentNode() != null) {
					Node pn = n;
					String pk;
					// проверка всех родительских нодов
					while(!((pn = pn.getParentNode()) == null || (pk = pn.getNodeName()).equals("body"))) {
						//System.out.println(":PARENT NODE " + pk);
						// стили текста
						if(pk.equals("a")) {
							String link = null;
							NamedNodeMap atr = pn.getAttributes();
							if(atr.getNamedItem("href") != null) {
								link = atr.getNamedItem("href").getNodeValue();
							}
							// ссылки в спойлерах
							if(!spoil) {
								st.addCommand(postLinkItemCmd);
								st.setDefaultCommand(postLinkItemCmd);
								st.setItemCommandListener(midlet);
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
							if(cls == null) continue;
							if(cls.equals("u") || cls.equals("o")) {
								fstyle |= Font.STYLE_UNDERLINED;
							}
							if(cls.equals("spoiler")) {
								st.setText("[спойлер]");
								st.addCommand(postSpoilerItemCmd);
								st.setDefaultCommand(postSpoilerItemCmd);
								st.setItemCommandListener(midlet);
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
				// пробел, чтоб тексты не слипались
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
				// рекусрия
				recursionParse(f, n.getChildNodes());
			}
		}
	}

	private static void addFile(ImageItem img, String path, String thumb) {
		files.put(img, path);
		// тумбы
		if(filePreview) {
			thumbsToLoad.addElement(new Object[] { thumb, img });
			synchronized(thumbLoadLock) {
				thumbLoadLock.notifyAll();
			}
		}
	}
	
	private static void display(Alert a, Displayable d) {
		if(d == null) {
			display.setCurrent(a);
			return;
		}
		display.setCurrent(a, d);
	}

	private static void display(Displayable d) {
		if(d instanceof Alert) {
			Displayable c;
			display((Alert) d, (c = display.getCurrent()) instanceof Alert ? mainFrm : c);
			return;
		}
		display.setCurrent(d);
	}

	private static void board(String txt) {
		txt = txt.toLowerCase();
		if(txt.length() <= 0)
			return;
		if(txt.startsWith("/"))
			txt = txt.substring(1);
		if(txt.endsWith("/"))
			txt = txt.substring(0, txt.length() - 1);
		Form f = new Form("Jch - /".concat(currentBoard = txt).concat("/"));
		f.addCommand(backCmd);
		f.setCommandListener(midlet);
		addLoadingLabel(f, "Загрузка...");
		display.setCurrent(boardFrm = f);
		start(RUN_BOARD);
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

	private static Image getImg(String url) throws IOException {
		try {
			return getImg(url, direct2ch ? null : apiProxyUrl);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Image getImg(String url, String proxy) throws IOException {
		url = prepareUrl(url, proxy, instanceUrl, false);
		byte[] b = get(url);
		return Image.createImage(b, 0, b.length);
	}

	private static Object getResult(String s, String proxy, String inst) throws Exception {
		//System.out.println(s);
		String s2 = s;
		Object result = getString(prepareUrl(s, proxy, inst, false));
		// пустой ответ
		if(result.toString().length() == 0) throw new IOException("Empty response: " + s2);
		// проверка на жсон
		char c = ((String) result).charAt(0);
		if(c == '{')
			return getObject((String) result);
		else if(c == '[')
			return getArray((String) result);
		return result;
	}

	private static Object getResult(String s) throws Exception {
		return getResult(s, direct2ch ? null : apiProxyUrl, instanceUrl);
	}

	private static String prepareUrl(String url, String proxy, String inst, boolean addCookie) {
		if(url.startsWith("/")) url = url.substring(1);
		if(url.endsWith("&") || url.endsWith("?"))
			url = url.substring(0, url.length() - 1);
		if(proxy != null && proxy.length() > 1) {
			//конкатконкатконкатконкатконкат
			url = proxy.concat("?u=").concat(encodeUrl("https://".concat(inst).concat("/").concat(url)));
			if(addCookie && cookiesStr != null) {
				url = url.concat("&c=").concat(encodeUrl(cookiesStr));
			}
		} else {
			url = "https://".concat(inst).concat("/").concat(url);
		}
		return url;
	}

	private static void destroyApp() {
		running = false;
		midlet.notifyDestroyed();
	}
	
	// здесь начинаются утилзы
	private static String replace(String str, String from, String to) {
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
	
	private static String[] split(String str, char d) {
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

	private static String encodeUrl(String s) {
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

	private static byte[] get(String url) throws IOException {
		//System.out.println("GET " + url);
		HttpConnection hc = (HttpConnection) open(url, Connector.READ);

		InputStream is = null;
		ByteArrayOutputStream o = null;
		try {
			hc.setRequestMethod("GET");
			if(cookiesStr != null) {
				hc.setRequestProperty("Cookie", cookiesStr);
			}
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
				if(cookiesStr != null) {
					hc.setRequestProperty("Cookie", cookiesStr);
				}
				if(redirects++ > 3) {
					throw new IOException("Too many redirects!");
				}
			}
			boolean cookies = false;
			if (hc.getHeaderField("set-cookie") != null) {
				for (int i = 0;; i++) {
					String k = hc.getHeaderFieldKey(i);
					if (k == null)
						break;
					String v = hc.getHeaderField(i);
					//System.out.println(k + ": " + v);
					if(k.equalsIgnoreCase("set-cookie")) {
						//if(v.indexOf("code_auth=") != -1) {
						cookies = true;
						addCookie(v);
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
			if(cookies)
				saveCookies();
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
		String v = s;
		if(s.indexOf(';') != -1) {
			v = s.substring(0, s.indexOf(';'));
		}
		System.out.println("addCookie " + s);
		if(s.indexOf('=') == -1)
			return;
		if(cookies == null)
			cookies = new Hashtable();
		int i = v.indexOf('=');
		cookies.put(v.substring(0, i).trim(), v.substring(i + 1));
	}
	
	// Приготовить печенья
	private static void cookCookies() {
		if(cookies == null) {
			cookiesStr = "";
			return;
		}
		StringBuffer sb = new StringBuffer();
		Enumeration en = cookies.keys();
		while (true) {
			Object k = en.nextElement();
			sb.append(k.toString()).append("=").append(cookies.get(k).toString());
			if (!en.hasMoreElements()) {
				cookiesStr = sb.toString();
				return;
			}
			sb.append("; ");
		}
	}
	
	private static void saveCookies() {
		cookCookies();
		try {
			RecordStore.deleteRecordStore("jchcookie");
		} catch (Throwable e) {
		}
		try {
			RecordStore r = RecordStore.openRecordStore("jchcookie", true);
			byte[] b = cookiesStr.getBytes();
			r.addRecord(b, 0, b.length);
			r.closeRecordStore();
		} catch (Exception e) {
		}
	}
	
	private static String getString(String url) throws IOException {
		byte[] b = get(url);
		try {
			return new String(b, "UTF-8");
		} catch (Throwable e) {
			return new String(b);
		}
	}
	
	private static HttpConnection open(String url, int i) throws IOException {
		try {
			HttpConnection con = (HttpConnection) Connector.open(url, i);
			con.setRequestProperty("User-Agent", "Jch/" + version + " " + platform);
			return con;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static String htmlText(String str) {
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
	// здесь начинается выдранное из жсон либы
	
	// parse all nested elements once
	static final boolean parse_members = false;
	
	// identation for formatting
	static final String FORMAT_TAB = "  ";
	
	// used for storing nulls, get methods must return real null
	static final Object json_null = new Object();
	
	static final Boolean TRUE = new Boolean(true);
	static final Boolean FALSE = new Boolean(false);

	static JSONObject getObject(String text) {
		if (text == null || text.length() <= 1)
			throw new RuntimeException("JSON: Empty text");
		if (text.charAt(0) != '{')
			throw new RuntimeException("JSON: Not JSON object");
		return (JSONObject) parseJSON(text);
	}

	static JSONArray getArray(String text) {
		if (text == null || text.length() <= 1)
			throw new RuntimeException("JSON: Empty text");
		if (text.charAt(0) != '[')
			throw new RuntimeException("JSON: Not JSON array");
		return (JSONArray) parseJSON(text);
	}

	static Object getJSON(Object obj) {
		if (obj instanceof Hashtable) {
			return new JSONObject((Hashtable) obj);
		}
		if (obj instanceof Vector) {
			return new JSONArray((Vector) obj);
		}
		if (obj == null) {
			return json_null;
		}
		return obj;
	}

	static Object parseJSON(String str) {
		char first = str.charAt(0);
		int length = str.length() - 1;
		char last = str.charAt(length);
		switch(first) {
		case '"': { // string
			if (last != '"')
				throw new RuntimeException("JSON: Unexpected end of text");
			if(str.indexOf('\\') != -1) {
				char[] chars = str.substring(1, length).toCharArray();
				str = null;
				int l = chars.length;
				StringBuffer sb = new StringBuffer();
				int i = 0;
				// parse escaped chars in string
				loop: {
					while (i < l) {
						char c = chars[i];
						switch (c) {
						case '\\': {
							next: {
								replace: {
									if (l < i + 1) {
										sb.append(c);
										break loop;
									}
									char c1 = chars[i + 1];
									switch (c1) {
									case 'u':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {chars[i++], chars[i++], chars[i++], chars[i++]}),
												16));
										break replace;
									case 'x':
										i+=2;
										sb.append((char) Integer.parseInt(
												new String(new char[] {chars[i++], chars[i++]}),
												16));
										break replace;
									case 'n':
										sb.append('\n');
										i+=2;
										break replace;
									case 'r':
										sb.append('\r');
										i+=2;
										break replace;
									case 't':
										sb.append('\t');
										i+=2;
										break replace;
									case 'f':
										sb.append('\f');
										i+=2;
										break replace;
									case 'b':
										sb.append('\b');
										i+=2;
										break replace;
									case '\"':
									case '\'':
									case '\\':
									case '/':
										i+=2;
										sb.append((char) c1);
										break replace;
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
				return str;
			}
			return str.substring(1, length);
		}
		case '{': // JSON object or array
		case '[': {
			boolean object = first == '{';
			if (object ? last != '}' : last != ']')
				throw new RuntimeException("JSON: Unexpected end of text");
			int brackets = 0;
			int i = 1;
			char nextDelimiter = object ? ':' : ',';
			boolean escape = false;
			String key = null;
			Object res = object ? (Object) new JSONObject() : (Object) new JSONArray();
			
			for (int splIndex; i < length; i = splIndex + 1) {
				// skip all spaces
				for (; i < length - 1 && str.charAt(i) <= ' '; i++);

				splIndex = i;
				boolean quote = false;
				for (; splIndex < length && (quote || brackets > 0 || str.charAt(splIndex) != nextDelimiter); splIndex++) {
					char c = str.charAt(splIndex);
					if (!escape) {
						if (c == '\\') {
							escape = true;
						} else if (c == '"') {
							quote = !quote;
						}
					} else escape = false;
	
					if (!quote) {
						if (c == '{' || c == '[') {
							brackets++;
						} else if (c == '}' || c == ']') {
							brackets--;
						}
					}
				}

				// fail if unclosed quotes or brackets left
				if (quote || brackets > 0) {
					throw new RuntimeException("JSON: Corrupted JSON");
				}

				if (object && key == null) {
					key = str.substring(i, splIndex);
					key = key.substring(1, key.length() - 1);
					nextDelimiter = ',';
				} else {
					Object value = str.substring(i, splIndex).trim();
					// check if value is empty
					if(((String) value).length() == 0) continue;
					// don't check length because if value is empty, then exception is going to be thrown anyway
					char c = ((String) value).charAt(0);
					// leave JSONString as value to parse it later, if its object or array and nested parsing is disabled
					value = parse_members || (c != '{' && c != '[') ?
							parseJSON((String) value) : new String[] {(String) value};
					if (object) {
						((JSONObject) res)._put(key, value);
						key = null;
						nextDelimiter = ':';
					} else if (splIndex > i) {
						((JSONArray) res).addElement(value);
					}
				}
			}
			return res;
		}
		case 'n': // null
			return json_null;
		case 't': // true
			return TRUE;
		case 'f': // false
			return FALSE;
		default: // number
			if ((first >= '0' && first <= '9') || first == '-') {
				try {
					// hex
					if (length > 1 && first == '0' && str.charAt(1) == 'x') {
						if (length > 9) // str.length() > 10
							return new Long(Long.parseLong(str.substring(2), 16));
						return new Integer(Integer.parseInt(str.substring(2), 16));
					}
					// decimal
					if (first == '-') length--;
					if (length > 8) // (str.length() - (str.charAt(0) == '-' ? 1 : 0)) >= 10
						return new Long(Long.parseLong(str));
					return new Integer(Integer.parseInt(str));
				} catch (Exception e) {}
			}
			throw new RuntimeException("JSON: Couldn't be parsed: " + str);
//			return new JSONString(str);
		}
	}

	// transforms string for exporting
	static String escape_utf8(String s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < len) {
			char c = s.charAt(i);
			switch (c) {
			case '"':
			case '\\':
				sb.append("\\").append(c);
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
					sb.append("\\u");
					for (int z = u.length(); z < 4; z++) {
						sb.append('0');
					}
					sb.append(u);
				} else {
					sb.append(c);
				}
			}
			i++;
		}
		return sb.toString();
	}

	static int getInt(Object o) {
		try {
			if (o instanceof String[])
				return Integer.parseInt(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).intValue();
			if (o instanceof Long)
				return (int) ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new RuntimeException("JSON: Cast to int failed: " + o);
	}

	static long getLong(Object o) {
		try {
			if (o instanceof String[])
				return Long.parseLong(((String[]) o)[0]);
			if (o instanceof Integer)
				return ((Integer) o).longValue();
			if (o instanceof Long)
				return ((Long) o).longValue();
		} catch (Throwable e) {}
		throw new RuntimeException("JSON: Cast to long failed: " + o);
	}
}
