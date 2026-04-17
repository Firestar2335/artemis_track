import ExtraMath.Quaternion;
import java.util.Scanner;

public class AngleTests {

	public static boolean isNegativeZero(double n) {
		return n == 0 && 1.0/n < 0;//(n==0) && Double.compare(n,0) < 0;
	}
	public static void main(String[] args) {
		System.out.println(isNegativeZero(-0.0));
		System.out.println(isNegativeZero(0.0));
		System.out.println(Math.copySign(1.0d, -0.0));
		System.exit(0);
		Scanner in = new Scanner(System.in);
		Scanner line;
		boolean cont = true;
		double comp[] = new double[4];
		int count = 0;
		Quaternion q;
		EulerAngles ang;
		while (cont) {
			System.out.print("Type the components of the quaternion > ");
			line = new Scanner(in.nextLine());
			while (line.hasNext()) {
				if (!line.hasNextDouble()) {
					String next = line.next();
					if (next.equalsIgnoreCase("quit")) {
						cont = false;
						break;
					}
					else {
						System.out.print("Unrecognized number: ");
						System.out.println(line.next());
						continue;
					}
				}
				comp[count++] = line.nextDouble();
				if (count == comp.length) {
					q = new Quaternion(comp[0], comp[1], comp[2], comp[3]);
					ang = EulerAngles.fromQuaternion(q);
					System.out.print("Angles for ");
					System.out.print(q);
					System.out.print(": ");
					System.out.println(ang);
					count = 0;
				}
			}
			line.close();
		}
		in.close();
	}
}
