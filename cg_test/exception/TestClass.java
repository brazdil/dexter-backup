public class TestClass {

	static class FirstException extends Exception {
	}

	static class SecondException extends Exception {
	}

	public static void rethrowException(boolean first) throws Exception {
		try {
			if (first) {
				throw new FirstException();
			} else {
				throw new SecondException();
			}
		} catch (FirstException e) {
			System.out.println(1);
		} catch (SecondException e) {
			System.out.println(2);
			throw e;
		}
	}

	public static void main(String[] arg) {
		try {
			rethrowException(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			rethrowException(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}