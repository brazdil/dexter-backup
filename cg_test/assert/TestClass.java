public class TestClass {

	private static int change(int v) {
		switch (v) {
		case 1:
			return 2;
		case 2:
			return 3;
		default:
			return 3*v + 4;
		}
	}
	public static void main(String[] arg) {
		assert change(1) == 2;
		assert change(2) == 3;
		assert change(3) == 3*3 + 4;
		System.out.println(true);
	}
}