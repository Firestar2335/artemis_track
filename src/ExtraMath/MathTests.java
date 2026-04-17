package ExtraMath;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MathTests {
	private static final double EPSILON = 1e-15;

	@Test
	@Tag("quaternion")
	@DisplayName("Quaternion add and subtract")
	public void testQuaternionAddSub() {
		Quaternion a = new Quaternion(1,0,0,0);
		Quaternion b = new Quaternion(2,0,0,0);
		assertEquals(new Quaternion(3,0,0,0),a.add(b),"1+2 was not 3");
		assertEquals(new Quaternion(-1,0,0,0),a.sub(b),"1-2 was not -1");
		a = new Quaternion(0,1,0,0);
		b = new Quaternion(0,2,0,0);
		assertEquals(new Quaternion(0,3,0,0),a.add(b),"1i+2i was not 3i");
		assertEquals(new Quaternion(0,-1,0,0),a.sub(b),"1i-2i was not -1i");
		a = new Quaternion(0,0,1,0);
		b = new Quaternion(0,0,2,0);
		assertEquals(new Quaternion(0,0,3,0),a.add(b),"1j+2j was not 3j");
		assertEquals(new Quaternion(0,0,-1,0),a.sub(b),"1j-2j was not -1j");
		a = new Quaternion(0,0,0,1);
		b = new Quaternion(0,0,0,2);
		assertEquals(new Quaternion(0,0,0,3),a.add(b),"1k+2k was not 3k");
		assertEquals(new Quaternion(0,0,0,-1),a.sub(b),"1k-2k was not -1k");
		a = new Quaternion(5,2,4,-5);
		b = new Quaternion(-4,2,-3,2);

		assertEquals(new Quaternion(1,4,1,-3),a.add(b),"(5+2i+4j-5k)+(-4+2i-3j+2k) was not (1+4i+j-3k)");
		assertEquals(new Quaternion(9,0,7,-7),a.sub(b), "(5+2i+4j-5k)+(-4+2i-3j+2k) was not (9+7j-7k)");
	}

	@Test
	@Tag("quaternion")
	@DisplayName("Quaternion magnitude")
	public void testQuaternionMag() {
		magTest(Quaternion.ZERO, 0, 0);
		magTest(new Quaternion(1,0,0,0), 1,1);
		magTest(new Quaternion(0,-5,0,0), 5,25);
		magTest(new Quaternion(0,0,3,-4),5,25);
		magTest(new Quaternion(-4.5, 6.5, -8.5, 9.5),15,225);
	}

	@Test
	@Tag("quaternion")
	@DisplayName("Quaternion inverse and conjugate")
	public void testQuaternionInverse() {
		testConj(new Quaternion(1,0,0,0),new Quaternion(1,0,0,0), new Quaternion(1,0,0,0));
		testConj(new Quaternion(0,-2,0,0),new Quaternion(0,2,0,0),new Quaternion(0,0.5,0,0));
		testConj(new Quaternion(-1,5,-5,7), new Quaternion(-1,-5,5,-7), new Quaternion(-0.01,-0.05,0.05,-0.07));
		testConj(new Quaternion(0.5,0.5,0.5,0.5),new Quaternion(0.5,-0.5,-0.5,-0.5),new Quaternion(0.5,-0.5,-0.5,-0.5));
	}

	@Test
	@Tag("quaternion")
	@DisplayName("Quaternion scalar multiplication")
	public void testQuaternionScale() {
		assertEquals(new Quaternion(2,4,-3,1), new Quaternion(1,2,-1.5,0.5).mul(2), "(1+2i-j+0.5k)*2 was not (2+4i-3j+k)");
		assertEquals(new Quaternion(-0.5,4, -10,-50), new Quaternion(-1,8,-20,-100).mul(0.5), "(-1+8i-20j-100k)*0.5 was not (-0.5+4i-10j-50k)");
	}

	@Test
	@Tag("quaternion")
	@DisplayName("Quaternion hamilton product")
	public void testQuaternionMultiplication() {
		Quaternion minusOne = new Quaternion(-1,0,0,0);
		testMultiply(Quaternion.REAL_UNIT, Quaternion.REAL_UNIT, Quaternion.REAL_UNIT);
		testMultiply(Quaternion.REAL_UNIT, Quaternion.I_UNIT, Quaternion.I_UNIT);
		testMultiply(Quaternion.REAL_UNIT, Quaternion.J_UNIT, Quaternion.J_UNIT);
		testMultiply(Quaternion.REAL_UNIT, Quaternion.K_UNIT, Quaternion.K_UNIT);

		testMultiply(Quaternion.I_UNIT, Quaternion.REAL_UNIT, Quaternion.I_UNIT);
		testMultiply(Quaternion.I_UNIT, Quaternion.I_UNIT, minusOne);
		testMultiply(Quaternion.I_UNIT, Quaternion.J_UNIT, Quaternion.K_UNIT);
		testMultiply(Quaternion.I_UNIT, Quaternion.K_UNIT, Quaternion.J_UNIT.negate());

		testMultiply(Quaternion.J_UNIT, Quaternion.REAL_UNIT, Quaternion.J_UNIT);
		testMultiply(Quaternion.J_UNIT, Quaternion.I_UNIT, Quaternion.K_UNIT.negate());
		testMultiply(Quaternion.J_UNIT, Quaternion.J_UNIT, minusOne);
		testMultiply(Quaternion.J_UNIT, Quaternion.K_UNIT, Quaternion.I_UNIT);

		testMultiply(Quaternion.K_UNIT, Quaternion.REAL_UNIT, Quaternion.K_UNIT);
		testMultiply(Quaternion.K_UNIT, Quaternion.I_UNIT, Quaternion.J_UNIT);
		testMultiply(Quaternion.K_UNIT, Quaternion.J_UNIT, Quaternion.I_UNIT.negate());
		testMultiply(Quaternion.K_UNIT, Quaternion.K_UNIT, minusOne);

		Quaternion a = new Quaternion(4,-2,4,1);
		Quaternion b = new Quaternion(-4,-7,9,-11);
		testMultiply(a,b,new Quaternion(-55,-73,-9,-38));
		testMultiply(b,a,new Quaternion(-55,33,49,-58));
	}

	private void testMultiply(Quaternion a, Quaternion b, Quaternion result) {
		assertEquals(result, a.mul(b), "("+a+")*("+b+") was not ("+result+")");
	}

	private void testConj(Quaternion val, Quaternion conj, Quaternion inv) {
		assertEquals(conj, val.conjugate(), "The conjugate of ("+val+") was not ("+conj+")");
		assertEquals(inv, val.inverse(), "The inverse of ("+val+") was not ("+inv+")");
	}

	private void magTest(Quaternion val, double expectedMag, double magSquared) {
		assertEquals(expectedMag, val.mag(), EPSILON, "The magnitude of ("+val.toString()+") was not "+expectedMag);
		assertEquals(magSquared, val.magSquared(), EPSILON, "The square of the magnitude of ("+val.toString()+") was not "+magSquared);
	}

	@Test
	@Tag("vector")
	@DisplayName("Vector add and subtract")
	public void testVectorAddSub() {
		Vector3D a = new Vector3D(1,0,0);
		Vector3D b = new Vector3D(2,0,0);
		assertEquals(new Vector3D(3,0,0), a.add(b), "<1,0,0>+<2,0,0> was not <3,0,0>");
		assertEquals(new Vector3D(-1,0,0), a.sub(b), "<1,0,0>-<2,0,0> was not <-1,0,0>");

		a = new Vector3D(0,1,0);
		b = new Vector3D(0,2,0);
		assertEquals(new Vector3D(0,3,0), a.add(b), "<0,1,0>+<0,2,0> was not <0,3,0>");
		assertEquals(new Vector3D(0,-1,0), a.sub(b), "<0,1,0>-<0,2,0> was not <0,-1,0>");

		a = new Vector3D(0,0,1);
		b = new Vector3D(0,0,2);
		assertEquals(new Vector3D(0,0,3), a.add(b), "<0,0,1>+<0,0,2> was not <0,0,3>");
		assertEquals(new Vector3D(0,0,-1), a.sub(b), "<0,0,1>-<0,0,2> was not <0,0,-1>");

		a = new Vector3D(4,-2,3);
		b = new Vector3D(-2,3,1);
		assertEquals(new Vector3D(2,1,4), a.add(b), "<4,-2,3>+<-2,3,1> was not <2,1,4>");
		assertEquals(new Vector3D(6,-5,2), a.sub(b), "<4,-2,3>-<-2,3,1> was not <6,-5,2>");
	}

	@Test
	@Tag("vector")
	@DisplayName("Vector scalar multiplication")
	public void testVectorScale() {
		Vector3D v = new Vector3D(4,2,-1);
		assertEquals(new Vector3D(4,2,-1), v.mul(1));
		assertEquals(new Vector3D(8,4,-2), v.mul(2));
		assertEquals(new Vector3D(-2,-1,0.5), v.mul(-0.5));
	}

	@Test
	@Tag("vector")
	@DisplayName("Vector dot product")
	public void testDotProduct() {
		Vector3D v = new Vector3D(1,2,3);
		Vector3D u = new Vector3D(2,-0.25,0);
		assertEquals(14,v.dot(v));
		assertEquals(4.0625,u.dot(u));
		assertEquals(1.5, v.dot(u));
		assertEquals(1.5,u.dot(v));
	}

	@Test
	@Tag("vector")
	@DisplayName("Vector cross product")
	public void testCrossProduct() {
		Vector3D u = new Vector3D(4,2,3);
		Vector3D v = new Vector3D(-3,1,0.5);
		Vector3D w = new Vector3D(-1,5,-5.25);
		Vector3D zero = new Vector3D(0,0,0);
		assertEquals(zero, u.cross(u));
		assertEquals(new Vector3D(-2,-11,10),u.cross(v));
		assertEquals(new Vector3D(-25.5,18,22),u.cross(w));
		assertEquals(new Vector3D(2,11,-10),v.cross(u));
		assertEquals(zero, v.cross(v));
		assertEquals(new Vector3D(-7.75,-16.25,-14), v.cross(w));
		assertEquals(new Vector3D(25.5,-18,-22), w.cross(u));
		assertEquals(new Vector3D(7.75,16.25,14), w.cross(v));
		assertEquals(zero, w.cross(w));
		
	}
}
