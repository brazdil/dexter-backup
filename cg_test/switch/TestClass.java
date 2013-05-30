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
		System.out.println(change(1));
		System.out.println(2);
		System.out.println(change(2));
		System.out.println(3);
		System.out.println(change(3));
		System.out.println(3*3+4);
	}
}