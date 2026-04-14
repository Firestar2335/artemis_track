import ODM.StateVector;
import ODM.KeplerElements;

import ExtraMath.Vector3D;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics;

import javax.swing.*;
//import javax.swing.text.*;

import java.io.File;

import java.util.Scanner;

public class NavballTests {
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Navball nb = new Navball(new File("./data"),300);
		//nb.setVectors(new Vector3D(1,0,0),new Vector3D(0.4,-0.3,-0.2),new Vector3D(-0.2,-0.2,0.8));
		nb.setVectors(Vector3D.X_UNIT, Vector3D.Y_UNIT, Vector3D.Z_UNIT);
		//Image im = frame.createImage(nb.getProducer());
		//Graphics g = frame.getGraphics();
		frame.add(nb);
		//frame.setSize(517,537);
		frame.setSize(300,300);
		frame.setVisible(true);
		Scanner in = new Scanner(System.in);
		Scanner line;
		boolean keepGoing = true;
		double yaw = Double.NaN;
		double pitch = Double.NaN;
		double roll = Double.NaN;
		do {
			System.out.print("Type the parameters > ");
			line = new Scanner(in.nextLine());
			while (line.hasNext()) {
				if (!line.hasNextDouble()) {
					String next = line.next();
					if (next.equalsIgnoreCase("quit")) {
						keepGoing = false;
						line.close();
						break;
					}
					else {
						System.out.println("Unrecognized number: "+next);
					}
				}
				else if (Double.isNaN(yaw)) {
					yaw = line.nextDouble();
				}
				else if (Double.isNaN(pitch)) {
					pitch = line.nextDouble();
				}
				else {
					roll = line.nextDouble();
					nb.setAngle(yaw,pitch,roll);
					//g.drawImage(im, 0,0,frame);
					nb.repaint();//frame.update(g);
					yaw=Double.NaN;pitch=Double.NaN;roll=Double.NaN;
				}
			}
			line.close();

		} while (keepGoing);
		in.close();
		frame.dispose();
	}
}
