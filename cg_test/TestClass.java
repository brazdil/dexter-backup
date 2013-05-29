public class TestClass {

//    public static int testMethod() {
//        return 2;
//    }

	public int hashCode() {
		switch (super.hashCode()) {
		case 1:
			return 2;
		case 2:
			return 3;
		default:
			return (int)((long)super.hashCode() * 3 + 1L);
		}
	}
}
