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

	private final Frame frame;

	private TextComponent currentData;

	private Navball navball;

	private Taskbar tb;

	private SliderPanel sliders;

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
		c.fill = GridBagConstraints.BOTH;
		sliders = new SliderPanel();
		sliders.addAdjustmentListener(new SliderAdjustment());
		addTo.add(sliders, c);
		c.insets = new Insets(INSET, INSET, INSET, INSET);
		c.ipadx = 200;
		c.ipady = 5;
		c.fill = GridBagConstraints.NONE;
		//c.weighty = 0;
		//c.weightx = 0.1;
		//c.fill = GridBagConstraints.BOTH;
		//c.anchor = GridBagConstraints.PAGE_END;
		//quitButton = new Button("Exit");
		//quitButton.addActionListener((ActionEvent e) -> {close();});
		//quitButton.setBackground(BUTTON_BACKGROUND);
		addTo.add(makeButtons(), c);
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

	private Panel makeButtons() {
		Panel buttons = new Panel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(INSET, INSET, INSET, INSET);
		c.ipadx = 200;
		c.ipady = 5;
		c.fill = GridBagConstraints.NONE;
		//c.weighty = 0;
		//c.weightx = 0.1;
		//c.fill = GridBagConstraints.BOTH;
		//c.anchor = GridBagConstraints.PAGE_END;
		Button quitButton = new Button("Exit");
		quitButton.addActionListener((ActionEvent e) -> {close();});
		quitButton.setBackground(BUTTON_BACKGROUND);
		buttons.add(quitButton, c);

		Button pauseButton = new Button("Pause");
		pauseButton.addActionListener((ActionEvent e) -> {
			if (parent.isPaused()) {
				parent.unpause();
				pauseButton.setLabel("Pause");
			}
			else {
				parent.pause();
				pauseButton.setLabel("Resume");
			}
		});
		pauseButton.setBackground(BUTTON_BACKGROUND);
		buttons.add(pauseButton, c);
		return buttons;
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
		updateTime();
		//frame.pack();
		flashIcon();
	}

	public void updateTime() {
		sliders.updateBounds(parent.getMinGen(), parent.getMaxGen(), parent.getGen());
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

	private static class SliderPanel extends Panel {
		//public final Panel panel;

		/** The number of microseconds each step takes */
		private static final long TIMESTEP = 1000l;

		/** The actual value for the minimum bound */
		private static final int LOWER_BOUND = Integer.MIN_VALUE;

		private Label currentLabel;
		
		private Label minLabel;
		
		private Label maxLabel;

		private Scrollbar slider;

		private long min;
		private long max;

		private int width;

		public SliderPanel() {
			super(new GridBagLayout());
			min = 0l;
			max = 0l;
			width = 100;
			//panel = new Panel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 1;
			c.gridy = 0;
			c.ipadx = 100;
			currentLabel = new Label("", Label.CENTER);
			add(currentLabel, c);
			//currentLabel.setBackground(new Color(0,0,0));
			
			c.gridy = 1;
			c.gridx = 0;
			c.anchor = GridBagConstraints.LINE_END;
			c.weightx = 0.3;
			minLabel = new Label("", Label.RIGHT);
			add(minLabel, c);

			c.gridx = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(INSET, INSET, INSET, INSET);
			c.weightx = 0.6;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.ipadx = 0;
			slider = new Scrollbar(Scrollbar.HORIZONTAL, LOWER_BOUND, width,LOWER_BOUND,LOWER_BOUND + width);
			slider.addAdjustmentListener(new LabelUpdate());
			add(slider, c);

			c.insets = new Insets(0,0,0,0);
			c.gridx = 2;
			c.anchor = GridBagConstraints.LINE_START;
			c.weightx = 0.3;
			c.fill = GridBagConstraints.NONE;
			c.ipadx = 100;
			maxLabel = new Label("", Label.LEFT);
			add(maxLabel, c);
			//setBackground(new Color(0,0,0));
		}

		/**
		 * Converts the provided long value into an int with saturation. If {@code val} is less 
		 * than {@code Integer.MIN_VALUE} ({@value Integer#MIN_VALUE}), then 
		 * {@code Integer.MIN_VALUE} is returned. If {@code val} is greater than 
		 * {@code Integer.MAX_VALUE} ({@value Integer#MAX_VALUE}), then {@code Integer.MAX_VALUE} 
		 * is returned. Otherwise, the int with the same numerical value as {@code val} is returned
		 * @param val
		 * @return
		 */
		private static int toInt(long val) {
			return Math.clamp(val, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}

		/**
		 * If the range of the slider would be larger than that of an int, then the value of 
		 * {@code maximum} is returned that would provided the largest range without overflowing an
		 * int
		 * @param minimum
		 * @param maximum
		 * @return
		 */
		private long clipMaximum(long minimum, long maximum) {
			long range = (maximum - minimum) / TIMESTEP + width;
			if (range > Integer.toUnsignedLong(Integer.MAX_VALUE - Integer.MIN_VALUE)) {
				System.err.println("maximum was clipped");
				return minimum + TIMESTEP * (((long) Integer.MAX_VALUE) - Integer.MIN_VALUE - width);
			}
			return maximum;
		}

		public void updateBounds(long minimum, long maximum, long current) {
			clipMaximum(minimum, maximum);
			
			min = minimum;
			max = maximum;

			slider.setValues((int) ((current - min)/TIMESTEP + LOWER_BOUND),width, LOWER_BOUND, (int) ((max - min)/TIMESTEP + LOWER_BOUND + width));
			minLabel.setText(Long.toString(min));
			maxLabel.setText(Long.toString(max));
			currentLabel.setText(Long.toString(current));

		}

		public void updatePosition(long current) {
			slider.setValue((int) ((current - min)/TIMESTEP +LOWER_BOUND));
			currentLabel.setText(Long.toString(current));//currentLabel.setText(Long.toString(getValue()));
		}

		/**
		 * Computes the generation from the provided slider value
		 * @param val
		 * @return
		 */
		private long adjustValue(int val) {
			return min + Integer.toUnsignedLong(val - LOWER_BOUND) * TIMESTEP;
		}

		public void setVisibleAmount(int width) {
			this.width = width;
			max = clipMaximum(min,max);
			maxLabel.setText(Long.toString(max));
			slider.setValues(slider.getValue(), width, LOWER_BOUND, (int) ((max-min)/TIMESTEP + LOWER_BOUND + width));
		}

		public long getGeneration() {
			return adjustValue(slider.getValue());
		}

		public void addAdjustmentListener(AdjustmentListener l) {
			slider.addAdjustmentListener(l);
		}

		public void removeAdjustmentListener(AdjustmentListener l) {
			slider.removeAdjustmentListener(l);
		}

		/*public int getBlockIncrement() {
			return slider.getBlockIncrement();
		}

		public int getMaximum() {
			return slider.getMaximum();
		}
		
		public int getMinimum() {
			return LOWER_BOUND;
		}

		public int getOrientation() {
			return HORIZONTAL;
		}

		public int getUnitIncrement() {
			return slider.getUnitIncrement();
		}

		public int getValue() {
			return slider.getValue();
		}

		public int getVisibleAmount() {
			return width;
		}

		public void setBlockIncrement(int b) {
			slider.setBlockIncrement(b);
		}

		public void setMaximum(int max) {
			slider.setMaximum(max);
		}

		public void setMinimum(int min) {
			setMaximum(slider.getMaximum() - (min - LOWER_BOUND));
		}
		
		public void setUnitIncrement(int u) {
			slider.setUnitIncrement(u);
		}

		public void setValue(int v) {
			slider.setValue(v);
		}*/

		private class LabelUpdate implements AdjustmentListener {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				currentLabel.setText(Long.toString(adjustValue(e.getValue())));
			}
		}
	}

	private class SliderAdjustment implements AdjustmentListener {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			if ((!e.getValueIsAdjusting())) {
				//Scrollbar source = (Scrollbar) e.getSource();
				parent.requestGen(sliders.getGeneration());
			}
		}
	}
}
