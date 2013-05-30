public class TestClass {

	private static long change(long x, long y) {
		return (2L * x  + 3L * y) / ((int)y - (int)x);
	}
	public static void main(String[] arg) {
		System.out.println(change(1, 2));
		System.out.println(8);
	}
}