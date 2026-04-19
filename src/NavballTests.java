import ODM.StateVector;

import ExtraMath.Vector3D;
import ExtraMath.Quaternion;

import javax.swing.*;
import java.awt.*;
//import javax.swing.text.*;

import java.io.File;

import java.util.Scanner;

public class NavballTests {
	private static final Quaternion INIT_ATTITUDE = new Quaternion(1,0,0,0);
	private static final StateVector INIT_STATE = new StateVector(null, new Vector3D(-0.2,0,1),new Vector3D(1,0,0),null,"");

	private static final int SCREEN_WIDTH = 2560;
	private static final int SCREEN_HEIGHT = 1600;

	static JFrame frame;
	static JFrame debugFrame;
	static Navball nb;

	static Navball debugNB;

	public static void main(String[] args) {
		setupFrame();

		int elements = 10;
		double[] vals = new double[elements];
		int count = 0;
		Scanner in = new Scanner(System.in);
		Scanner line;
		boolean keepGoing = true;
		do {
			prompt(count);
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
						continue;
					}
				}
				vals[count++] = line.nextDouble();
				if (count == elements) {
					Vector3D pos = new Vector3D(vals[0],vals[1],vals[2]);
					Vector3D vel = new Vector3D(vals[3],vals[4],vals[5]);
					Quaternion attitude = new Quaternion(vals[6],vals[7],vals[8],vals[9]).unit();
					StateVector state = new StateVector(null, pos,vel,null,"");
					nb.updateAngles(attitude, state);
					debugNB.updateAngles(attitude, state);
					nb.repaint();
					debugNB.repaint();
					count = 0;
				}
			}
			line.close();

		} while (keepGoing);
		in.close();
		frame.dispose();
		debugFrame.dispose();
	}

	public static void prompt(int count) {
		System.out.print("Type the ");
		switch (count) {
			case 0: System.out.print("position x, y, z, velocity x, y, z, and quaternion w, i, j, and k components"); break;
			case 1: System.out.print("position y, z, velocity x, y, z, and quaternion w, i, j, and k components"); break;
			case 2: System.out.print("position z, velocity x, y, z, and quaternion w, i, j, and k components"); break;
			case 3: System.out.print("velocity x, y, z, and quaternion w, i, j, and k components"); break;
			case 4: System.out.print("velocity y, z, and quaternion w, i, j, and k components"); break;
			case 5: System.out.print("velocity z, and quaternion w, i, j, and k components"); break;
			case 6: System.out.print("quaternion w, i, j, and k components"); break;
			case 7: System.out.print("quaternion i, j, and k components"); break;
			case 8: System.out.print("quaternion j and k components"); break;
			case 9: System.out.print("quaternion k component"); break;
			default: System.out.print("parameters"); break;
		}
		System.out.print(" > ");
	}

	public static void setupFrame() {
		frame = new JFrame();
		debugFrame = new JFrame();
		nb = new Navball(new File("./data"), "IVANavBall.png", false,300);
		debugNB = new Navball(new File("./data"), "debugNavball.png", true,300);
		//nb.setVectors(new Vector3D(1,0,0),new Vector3D(0.4,-0.3,-0.2),new Vector3D(-0.2,-0.2,0.8));
		//nb.setVectors(Vector3D.X_UNIT, Vector3D.Y_UNIT, Vector3D.Z_UNIT);
		nb.updateAngles(INIT_ATTITUDE,INIT_STATE);
		debugNB.updateAngles(INIT_ATTITUDE,INIT_STATE);
		//Image im = frame.createImage(nb.getProducer());
		//Graphics g = frame.getGraphics();
		frame.add(nb);
		debugFrame.add(debugNB);
		//frame.setSize(517,537);
		//Insets ins = frame.getInsets();
		//frame.setSize(300+ins.left+ins.right,300+ins.top+ins.bottom);
		//System.out.println(ins);
		//System.out.println(frame.getInsets());
		frame.pack();
		//frame.setSize(316,339);
		frame.setLocation((int) (SCREEN_WIDTH * 0.7), (int) (SCREEN_HEIGHT * 0.01));
		debugFrame.setSize(316,339);
		debugFrame.setLocation((int) (SCREEN_WIDTH * 0.85), (int) (SCREEN_HEIGHT * 0.01));
		frame.setVisible(true);
		debugFrame.setVisible(true);
	}
}
