import ExtraMath.*;
import ODM.StateVector;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.geom.Ellipse2D;

/**
 * Ideally, I would have this done on the GPU, but I am not smart enough to figure out how to do that 
 * in a few days so I am just doing it this way
 */
@SuppressWarnings("unused")
public class Navball extends Canvas {
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

	//private Color bg;
	private int bg;
	private boolean bgSet;

	/** The image that is being made */
	private Image totalImage;

	private Image backgroundImage;

	//private VolatileImage offscreen;
	private Image offscreen;

	private DelayResend delay;


	public Navball(File imageDir, int width) {
		this(imageDir, "IVANavBall.png", false, width);
	}

	public Navball(File imageDir, String navballTexture, boolean debug, int width) {
		super();
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
		offscreen = null;//createImage(width,width);//createVolatileImage(width,width);
		//setBackground(DEFAULT_BG);

		bg = 0;
		bgSet = false;

		//createBufferStrategy(2);
		delay = null;//new DelayResend(50);
	}

	/**
	 * Samples a color given the pixel coordinates centered at the top left corner from the image 
	 * in this object.
	 * @param x
	 * @param y
	 * @return
	 */// * @throws IllegalArgumentException if {@code x} or {@code y} is negative
	private int sample(double x, double y) {
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

	private static int compositeFour(int a, int b, int c, int d, double fracA, double fracB, double fracC) {
		return compositeFour(a,b,c,d,(float)fracA,(float)fracB,(float)fracC);
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
	private static int compositeFour(int a, int b, int c, int d, float fracA, float fracB, float fracC) {
		if (fracA < 0) {
			throw new IllegalArgumentException("fracA was negative");
		}
		else if (fracB < 0) {
			throw new IllegalArgumentException("fracB was negative");
		}
		else if (fracC < 0) {
			throw new IllegalArgumentException("fracC was negative");
		}
		float fracD = 1 - (fracA + fracB + fracC);
		if (fracD < 0) {
			throw new IllegalArgumentException("The values provided for fracA, fracB, and fracC would force fracD to be negative");
		}

		float red = 0;
		float green = 0;
		float blue = 0;

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

		return 0xff000000 | (((int)Math.round(red))<<16) | (((int) Math.round(green)) << 8) | (int) Math.round(blue);
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
		prod.resendAllAsync();
	}

	public ImageProducer getProducer() {
		return prod;
	}

	public void updateWidth(int newWidth) {
		//prod.setWidth(width);
		boolean change = (width != newWidth);
		this.width = newWidth;
		if (change) {
			if (delay == null || !delay.isAlive()) {
				delay = new DelayResend(10l);
				delay.start();
			}
			else {
				delay.reset();
			}
		}
		//prod.resendAllAsync();
	}

	private int getColor(int x, int y, int width) {
		double rad = width/2.0;
		if (Math.hypot(x-rad,y-rad) > rad) {
			if (!bgSet) {
				bg = getBackground().getRGB();
				bgSet = true;
			}
			return bg;//getBackground();//TRANSPARENT;
		}
		Vector3D p = fromYZ(x-rad,rad-y);
		p = att.conjugation(p);
		double[] polar = p.toPolarForm();
		return sample(-polar[1]*(textureWidth-1)/Math.TAU+(textureWidth-1)/2.0,(textureHeight-1)/2.0+polar[2]*(textureHeight-1)/Math.PI);
	}

	public void paint(Graphics g) {
		//BufferStrategy b = getBufferStrategy();
		if (offscreen == null || offscreen.getWidth(this) != width || offscreen.getHeight(this) != width) {
			offscreen = createImage(width, width);
		}
		Graphics buf = null;
		try {
			buf = offscreen.getGraphics();
			drawToGraphics(buf);
			//g.drawImage(offscreen,0,0,null);
		}
		finally {
			if (buf != null) {
				buf.dispose();
			}
		}
		//drawToGraphics(g);
		/*if (offscreen == null) {
			offscreen = createVolatileImage(width,width);
		}
		do {
			int returnCode = offscreen.validate(getGraphicsConfiguration());
			if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE || offscreen.getWidth() != width || offscreen.getHeight() != width) {
				offscreen = createVolatileImage(width,width);
				prod.resendAll();
			}
			Graphics offG = offscreen.createGraphics();
			drawToGraphics(offG);
			offG.dispose();
			
		} while(offscreen.contentsLost());*/
		g.drawImage(offscreen,0,0,this);
	}

	private void drawToGraphics(Graphics g) {
		if (backgroundImage == null) {
			backgroundImage = createImage(prod);
		}
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, width, width);
		Shape clip = g.getClip();
		g.drawImage(backgroundImage, 0, 0, width, width, this);
		//g.drawImage(backgroundImage,0,0, width, width, getBackground(), this);
		g.setClip(new Ellipse2D.Double(0, 0, width, width));
		if (prograde != null) {
			drawVector(g, prograde, progradeImage, 1.0, clip);
			drawVector(g,prograde.negate(),retrogradeImage, 1.0, clip);
		}
		if (normal != null) {
			drawVector(g, normal, normalImage, 1.0, clip);
			drawVector(g, normal.negate(), antinormalImage, 1.0, clip);
		}
		if (radialOut != null) {
			drawVector(g, radialOut, radialOutImage, 1.0, clip);
			drawVector(g, radialOut.negate(), radialInImage, 1.0, clip);
		}
		drawCursor(g, 1.0);
		g.setClip(clip);
	}

	private void drawCursor(Graphics g, double scale) {
		g.drawImage(cursor,(int) (width/2.0-55*scale),(int) (width/2.0-6*scale), 
					(int) Math.ceil(cursor.getWidth(this)*scale), (int) Math.ceil(cursor.getHeight(this)*scale),this);
	}

	private void drawVector(Graphics g, Vector3D vec, Image vectorImage, double scale, Shape oldClip) {
		Vector3D result = att.conjugate().conjugation(vec);
		if (result.x >= 0) {
			int w = vectorImage.getWidth(this), h = vectorImage.getHeight(this);
			int x = (int) ( (result.y + 1) * width/2.0 - w*scale/2.0);
			int y = (int) ((-result.z + 1) * width/2.0 - h*scale/2.0);
			g.drawImage(vectorImage,x,y, (int) Math.ceil(w * scale), (int) Math.ceil(h * scale),this);
		}
	}

	public void update(Graphics g) {
		paint(g);
	}

	private Vector3D fromYZ(double y, double z) {
		double x = Math.sqrt(width*width/4.0- y*y-z*z);
		return new Vector3D(x,y,z);
	}
	
	//#region Component methods

	public boolean isDoubleBuffered() {
		return true;
	}

	public Dimension getPreferredSize() {
		return new Dimension(prefWidth,prefWidth);
	}

	//public Dimension getMinimumSize() {
	//	return new Dimension(prefWidth, prefWidth);
	//}

	//public void setMinimumSize(Dimension minimumSize) {
	//	if (minimumSize != null) {
	//		prefWidth = Math.min(minimumSize.height, minimumSize.width);
	//	}
	//	else {
	//		prefWidth = width;
	//	}
	//	super.setMinimumSize(new Dimension(prefWidth, prefWidth));
	//}

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
		return super.getBackground();//(bgSet) ? super.getBackground() : new Color(bg,true);
	}

	public boolean isBackgroundSet() {
		return super.isBackgroundSet();//bg == null;
	}

	public void setBackground(Color c) {
		bg = c.getRGB();
		bgSet = true;
		super.setBackground(c);
	}

	//#endregion Component methods


	private class DelayResend extends Thread {
		private long delay;
		private volatile boolean keepGoing;

		/**
		 * 
		 * @param delay The number of milliseconds to buffer resend requests
		 */
		public DelayResend(long delay) {
			super();
			this.delay = delay;
			keepGoing = true;
			setDaemon(true);
		}

		public void reset() {
			keepGoing = true;
			interrupt();
		}

		public void run() {
			while (keepGoing) {
				keepGoing = false;
				try {
					sleep(delay);
					prod.resendAll();
					break;
				} catch (InterruptedException e) {
					continue;
				}
				//try {prod.join();} catch (InterruptedException e) {break;}
			}
		}
	}

	private class NavBallProducer implements ImageProducer {
		//private int width;

		private Set<ImageConsumer> consumers;

		private Set<Thread> runningTasks;

		//private ConcurrentMap<ImageConsumer, Thread> running;

		//private Thread sender;

		public NavBallProducer() {
			//sender = new Thread(new ThreadSender());
			consumers = new HashSet<>();
			runningTasks = new CopyOnWriteArraySet<>();
			//running = new ConcurrentHashMap<>();

		}

		public boolean isConsumer(ImageConsumer ic) {
			return consumers.contains(ic);
			//return running.containsKey(ic);
		}

		public void removeConsumer(ImageConsumer ic) {
			consumers.remove(ic);
			/*if (running.containsKey(ic)) {
				Thread current = running.get(ic);
				current.interrupt();
				while (current.isAlive()) {
					try {
						current.join(100);
					} catch (InterruptedException e) {
					}
				}
				running.remove(ic);
			}*/
		}

		public void addConsumer(ImageConsumer ic) {
			consumers.add(ic);
			//running.put(ic,null);
			sendAsync(ic);
		}

		public void startProduction(ImageConsumer ic) {
			consumers.add(ic);
			sendAsync(ic);
		}

		public void requestTopDownLeftRightResend(ImageConsumer ic) {
			consumers.add(ic);
			sendAsync(ic);
		}

		/**
		 * Sends the image data to the specified consumer
		 * @param cons
		 */
		private void send(ImageConsumer cons, int pictureWidth) {
			cons.setColorModel(ARGB);
			cons.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS);			
			Thread current = Thread.currentThread();

			/*int[] scanline;
			for (int r = 0; r < pictureWidth; r++) {
				scanline = new int[pictureWidth];
				for (int c = 0; c < width; c++) {
					try {
						scanline[c] = getColor(c,r,pictureWidth).getRGB();
					}
					catch (RuntimeException e) {
						cons.imageComplete(ImageConsumer.IMAGEERROR);
						throw e;
					}
				}
				if (current.isInterrupted()) {
					cons.imageComplete(ImageConsumer.IMAGEABORTED);
					return;//throw new InterruptedException();
				}
				cons.setPixels(0,r,pictureWidth,1,ARGB,scanline,0,pictureWidth);
			}*/
			int[] lines = new int[pictureWidth*pictureWidth];
			for (int r = 0; r < pictureWidth; r++) {
				for (int c = 0; c < pictureWidth; c++) {
					try {
						lines[r*pictureWidth+c] = getColor(c,r,pictureWidth);//.getRGB();
					} catch (RuntimeException e) {
						cons.imageComplete(ImageConsumer.IMAGEERROR);
						throw e;
					}
					if (current.isInterrupted()) {
						cons.imageComplete(ImageConsumer.IMAGEABORTED);
						return;//throw new InterruptedException();
					}
				}
			}
			synchronized (cons) {
				cons.setDimensions(pictureWidth, pictureWidth);
				cons.setPixels(0,0,pictureWidth,pictureWidth,ARGB,lines,0,pictureWidth);
				cons.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
			}
		}

		private void sendAsync(ImageConsumer recipient) {
			Thread sender = new ThreadSender(recipient, width);//new Thread(new ThreadSender(recipient, width));
			/*running.putIfAbsent(recipient,sender);
			Thread current = running.get(recipient);
			while (!current.equals(sender)) {
				current.interrupt();
				try {
					current.join();
				} catch (InterruptedException e) {}
				running.putIfAbsent(recipient, sender);
				current = running.get(recipient);
			}*/
			//running.put(recipient,sender);
			//runningTasks.add(sender);
			sender.start();
		}

		public void resendAll() {
			for (ImageConsumer cons : consumers) {
				send(cons, width);
			}
		}

		public void resendAllAsync() {
			for (ImageConsumer cons : consumers) {
				sendAsync(cons);
			}
			/*while (sender != null && sender.isAlive()) {
				try {
					sender.join();
				} catch (InterruptedException e) {

				}
			}
			sender = new Thread(new ThreadSender());
			sender.start();*/
		}

		public void join() throws InterruptedException {
			for (Thread currentlyRunning : runningTasks) {
				currentlyRunning.join();
			}
		}

		public boolean isDone() {
			for (Thread t : runningTasks) {
				if (t.isAlive()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Removes any threads that aren't alive
		 */
		private void clean() {
			runningTasks.removeIf((Thread t) -> (t.getState() == Thread.State.TERMINATED));
		}

		private class ThreadSender extends Thread {
			private final ImageConsumer cons;
			private int pictureWidth;

			public ThreadSender(ImageConsumer consumer, int pictureWidth) {
				super();
				cons = consumer;
				this.pictureWidth = pictureWidth;
				setDaemon(true);
			}

			public void run() {
				send(cons, pictureWidth);
				//runningTasks.remove(this);
			}
		}
	}
}
