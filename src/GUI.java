import ODM.StateVector;
import ODM.KeplerElements;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Image;

import javax.swing.*;
import javax.swing.text.*;

import java.time.Duration;

import java.io.File;

public class GUI {
	private static final Color BACKGROUND = new Color(255,255,208);

	private static final Color FOREGROUND = Color.BLACK;

	private final JFrame frame;

	private final UserInterface parent;

	private final JButton quitButton;

	private final JTextComponent currentData;

	private final Navball navball;

	public GUI(UserInterface parent) {
		this.parent = parent;
		frame = new JFrame("Artemis II Telemetry Data");
		frame.setSize(800,500);
		frame.setBackground(BACKGROUND);
		quitButton = new JButton("Exit");
		quitButton.addActionListener((ActionEvent e) -> {frame.dispose();parent.quit();});
		frame.getContentPane().add(BorderLayout.NORTH, quitButton);
		currentData = new JTextArea("Waiting for web...",20,110);
		currentData.setEditable(false);
		currentData.setBackground(BACKGROUND);
		frame.getContentPane().add(BorderLayout.SOUTH,currentData);
		frame.setVisible(true);
		navball = new Navball(new File("./data"),500);
		//navballImage = frame.createImage(navball.getProducer());
	}

	public void updateCurrentTelemetry(Duration elapsedTime,StateVector vectors, KeplerElements elements, EulerAngles attitude) {
		currentData.setText(UserInterface.formatDuration(elapsedTime)+"\n"+UserInterface.formatVectors(vectors)+"\n\n"+UserInterface.formatElements(elements)+"\n\n"+UserInterface.formatAngles(attitude));
	}
}
