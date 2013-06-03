public class TestClass {

	private static long change(long x, long y) {
		int z = ((int)foo((double)x, (double) y)) / ((int)y - (int)x);
		return x + y / z;
		
	}

	private static double foo(double x, double y) {
		return (2.0 * x  + 3.0 * y);
	}
	
	public static void main(String[] arg) {
		System.out.println(change(1, 2));
	}
}