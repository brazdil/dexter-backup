class BaseClass {
	private int value;
	
	public void setValue(int v) {
		value = v;
	}
	
	public int getValue() {
		return value;
	}
}

class SubClass extends BaseClass{
	public int getValue() {
		switch (super.getValue()) {
		case 1:
			return 2;
		case 2:
			return 3;
		default:
			return (int)((long)super.getValue() * 3 + 1L);
		}
	}
}


public class TestClass {
	public static void main(String[] arg) {
		SubClass c = new SubClass();
		c.setValue(7);
		System.out.println(c.getValue());
	}
}