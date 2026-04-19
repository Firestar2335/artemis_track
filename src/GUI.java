import ODM.StateVector;
import ODM.KeplerElements;

import ExtraMath.Quaternion;

import java.awt.event.*;
import java.awt.*;
//import java.awt.Color;
//import java.awt.Image;
//import java.awt.Taskbar;

//import javax.swing.*;
//import javax.swing.text.*;

import java.time.Duration;

import java.io.File;

public class GUI {
	private static final int INSET = 10;
	private static final Color FRAME_BACKGROUND = new Color(255,255,208);

	private static final Color TEXT_BACKGROUND = new Color(255,250,200);
	private static final Color BUTTON_BACKGROUND = new Color(255,230,0);

	private final UserInterface parent;

	/*private final JFrame frame;

	private final JButton quitButton;

	private final JTextComponent currentData;*/

	private final Frame frame;

	private Button quitButton;

	private TextComponent currentData;

	private Navball navball;

	private Taskbar tb;

	public GUI(UserInterface parent) {
		this.parent = parent;
		if (Taskbar.isTaskbarSupported()) {
			tb = Taskbar.getTaskbar();
			if (!tb.isSupported(Taskbar.Feature.USER_ATTENTION_WINDOW)) {
				tb = null;
			}
		}
		frame = new Frame("Artemis II Telemetry Data");
		frame.addWindowListener(new WindowCloser());
		//Panel p = new Panel(new GridBagLayout());
		//p.setBackground(BACKGROUND);
		//frame.add(p);
		frame.setBackground(FRAME_BACKGROUND);
		addComponents(frame);
		frame.pack();
		frame.setVisible(true);
	}

	private void addComponents(Container addTo) {
		addTo.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(INSET, INSET, INSET, INSET);
		c.ipadx = 200;
		c.ipady = 5;
		//c.weighty = 0;
		//c.weightx = 0.1;
		//c.fill = GridBagConstraints.BOTH;
		//c.anchor = GridBagConstraints.PAGE_END;
		quitButton = new Button("Exit");
		quitButton.addActionListener((ActionEvent e) -> {close();});
		quitButton.setBackground(BUTTON_BACKGROUND);
		addTo.add(quitButton, c);
		c.gridwidth = 1;
		c.gridheight = 2;
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx = 0.7;
		c.fill = GridBagConstraints.BOTH;
		//c.anchor = GridBagConstraints.CENTER;
		currentData = new TextArea("Waiting for web...",30,101);
		currentData.setEditable(false);
		currentData.setBackground(TEXT_BACKGROUND);
		addTo.add(currentData,c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		//c.ipady = 50;
		c.weightx = 0.3;
		c.weighty = 0.4;
		c.insets = new Insets(INSET,INSET,2,INSET);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.PAGE_END;
		Label lab = new Label("Attitude relative to the Earth's surface", Label.CENTER);
		//lab.setBackground(new Color(0,0,0,0));
		addTo.add(lab, c);
		c.insets = new Insets(2,INSET, INSET, INSET);
		c.anchor = GridBagConstraints.PAGE_START;
		c.weighty = 0.6;
		//c.ipady = 0;
		c.fill = GridBagConstraints.BOTH;
		navball = new Navball(new File("./data"),300);
		addTo.add(navball, c);
	}

	private void close() {
		frame.dispose();
		parent.quit();
	}

	public void updateCurrentTelemetry(Duration elapsedTime,StateVector vectors, KeplerElements elements, Quaternion attitude) {
		currentData.setText(UserInterface.formatDuration(elapsedTime)+"\n"+UserInterface.formatVectors(vectors)+"\n\n"+
				UserInterface.formatElements(elements)+"\n\n"+UserInterface.formatAngles(EulerAngles.fromQuaternion(attitude)));
		if (vectors != null && attitude != null) {
			navball.updateAngles(attitude, vectors);
			navball.repaint();
		}
		//frame.pack();
		flashIcon();
	}

	private void flashIcon() {
		if (tb != null && !frame.isFocused()) {
			tb.requestWindowUserAttention(frame);
		}
	}

	private class WindowCloser implements WindowListener {

		public void windowClosed(WindowEvent e) {
			close();
		}

		public void windowClosing(WindowEvent e) {
			close();
		}
		
		public void windowOpened(WindowEvent e) { }

		public void windowActivated(WindowEvent e) { }

		public void windowDeactivated(WindowEvent e) { }
		
		public void windowIconified(WindowEvent e) { }

		public void windowDeiconified(WindowEvent e) { }
	}
}
