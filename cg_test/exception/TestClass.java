public class TestClass {

	static class FirstException extends Exception {
	}

	static class SecondException extends Exception {
	}

	public static void rethrowException(int type) throws Exception {
		try {
			switch(type) {
			case 1:
				throw new FirstException();
			case 0:
				throw new SecondException();
			default:
				assert false;
				return;
			}
		} catch (FirstException e) {
			System.out.println(1);
		} catch (SecondException e) {
			System.out.println(2);
			throw e;
		}
	}

	private Object someField;
	
	private static void doInternals() {
		try {
			int[] x = new int[0];
			x[1] = 0;
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		try {
			TestClass x = null;
			x.someField.toString();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] arg) {
		try {
			rethrowException(1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			rethrowException(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		doInternals();
	}
}