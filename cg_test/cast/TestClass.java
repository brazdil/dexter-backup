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
		Object c = new BaseClass();
		((BaseClass)c).setValue(7);
		if (c instanceof SubClass) 
			System.out.println("This should not happen.");
			
		System.out.println(((BaseClass)c).getValue());
	}
}