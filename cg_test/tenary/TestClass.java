public class TestClass {
    public static void main(String[] args) {
		byte dbl2 = 1;
        System.out.println(Long.toHexString(dbl2).equals("0") ? "AA" : Long.toHexString(dbl2));
		}
}