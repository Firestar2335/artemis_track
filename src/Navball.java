import ExtraMath.*;
import ODM.StateVector;
import java.io.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;
import java.awt.geom.Ellipse2D;

/**
 * Ideally, I would have this done on the GPU, but I am not smart enough to figure out how to do that 
 * in a few days so I am just doing it this way
 */
@SuppressWarnings("unused")
public class Navball extends Component {
	private static final Color TRANSPARENT = new Color(0.0f,0.0f,0.0f,0.0f);

	private Image im;

	private static final double EPSILON = 1e-10;

	/** For roll */
	private double[][] rollMat;

	private int width;

	//private final File image;
	private final BufferedImage navball;
	private final int textureWidth;
	private final int textureHeight;

	private Image progradeImage;
	private Image retrogradeImage;
	private Image radialInImage;
	private Image radialOutImage;
	private Image normalImage;
	private Image antinormalImage;

	private Image cursor;

	private double yaw;
	private double pitch;
	private double roll;

	private Vector3D prograde;
	private Vector3D normal;
	private Vector3D radialOut;

	private NavBallProducer prod;

	private static final ColorModel ARGB = ColorModel.getRGBdefault();

	public Navball(File imageDir, int width) {
		try {
			navball = ImageIO.read(new File(imageDir, "IVANavBall.png"));
			progradeImage = ImageIO.read(new File(imageDir, "prograde.png"));
			retrogradeImage = ImageIO.read(new File(imageDir, "retrograde.png"));
			radialInImage = ImageIO.read(new File(imageDir, "radialIn.png"));
			radialOutImage = ImageIO.read(new File(imageDir, "radialOut.png"));
			normalImage = ImageIO.read(new File(imageDir, "normal.png"));
			antinormalImage = ImageIO.read(new File(imageDir, "antinormal.png"));
			cursor = ImageIO.read(new File(imageDir, "NavBallCursor.png"));
		}
		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		textureWidth = navball.getWidth();
		textureHeight = navball.getHeight();
		this.width = width;
		prod = new NavBallProducer();
		yaw = 0;
		pitch = 0;
		roll = 0;
		rollMat = new double[][]{{1,0},{0,1}};
	}

	/**
	 * Samples a color given the pixel coordinates centered at the top left corner from the image 
	 * in this object.
	 * @param x
	 * @param y
	 * @return
	 */// * @throws IllegalArgumentException if {@code x} or {@code y} is negative
	private Color sample(double x, double y) {
		while (y < 0 || y >= textureHeight) {
			if (y < 0) {
				y = -y;
			}
			else {
				y = 2*textureHeight - 2 - y;
			}
			x += textureWidth/2.0;
		}
		x = x % textureWidth;
		if (x < 0) {
			x += textureWidth;
		}
		//if (x < 0) {
		//	throw new IllegalArgumentException("x was negative");
		//}
		//else if (y < 0) {
		//	throw new IllegalArgumentException("y was negative");
		//}
		int cX = (int) x;
		double tX = x-cX;

		int cY = (int) y;
		double tY = y-cY;

		int nextX = (cX + 1) % textureWidth;
		int nextY = Math.min(cY+1,textureHeight-1);

		int upperLeft = navball.getRGB(cX,cY);
		int upperRight = navball.getRGB(nextX, cY);
		int lowerLeft = navball.getRGB(cX, nextY);
		int lowerRight = navball.getRGB(nextX,nextY);

		return compositeFour(lowerRight,lowerLeft,upperRight,upperLeft,tX*tY,(1-tX)*tY,tX*(1-tY));
	}

	/**
	 * Composites four colors. The colors are represented with the red in bits 16-23, the green in 
	 * bits 8-15, and the blue in bits 0-7
	 * @param a Color A
	 * @param b Color B
	 * @param c Color C
	 * @param d Color D
	 * @param fracA The fraction contributed by color A
	 * @param fracB The fraction contributed by color B
	 * @param fracC The fraction contributed by color C
	 * @return The composite color
	 * @throws IllegalArgumentException if any of {@code fracA}, {@code fracB}, or {@code fracC} 
	 * are negative or if {@code fracA + fracB + fracC > 1}
	 */
	private static Color compositeFour(int a, int b, int c, int d, double fracA, double fracB, double fracC) {
		if (fracA < 0) {
			throw new IllegalArgumentException("fracA was negative");
		}
		else if (fracB < 0) {
			throw new IllegalArgumentException("fracB was negative");
		}
		else if (fracC < 0) {
			throw new IllegalArgumentException("fracC was negative");
		}
		double fracD = 1 - (fracA + fracB + fracC);
		if (fracD < 0) {
			throw new IllegalArgumentException("The values provided for fracA, fracB, and fracC would force fracD to be negative");
		}

		double red = 0;
		double green = 0;
		double blue = 0;

		blue = (a & 0xff) * fracA;
		green = ((a >> 8) & 0xff) * fracA;
		red = ((a >> 16) & 0xff) * fracA;

		blue += (b & 0xff) * fracB;
		green += ((b >> 8) & 0xff) * fracB;
		red += ((b >> 16) & 0xff) * fracB;
		
		blue += (c & 0xff) * fracC;
		green += ((c >> 8) & 0xff) * fracC;
		red += ((c >> 16) & 0xff) * fracC;

		blue += (d & 0xff) * fracD;
		green += ((d >> 8) & 0xff) * fracD;
		red += ((d >> 16) & 0xff) * fracD;

		return new Color((int)Math.round(red), (int) Math.round(green), (int) Math.round(blue));
	}

	/**
	 * Composites four colors.
	 * @param a Color A
	 * @param b Color B
	 * @param c Color C
	 * @param d Color D
	 * @param fracA The fraction contributed by color A
	 * @param fracB The fraction contributed by color B
	 * @param fracC The fraction contributed by color C
	 * @return The composite color
	 * @throws IllegalArgumentException if any of {@code fracA}, {@code fracB}, or {@code fracC} 
	 * are negative or if {@code fracA + fracB + fracC > 1}
	 */
	private static Color compositeFour(Color a, Color b, Color c, Color d, double fracA, double fracB, double fracC) {
		if (fracA < 0) {
			throw new IllegalArgumentException("fracA was negative");
		}
		else if (fracB < 0) {
			throw new IllegalArgumentException("fracB was negative");
		}
		else if (fracC < 0) {
			throw new IllegalArgumentException("fracC was negative");
		}
		double fracD = 1 - (fracA + fracB + fracC);
		if (fracD < 0) {
			throw new IllegalArgumentException("The values provided for fracA, fracB, and fracC would force fracD to be negative");
		}
		double[] newComp = new double[3];
		float[] colorComp = new float[3];

		if (fracA > 0) {
			a.getRGBColorComponents(colorComp);
			for (int i = 0; i < 3; i++) {
				newComp[i] += colorComp[i] * fracA;
			}
		}
		if (fracB > 0) {
			b.getRGBColorComponents(colorComp);
			for (int i = 0; i < 3; i++) {
				newComp[i] += colorComp[i] * fracB;
			}
		}
		if (fracC > 0) {
			c.getRGBColorComponents(colorComp);
			for (int i = 0; i < 3; i++) {
				newComp[i] += colorComp[i] * fracC;
			}
		}
		if (fracD > 0) {
			d.getRGBColorComponents(colorComp);
			for (int i = 0; i < 3; i++) {
				newComp[i] += colorComp[i] * fracD;
			}
		}

		return new Color((float) newComp[0], (float) newComp[1], (float) newComp[2]);
	}

	private Color getColor(int x, int y, int width) {
		double radius = width/2.0;
		double[] p = new double[]{x-radius,y-radius};
		if (Math.hypot(p[0],p[1]) > radius) {
			return TRANSPARENT;
		}
		p = matMul(rollMat, p);
		p = orthographic(-p[0],p[1],yaw,-pitch,radius);
		return sample(p[0]*(textureWidth-1)/Math.TAU+(textureWidth-1)/2.0,(textureHeight-1)/2.0-p[1]*(textureHeight-1)/Math.PI);
	}

	public void updateAngles(Quaternion attitude, StateVector state) {
		//Construct matrix of basis vectors
		Vector3D zBasis = state.pos.unit();
		Vector3D yBasis = zBasis.cross(Vector3D.Z_UNIT);
		if (yBasis.mag() <= EPSILON) {
			yBasis = Vector3D.Y_UNIT;
		}
		else {
			yBasis = yBasis.unit();
		}
		Vector3D xBasis = yBasis.cross(zBasis);

		Matrix basis = Matrix.fromVectorColumns(xBasis,yBasis,zBasis);

		//Matrix basisChange = basis.inverse();
		Matrix basisChange = basis.transpose();//This is an orthonormal matrix, so its inverse is its transpose

		Matrix newRotation = attitude.toMatrix().mul(basisChange);

		prograde = new Vector3D(basisChange.mul(state.vel.unit()));
		normal = new Vector3D(basisChange.mul(state.pos.cross(state.vel).unit()));
		radialOut = prograde.cross(normal);

		yaw = Math.atan2(newRotation.get(1,0),newRotation.get(0,0));
		pitch = Math.asin(-newRotation.get(2,0));
		roll = Math.atan2(newRotation.get(2,1),newRotation.get(2,2));
		rollMat = genRotMat(-roll);
	}

	public void setAngle(double yaw, double pitch, double roll) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.roll = roll;
		rollMat = genRotMat(-this.roll);
		prod.resendAll();
	}

	/**
	 * Projects the given point in (x,y) space in a circle of the specified radius centered at the 
	 * origin to the corresponding longitude and latitude
	 * @param x
	 * @param y
	 * @param centerLongitude
	 * @param centerLatitude
	 * @param radius
	 * @return the tuple (longitude, latitude)
	 */
	private static double[] orthographic(double x, double y, double centerLongitude, double centerLatitude, double radius) {
		if (x == 0 && y == 0) {
			return new double[]{centerLongitude, centerLatitude};
		}
		double r = Math.hypot(x,y);
		double c = Math.asin(r/radius);

		double latitude = Math.asin(Math.cos(c)*Math.sin(centerLatitude)+y*Math.sin(c)*Math.cos(centerLatitude)/r);
		double longitude = centerLongitude+Math.atan2(x*Math.sin(c),r*Math.cos(c)*Math.cos(centerLatitude)-y*Math.sin(c)*Math.sin(centerLatitude));
		return new double[]{longitude, latitude};
	}

	/**
	 * Converts the provided longiude and latitude to a point in the cartesian plane through an 
	 * orthographic projection
	 * @param longitude
	 * @param latitude
	 * @param centerLongitude
	 * @param centerLatitude
	 * @param radius
	 * @return
	 */
	private static double[] toOrthographic(double longitude, double latitude, double centerLongitude, double centerLatitude, double radius) {
		double x = radius * Math.cos(latitude) * Math.sin(longitude - centerLongitude);
		double y = radius * (Math.cos(centerLatitude) * Math.sin(latitude) - Math.sin(centerLatitude)*Math.cos(latitude) * Math.cos(longitude-centerLongitude));
		return new double[]{x,y};
	}

	/**
	 * Produces the 2x2 rotation matrix for a counterclockwise rotation
	 * @param roll
	 * @return
	 */
	private static double[][] genRotMat(double roll) {
		double[][] rollMat = new double[2][2];
		rollMat[0][0] = Math.cos(roll);
		rollMat[0][1] = -Math.sin(roll);
		rollMat[1][0] = Math.sin(roll);
		rollMat[1][1] = Math.cos(roll);
		return rollMat;
	}

	private static double[] matMul(double[][] mat, double[] point) {
		if (mat.length != 2 || mat[0].length != 2 || point.length != 2) {
			throw new IllegalArgumentException();
		}
		return new double[]{mat[0][0]*point[0]+mat[0][1]*point[1],mat[1][0]*point[0]+mat[1][1]*point[1]};
	}

	public ImageProducer getProducer() {
		return prod;
	}

	public void updateWidth(int newWidth) {
		//prod.setWidth(width);
		this.width = newWidth;
		prod.resendAll();
	}

	public Dimension getPreferredSize() {
		return new Dimension(width,width);
	}

	public void setVectors(Vector3D prograde, Vector3D normal, Vector3D radialOut) {
		this.prograde = prograde;
		this.normal = normal;
		this.radialOut = radialOut;
	}

	public void paint(Graphics g) {
		if (im == null) {
			im = createImage(prod);
		}
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, width, width);
		g.drawImage(im,0,0, Color.BLACK, null);
		g.setClip(new Ellipse2D.Double(0, 0, width, width));
		double[][] mat = genRotMat(roll);
		if (prograde != null) {
			drawVector(g, prograde, progradeImage, mat);
			drawVector(g,prograde.negate(),retrogradeImage,mat);
		}
		if (normal != null) {
			drawVector(g, normal, normalImage, mat);
			drawVector(g, normal.negate(), antinormalImage, mat);
		}
		if (radialOut != null) {
			drawVector(g, radialOut, radialOutImage, mat);
			drawVector(g, radialOut.negate(), radialInImage, mat);
		}
		g.drawImage(cursor,width/2-55,width/2-6,null);
	}

	private void drawVector(Graphics g, Vector3D vec, Image im, double[][] mat) {
		double[] polar = vec.toPolarForm();
		polar[2] = -polar[2];
		double cosC = Math.sin(-pitch)*Math.sin(polar[2])+Math.cos(-pitch)*Math.cos(polar[2])*Math.cos(polar[1]-yaw);
		if (cosC > 0) {
			double radius = width / 2.0;
			double[] point = toOrthographic(polar[1],polar[2],yaw,-pitch,radius);
			point[0] = -point[0];
			point[1] = point[1];
			point = matMul(mat,point);
			point[0] = point[0]+radius;
			point[1] = point[1] + radius;
			int x = (int) (point[0] - im.getWidth(null)/2.0);
			int y = (int) (point[1] - im.getHeight(null)/2.0);
			g.drawImage(im,x,y,null);
		}
	}
	
	private class NavBallProducer implements ImageProducer {
		//private int width;

		private Set<ImageConsumer> consumers;

		public NavBallProducer() {
			//this.width = width;
			consumers = new HashSet<>();
		}

		//public int getWidth() {
		//	return width;
		//}

		//public void setWidth(int newWidth) {
		//	width = newWidth;
		//}

		public boolean isConsumer(ImageConsumer ic) {
			return consumers.contains(ic);
		}

		public void removeConsumer(ImageConsumer ic) {
			/*System.out.print("removeConsumer: ");
			System.out.println(ic);
			System.out.println(consumers);*/
			//throw new RuntimeException();
			boolean rm = consumers.remove(ic);
			//System.out.println(consumers);
			/*if (!rm) {
				throw new IllegalArgumentException("The provided consumer was not already a consumer");
			}*/
		}

		public void addConsumer(ImageConsumer ic) {
			//System.out.print("AddConsumer: ");
			//System.out.println(ic);
			consumers.add(ic);
			send(ic);
		}

		public void startProduction(ImageConsumer ic) {
			//System.out.print("startProduction: ");
			//System.out.println(ic);
			consumers.add(ic);
			send(ic);
		}

		public void requestTopDownLeftRightResend(ImageConsumer ic) {
			consumers.add(ic);
			send(ic);
		}

		/**
		 * Sends the image data to the specified consumer
		 * @param cons
		 */
		private void send(ImageConsumer cons) {
			cons.setColorModel(ARGB);
			cons.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS);
			cons.setDimensions(width, width);

			int[] scanline;
			for (int r = 0; r < width; r++) {
				scanline = new int[width];
				for (int c = 0; c < width; c++) {
					try {
						scanline[c] = getColor(c,r,width).getRGB();
					}
					catch (RuntimeException e) {
						cons.imageComplete(ImageConsumer.IMAGEERROR);
						throw e;
					}
				}
				cons.setPixels(0,r,width,1,ARGB,scanline,0,width);
			}
			cons.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
		}

		public void resendAll() {
			for (ImageConsumer cons : consumers) {
				send(cons);
			}
		}
	}
}
