
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public class JChMIDlet extends MIDlet {

	private static boolean started;

	protected void startApp() {
		Jch.display = Display.getDisplay(this);
		Jch.midlet = this;
		if(started)
			return;
		started = true;
		Jch.startApp();
	}
	

	protected void destroyApp(boolean b) {
		Jch.destroyApp();
	}

	protected void pauseApp() {
	}

}
