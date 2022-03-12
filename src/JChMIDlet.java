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
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;
import me.regexp.RE;

public class JChMIDlet extends MIDlet implements CommandListener, ItemCommandListener {
	
	private static String version;
	
	private static final String aboutText =
			  "<h>JCh</h><br>"
			+ "версия <ver><br><br>"
			+ "Клиент <a href=\"https://2ch.hk\">2ch.hk</a> для Symbian/J2ME устройств<br><br>"
			+ "Разработал<br>"
			+ "Shinovon (<a href=\"http://nnproject.cc\">nnproject.cc</a>)";

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
	private static Command postThreadCmd = new Command("Запостить тред", Command.SCREEN, 0);
	private static Command postCommentCmd = new Command("Ответить в тред", Command.SCREEN, 0);
	private static Command aboutCmd = new Command("О программе", Command.SCREEN, 0);
	private static Command settingsCmd = new Command("Настройки", Command.SCREEN, 0);
	private static Command agreeCmd = new Command("Да", Command.OK, 0);
	private static Command disagreeCmd = new Command("Нет", Command.EXIT, 0);
	private static Command postCmd = new Command("Запостить", Command.OK, 0);
	private static Command textOkCmd = new Command("Ок", Command.OK, 0);
	private static Display display;

	private static Font largeBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
	private static Font smallPlainFont = Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private static Font smallBoldFont = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL);
	private static Font smallItalicFont = Font.getFont(0, Font.STYLE_ITALIC, Font.SIZE_SMALL);
	private static Font smallUnderlinedFont = Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
	
	private static Object result;
	
	private Form mainFrm;
	private Form boardFrm;
	private Form threadFrm;
	private Form aboutFrm;
	private Form boardsFrm;
	private Form postingFrm;
	private TextField boardField;
	private TextField boardSearchField;
	
	private String currentBoard;
	private String currentThread;

	private boolean running = true;
	private boolean started;
	
	private StringItem tempLoadingLabel;
	private int loadingLabelIndex = -1;
	
	private Hashtable files = new Hashtable();
	private Hashtable links = new Hashtable();
	private Hashtable spoilers = new Hashtable();
	private Hashtable comments = new Hashtable();
	
	private Thread lastThread;
	
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
	};

	private JSONArray cachedPosts;
	private int postsCount;
	private int currentIndex;

	// Settings
	private static String glypeProxyUrl = "http://f.spoolls.com/a/browse.php?u=";
	private static String apiProxyUrl = "http://f.spoolls.com/a/browse.php?u=";
	private static String imgProxyUrl = "http://f.spoolls.com/a/browse.php?u=";
	private int maxPostsCount = 10;

	private TextField postSubjectField;

	private TextField postTextField;

	private StringItem postTextBtn;

	private String postThread;

	private String postBoard;

	private TextBox tempTextBox;
	
	private static final RE htmlRe = new RE("(<a(.*?)>(.*?)</a>|<strong>(.*?)</strong>|<b>(.*?)</b>|<i>(.*?)</i>|<em>(.*?)</em>|<span(.*?)>(.*?)</span>|(<h>(.*?)</h>))");
	private static final RE hrefRe = new RE("(href=\"(.*?)\")");
	private static final RE classRe = new RE("(class=\"(.*?)\")");
	
	static String version() {
		return version;
	}

	public JChMIDlet() {
		mainFrm = new Form("Jch - Главная");
		mainFrm.setCommandListener(this);
		mainFrm.addCommand(exitCmd);
		mainFrm.addCommand(aboutCmd);
		mainFrm.append(boardField = new TextField("", "", 8, TextField.ANY));
		boardField.setLabel("Доска");
		boardField.addCommand(boardFieldCmd);
		boardField.setItemCommandListener(this);
		StringItem btn = new StringItem("", "", StringItem.BUTTON);
		btn.setText("Ввод");
		btn.setDefaultCommand(boardFieldCmd);
		btn.setItemCommandListener(this);
		mainFrm.append(btn);
		StringItem btn2 = new StringItem("", "", StringItem.BUTTON);
		btn2.setText("Доски");
		btn2.setLayout(Item.LAYOUT_EXPAND);
		btn2.setDefaultCommand(boardsItemCmd);
		btn2.setItemCommandListener(this);
		mainFrm.append(btn2);
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
		display = Display.getDisplay(this);
		display.setCurrent(mainFrm);
		thumbLoaderThread.setPriority(2);
		thumbLoaderThread.start();
	}

	private void loadBoards() {
		new Thread() {
			public void run() {
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
							btn.setItemCommandListener(JChMIDlet.this);
							btn.setLayout(Item.LAYOUT_EXPAND);
							mainFrm.append(btn);
							Thread.yield();
						}
						Thread.yield();
					}
					*/
					getResult("boards.json");
					System.out.println(result);
					if(!(result instanceof JSONObject))
						return;
					JSONArray boards = ((JSONObject)result).getArray("boards");
					for(Enumeration en = boards.elements(); en.hasMoreElements();) {
						JSONObject board = (JSONObject) en.nextElement();
						String id = board.getNullableString("id");
						String name = board.getNullableString("name");
						StringItem btn = new StringItem("", "", StringItem.BUTTON);
						btn.setText("/" + id + "/ " + name);
						btn.setDefaultCommand(boardCmd);
						btn.setItemCommandListener(JChMIDlet.this);
						btn.setLayout(Item.LAYOUT_EXPAND);
						boardsFrm.append(btn);
						Thread.yield();
					}
				} catch (Exception e) {
					e.printStackTrace();
					removeLoadingLabel(boardsFrm);
					addLoadingLabel(boardsFrm, "Ошибка!");
					StringItem s = new StringItem("", e.toString());
					s.setLayout(Item.LAYOUT_LEFT);
					boardsFrm.append(s);
				}
			}
		}.start();
	}

	public static void getResult(String string) throws Exception {
		System.out.println(string);
		result = null;
		result = Util.getString(prepareUrl(string));
		if(result.toString().length() == 0) throw new IOException("Empty response");
		char c = ((String) result).charAt(0);
		if(c == '{')
			result = JSON.getObject((String) result);
		else if(c == '[')
			result = JSON.getArray((String) result);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == exitCmd)
			destroyApp(false);
		else if(c == backCmd) {
			if(d == boardFrm) {
				display.setCurrent(mainFrm);
			} else if(d == threadFrm) {
				display.setCurrent(boardFrm);
				clearThreadData();
			} else if(d == aboutFrm) {
				display.setCurrent(mainFrm);
				clearThreadData();
				aboutFrm = null;
			} else if(d == boardsFrm) {
				display.setCurrent(mainFrm);
			}
		} else if(c == aboutCmd) {
			aboutFrm = new Form("Jch - О программе");
			aboutFrm.addCommand(backCmd);
			aboutFrm.setCommandListener(this);
			StringItem s = new StringItem(null, "");
			aboutFrm.append(s);
			parseHtmlText(aboutFrm, Util.replace(aboutText, "<ver>", version));
			display(aboutFrm);
			if(Util.platform.indexOf("S60") > -1 && Util.platform.indexOf("=3.2") == -1)
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
		}
	}
	
	private void createPostingForm() {
		postingFrm = new Form("Jch - Форма постинга");
		postingFrm.addCommand(backCmd);
		postingFrm.addCommand(postCmd);
		postingFrm.setCommandListener(this);
		postSubjectField = new TextField("", "", 200, TextField.ANY);
		postingFrm.append(postSubjectField);
		postTextField = new TextField("Текст", "", 1000, TextField.ANY);
		postTextField.setLayout(Item.LAYOUT_VEXPAND | Item.LAYOUT_EXPAND );
		postingFrm.append(postTextField);
		postTextBtn = new StringItem("", "", StringItem.BUTTON);
		postTextBtn.setText("...");
		postTextBtn.setLayout(Item.LAYOUT_RIGHT);
		postTextBtn.addCommand(postTextItemCmd);
		postTextBtn.setDefaultCommand(postTextItemCmd);
		postTextBtn.setItemCommandListener(this);
		postingFrm.append(postTextBtn);
		StringItem s = new StringItem("", "Файлы будут добавлены позже!\n"
				+ "Капча спрошена будет после нажатия на Запостить\n"
				+ "ответьте на нее не менее чем за минуту");
		s.setLayout(Item.LAYOUT_LEFT);
		postingFrm.append(s);
		display(postingFrm);
	}

	private void clearThreadData() {
		files.clear();
		links.clear();
		spoilers.clear();
		comments.clear();
		thumbsToLoad.removeAllElements();
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
			openThread(s);
		} else if(c == fileImgItemOpenCmd) {
			String path = (String) files.get(item);
			if(path != null) {
				try {
					if(platformRequest(prepareUrl(path, glypeProxyUrl))) {
						//notifyDestroyed();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if(c == postLinkItemCmd) {
			String path = (String) links.get(item);
			System.out.println("Link: " + path);
			if(path != null) {
				if(path.startsWith("/") || path.startsWith("https://2ch.hk/")) {
					if(path.indexOf("/res/") != -1 && path.indexOf(".html") != -1) {
						String bd = path;
						bd = bd.substring(0, bd.indexOf("/res/"));
						bd = bd.substring(bd.lastIndexOf('/')+1);
						String tid = path;
						tid = tid.substring(tid.indexOf("/res/") + "/res/".length(), tid.indexOf(".html"));
						System.out.println(tid);
						if(path.indexOf("html#") != -1) {
							String cid = path;
							cid = cid.substring(cid.indexOf("html#") + "html#".length());
							System.out.println(cid);
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
							if(platformRequest(prepareUrl(path, glypeProxyUrl))) {
								//notifyDestroyed();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
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
		} else if(c == boardSearchItemCmd) {
			//String q = ((TextField)item).getString();
		} else if(c == postSpoilerItemCmd) {
			String t = (String) spoilers.get(item);
			if(t != null) {
				StringItem s = ((StringItem) item);
				s.setText(t);
				//s.setDefaultCommand(null);
				s.setItemCommandListener(null);
				//s.removeCommand(postSpoilerItemCmd);
			}
		} else if(c == nextPostsItemCmd) {
			clearThreadData();
			threadFrm.deleteAll();
			currentIndex += maxPostsCount;
			parsePosts(cachedPosts, maxPostsCount);
		} else if(c == boardsItemCmd) {
			if(boardsFrm != null) {
				display(boardsFrm);
				return;
			}
			boardsFrm = new Form("Jch - Доски");
			boardsFrm.addCommand(backCmd);
			boardsFrm.setCommandListener(this);
			display(boardsFrm);
			addLoadingLabel(boardsFrm);
			loadBoards();
		} else if(c == postTextItemCmd) {
			tempTextBox = new TextBox("", "", 1000, TextField.ANY);
			tempTextBox.setString(postTextField.getString());
			tempTextBox.addCommand(textOkCmd);
			tempTextBox.setCommandListener(this);
			display(tempTextBox);
		}
	}
	
	private void openThread(String id) {
		openThread(id, currentBoard, null);
	}
	
	private void openThread(String id, String board) {
		openThread(id, board, null);
	}

	private void openThread(final String id, final String bd, final String post) {
		if(lastThread != null && lastThread.isAlive()) {
			lastThread.interrupt();
			lastThread = null;
		}
		if(threadFrm != null) {
			clearThreadData();
		}
		System.out.println("Thread: " + id);
		currentThread = id;
		Thread t = lastThread = new Thread() {
			public void run() {
				try {
					threadFrm = new Form("#" + id);
					threadFrm.addCommand(backCmd);
					threadFrm.setCommandListener(JChMIDlet.this);
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
							if(result == null || !(result instanceof JSONArray))
								return;
							posts = (JSONArray) result;
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
							threadFrm.setTitle("/" + bd + "/ - " + Util.htmlText(j.getString("title", "")));
							JSONArray th = j.getNullableArray("threads");
							if(th == null)
								return;
							JSONObject t = th.getObject(0);
							posts = t.getArray("posts");
							postsCount = j.getInt("posts_count");
						}
						if(threadFrm != null) removeLoadingLabel(threadFrm);
						else return;
						parsePosts(posts, 0);
					} catch (InterruptedException e) {
					} catch (Exception e) {
						e.printStackTrace();
						if(threadFrm != null) {
							removeLoadingLabel(threadFrm);
							addLoadingLabel(threadFrm, "Ошибка!");
							StringItem s = new StringItem("", e.toString());
							s.setLayout(Item.LAYOUT_LEFT);
							threadFrm.append(s);
						}
					}
				} catch (Exception e) {
				}
			}
		};
		t.start();
		
	}

	private void parsePosts(JSONArray posts, int offset) {
		int l = posts.size();
		if(offset != 0) {
			StringItem btn = new StringItem(null, "Предыдущие посты");
			btn.setLayout(Item.LAYOUT_CENTER);
			btn.addCommand(prevPostsItemCmd);
			btn.setDefaultCommand(prevPostsItemCmd);
			btn.setItemCommandListener(JChMIDlet.this);
			threadFrm.append(btn);
		}
		for(int i = 0; i < l /*&& i < 30*/; i++) {
			if(i + currentIndex + offset >= l) return;
			if(i >= maxPostsCount) {
				if(offset == 0) {
					currentIndex = 0;
					cachedPosts = posts;
				}
				StringItem btn = new StringItem(null, "Следующие посты");
				btn.setLayout(Item.LAYOUT_CENTER);
				btn.addCommand(nextPostsItemCmd);
				btn.setDefaultCommand(nextPostsItemCmd);
				btn.setItemCommandListener(JChMIDlet.this);
				threadFrm.append(btn);
				break;
			}
			JSONObject post = posts.getObject(i + currentIndex);
			JSONArray files = post.getNullableArray("files");
			//System.out.println(post);
			String num = post.getString("num", "");
			StringItem title = new StringItem(null, Util.htmlText(post.getString("name", "")) + " " + post.getString("date", "") + " #" + num);
			title.setFont(smallBoldFont);
			title.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_LEFT);
			comments.put(num, title);
			threadFrm.append(title);
			/*
			StringItem text = new StringItem(null, Util.text(post.getString("comment", "")));
			text.setFont(Font.getFont(0, Font.STYLE_PLAIN, Font.SIZE_SMALL));
			text.setLayout(Item.LAYOUT_NEWLINE_AFTER);
			threadFrm.append(text);
			*/
			parseHtmlText(threadFrm, post.getString("comment", ""));
			
			if(files != null) {
				int fl = files.size();
				for(int fi = 0; fi < fl && fi < 5; fi++) {
					JSONObject file = files.getObject(fi);
					String name = file.getString("displayname", file.getString("name", ""));
					ImageItem fitem = new ImageItem(name, null, 0, name);
					fitem.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
					fitem.addCommand(fileImgItemOpenCmd);
					fitem.setDefaultCommand(fileImgItemOpenCmd);
					fitem.setItemCommandListener(JChMIDlet.this);
					addFile(fitem, file.getString("path", null), file.getString("thumbnail", null));
					threadFrm.append(fitem);
				}
			}
			Spacer s = new Spacer(2, 20);
			s.setLayout(Item.LAYOUT_2);
			threadFrm.append(s);
		}
	}

	protected void parseHtmlText(Form f, String s) {
		int ti;
		int tl;
		for (ti = 0, tl = s.length(); ti < tl && htmlRe.match(s, ti); ti = htmlRe.getParenEnd(0)) {
			String o = s.substring(ti, htmlRe.getParenStart(0));
			if(o != null && o.length() > 0) {
				o = Util.htmlText(o);
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
					link = Util.htmlText(link);
				}
				String c = htmlRe.getParen(3);
				c = Util.htmlText(c);
				StringItem linkitem = new StringItem(null, c);
				linkitem.setFont(smallUnderlinedFont);
				linkitem.setLayout(Item.LAYOUT_2);
				linkitem.addCommand(postLinkItemCmd);
				linkitem.setDefaultCommand(postLinkItemCmd);
				linkitem.setItemCommandListener(JChMIDlet.this);
				f.append(linkitem);
				links.put(linkitem, link);
			} else if(w.startsWith("<strong>")) {
				// жирный текст
				String c = htmlRe.getParen(4);
				c = Util.htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<b>")) {
				// жирный текст
				String c = htmlRe.getParen(5);
				c = Util.htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<i>")) {
				// жирный текст
				String c = htmlRe.getParen(6);
				c = Util.htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(smallBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			} else if(w.startsWith("<em>")) {
				// курсивный текст
				String c = htmlRe.getParen(7);
				c = Util.htmlText(c);
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
				c = Util.htmlText(c);
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
					textitem.setItemCommandListener(JChMIDlet.this);
					spoilers.put(textitem, c);
					//textitem.setFont(Font.getFont(0, Font.STYLE_UNDERLINED, Font.SIZE_SMALL));
				} else if(cls.equals("unkfunc")) {
					// цитата
				}
				f.append(textitem);
			} else if(w.startsWith("<h>")) {
				String c = htmlRe.getParen(11);
				System.out.println("h " + c);
				c = Util.htmlText(c);
				StringItem bolditem = new StringItem(null, c);
				bolditem.setFont(largeBoldFont);
				bolditem.setLayout(Item.LAYOUT_2);
				f.append(bolditem);
			}
		}
		if (ti < tl) {
			String o = s.substring(ti);
			if(o != null && !o.equals("")) {
				StringItem textitem = new StringItem(null, o = Util.htmlText(o));
				textitem.setFont(smallPlainFont);
				textitem.setLayout(Item.LAYOUT_2);
				f.append(textitem);
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
		display.setCurrent(f);
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
	}
	
	private void createBoard(final String board) {
		boardFrm = new Form("Jch - /" + board + "/");
		boardFrm.addCommand(backCmd);
		boardFrm.addCommand(postThreadCmd);
		boardFrm.setCommandListener(this);
		boardFrm.append(boardSearchField = new TextField("","", 1000, TextField.ANY));
		boardSearchField.setLabel("Поиск");
		boardSearchField.addCommand(boardSearchItemCmd);
		boardSearchField.setItemCommandListener(this);
		addLoadingLabel(boardFrm);
		display.setCurrent(boardFrm);
		/*StringItem btn = new StringItem("Поиск", "", StringItem.BUTTON);
		btn.setDefaultCommand(boardSearchCmd);
		btn.setItemCommandListener(this);
		boardFrm.append(btn);*/
		Thread t = lastThread = new Thread() {
			public void run() {
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
					int l = th.size();
					removeLoadingLabel(boardFrm);
					for(int i = 0; i < l && i < 20; i++) {
						JSONObject thread = th.getObject(i);
						//System.out.println(thread);
						StringItem s = new StringItem("#" + thread.getString("num", ""),
								Util.htmlText(thread.getString("subject", "")));
						s.addCommand(openThreadCmd);
						s.setDefaultCommand(openThreadCmd);
						s.setItemCommandListener(JChMIDlet.this);
						boardFrm.append(s);
						//boardFrm.append(Util.text(comment) + "\n");
					}
				} catch (InterruptedException e) {
				} catch (Exception e) {
					e.printStackTrace();
					try {
						removeLoadingLabel(boardFrm);
						addLoadingLabel(boardFrm, "Ошибка!");
						StringItem s = new StringItem("", e.toString());
						s.setLayout(Item.LAYOUT_LEFT);
						boardFrm.append(s);
					} catch (NullPointerException e2) {
					}
				}
			}
		};
		t.start();
	}

	private void addLoadingLabel(Form f) {
		addLoadingLabel(f, "Загрузка...");
	}

	private void addLoadingLabel(Form f, String s) {
		if(f == null) return;
		tempLoadingLabel = new StringItem(null, s);
		tempLoadingLabel.setFont(smallPlainFont);
		tempLoadingLabel.setLayout(Item.LAYOUT_CENTER);
		loadingLabelIndex = f.append(tempLoadingLabel);
	}

	private void removeLoadingLabel(Form f) {
		if(tempLoadingLabel != null && loadingLabelIndex != -1) {
			f.delete(loadingLabelIndex);
			tempLoadingLabel = null;
			loadingLabelIndex = -1;
		}
	}

	public static Image getImg(String url) throws IOException {
		url = prepareUrl(url, imgProxyUrl);
		byte[] b = Util.get(url);
		return Image.createImage(b, 0, b.length);
	}

	public static String prepareUrl(String url) {
		return prepareUrl(url, apiProxyUrl);
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
