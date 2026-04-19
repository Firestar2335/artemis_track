import ExtraMath.*;
import ODM.StateVector;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;
import java.awt.geom.Ellipse2D;

/**
 * Ideally, I would have this done on the GPU, but I am not smart enough to figure out how to do that 
 * in a few days so I am just doing it this way
 */
public class Navball extends Component {
	private static final ColorModel ARGB = ColorModel.getRGBdefault();
	private static final Color TRANSPARENT = new Color(0.0f,0.0f,0.0f,0.0f);
	//private static final Color DEFAULT_BG = Color.BLACK;
	private static final double EPSILON = 1e-10;

	private int width;

	private int prefWidth;

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

	private Quaternion att;

	private Vector3D prograde;
	private Vector3D normal;
	private Vector3D radialOut;

	private NavBallProducer prod;

	private boolean debug;

	private Color bg;

	/** The image that is being made */
	private Image im;

	public Navball(File imageDir, int width) {
		this(imageDir, "IVANavBall.png", false, width);
	}

	public Navball(File imageDir, String navballTexture, boolean debug, int width) {
		try {
			//navball = ImageIO.read(new File(imageDir, "IVANavBall.png"));
			//navball = ImageIO.read(new File(imageDir, "debugNavball.png"));
			navball = ImageIO.read(new File(imageDir, navballTexture));
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
		prefWidth = width;
		this.debug = debug;
		att = Quaternion.REAL_UNIT;
		prod = new NavBallProducer();
		//setBackground(DEFAULT_BG);
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
		x = ((x % textureWidth)+textureWidth) % textureWidth;
		/*if (x < 0) {
			x += textureWidth;
		}*/
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

	@SuppressWarnings("unused")
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


	/**
	 * Updates the state of this navball to match the provided vectors
	 * @param attitude The quaternion representing the rotation of this quaternion relative to the 
	 * positive x-axis of the reference frame
	 * @param state The state containing the position and velocity vectors of the spacecraft
	 */
	public void updateAngles(Quaternion attitude, StateVector state) {
		//Construct matrix of basis vectors
		Vector3D zBasis;
		if (state.pos.mag() <= EPSILON) {
			zBasis = Vector3D.Z_UNIT;
		}
		else {
			zBasis = state.pos.unit();
		}
		Vector3D yBasis = zBasis.cross(Vector3D.Z_UNIT);
		if (yBasis.mag() <= EPSILON) {
			yBasis = Vector3D.Y_UNIT;
		}
		else {
			yBasis = yBasis.unit();
		}
		Vector3D xBasis = yBasis.cross(zBasis);

		Matrix toOld = Matrix.fromVectorColumns(xBasis,yBasis,zBasis);
		Quaternion co;
		if (debug) {
			co = Quaternion.REAL_UNIT;
			att = attitude;
		}
		else {
			co = Quaternion.fromMatrix(toOld).conjugate().invertRoll();
			att = co.mul(attitude);
		}

		prograde = co.conjugation(state.vel.unit());
		normal = co.conjugation(state.pos.cross(state.vel).unit());
		radialOut = prograde.cross(normal);
		prod.resendAll();
	}

	public ImageProducer getProducer() {
		return prod;
	}

	public void updateWidth(int newWidth) {
		//prod.setWidth(width);
		this.width = newWidth;
		//prod.resendAll();
	}

	private Color getColor(int x, int y, int width) {
		double rad = width/2.0;
		if (Math.hypot(x-rad,y-rad) > rad) {
			return TRANSPARENT;
		}
		Vector3D p = fromYZ(x-rad,rad-y);
		p = att.conjugation(p);
		double[] polar = p.toPolarForm();
		return sample(-polar[1]*(textureWidth-1)/Math.TAU+(textureWidth-1)/2.0,(textureHeight-1)/2.0+polar[2]*(textureHeight-1)/Math.PI);
	}

	public void paint(Graphics g) {
		if (im == null) {
			im = createImage(prod);
		}
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, width, width);
		Shape clip = g.getClip();
		g.drawImage(im,0,0, getBackground(), null);
		g.setClip(new Ellipse2D.Double(0, 0, width, width));
		if (prograde != null) {
			drawVector(g, prograde, progradeImage, 1.0);
			drawVector(g,prograde.negate(),retrogradeImage, 1.0);
		}
		if (normal != null) {
			drawVector(g, normal, normalImage, 1.0);
			drawVector(g, normal.negate(), antinormalImage, 1.0);
		}
		if (radialOut != null) {
			drawVector(g, radialOut, radialOutImage, 1.0);
			drawVector(g, radialOut.negate(), radialInImage, 1.0);
		}
		drawCursor(g, 1.0);
		g.setClip(clip);
	}

	private void drawCursor(Graphics g, double scale) {
		g.drawImage(cursor,(int) (width/2.0-55*scale),(int) (width/2.0-6*scale), 
					(int) Math.ceil(cursor.getWidth(null)*scale), (int) Math.ceil(cursor.getHeight(null)*scale),null);
	}

	private void drawVector(Graphics g, Vector3D vec, Image vectorImage, double scale) {
		Vector3D result = att.conjugate().conjugation(vec);
		if (result.x >= 0) {
			int w = vectorImage.getWidth(null), h = vectorImage.getHeight(null);
			int x = (int) ( (result.y + 1) * width/2.0 - w*scale/2.0);
			int y = (int) ((-result.z + 1) * width/2.0 -h*scale/2.0);
			g.drawImage(vectorImage,x,y, (int) Math.ceil(w * scale), (int) Math.ceil(h * scale),null);
		}
	}

	private Vector3D fromYZ(double y, double z) {
		double x = Math.sqrt(width*width/4.0- y*y-z*z);
		return new Vector3D(x,y,z);
	}
	
	//#region Component methods

	public Dimension getPreferredSize() {
		return new Dimension(prefWidth,prefWidth);
	}

	public Dimension getMinimumSize() {
		return new Dimension(prefWidth, prefWidth);
	}

	public void setMinimumSize(Dimension minimumSize) {
		if (minimumSize != null) {
			prefWidth = Math.min(minimumSize.height, minimumSize.width);
		}
		else {
			prefWidth = width;
		}
		super.setMinimumSize(new Dimension(prefWidth, prefWidth));
	}

	public void setPreferredSize(Dimension preferredSize) {
		if (preferredSize != null) {
			prefWidth = Math.min(preferredSize.height, preferredSize.width);
		}
		else {
			prefWidth = width;
		}
		super.setPreferredSize(new Dimension(prefWidth, prefWidth));
	}

	public void setSize(int width, int height) {
		updateWidth(Math.min(width, height));
		super.setSize(width, height);
	}

	public void setSize(Dimension d) {
		if (d == null) {
			throw new NullPointerException();
		}
		updateWidth(Math.min(d.width, d.height));
		super.setSize(d);
	}

	public void setBounds(int x, int y, int width, int height) {
		updateWidth(Math.min(width, height));
		super.setBounds(x,y,width,height);
	}

	public void setBounds(Rectangle r) {
		if (r == null) {
			throw new NullPointerException();
		}
		updateWidth(Math.min(r.width, r.height));
		super.setBounds(r);
	}

	public Color getBackground() {
		return (bg == null) ? super.getBackground() : bg;
	}

	public boolean isBackgroundSet() {
		return bg == null;
	}

	public void setBackground(Color c) {
		bg = c;
		super.setBackground(c);
	}

	//#endregion Component methods


	private class NavBallProducer implements ImageProducer {
		//private int width;

		private Set<ImageConsumer> consumers;

		public NavBallProducer() {
			consumers = new HashSet<>();
		}

		public boolean isConsumer(ImageConsumer ic) {
			return consumers.contains(ic);
		}

		public void removeConsumer(ImageConsumer ic) {
			consumers.remove(ic);
		}

		public void addConsumer(ImageConsumer ic) {
			consumers.add(ic);
			send(ic);
		}

		public void startProduction(ImageConsumer ic) {
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
			int width = Navball.this.width;
			cons.setDimensions(width, width);

			/*int[] scanline;
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
			}*/
			int[] lines = new int[width*width];
			for (int r = 0; r < width; r++) {
				for (int c = 0; c < width; c++) {
					try {
						lines[r*width+c] = getColor(c,r,width).getRGB();
					} catch (RuntimeException e) {
						cons.imageComplete(ImageConsumer.IMAGEERROR);
						throw e;
					}
				}
			}
			cons.setPixels(0,0,width,width,ARGB,lines,0,width);
			cons.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
		}

		public void resendAll() {
			for (ImageConsumer cons : consumers) {
				send(cons);
			}
		}
	}
}
