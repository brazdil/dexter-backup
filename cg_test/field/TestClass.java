public class TestClass {

	private Object objField;
	private int intField;
	private double doubleField;
	
	private static Object staticObjField;
	private static int staticIntField;
	private static double staticDoubleField;
	
	public static void main(String[] arg) {
		try {
			TestClass a = new TestClass();
			TestClass b = new TestClass();
			a.objField = staticObjField;
			a.intField = staticIntField;
			b.doubleField = staticDoubleField;
			
			b.objField = a.objField;
			b.intField = a.intField;
			b.doubleField = a.doubleField;
			
			staticObjField = b.objField;
			staticIntField = b.intField;
			staticDoubleField = b.doubleField;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}