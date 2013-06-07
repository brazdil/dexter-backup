public class TestClass implements Runnable{

	private int counter;
	private synchronized void increment() {
		counter++;
	}
	
	public void run () {
		for(int i=0; i<1000; i++)
			increment();
	}
	
	public static void main(String[] arg) throws InterruptedException {
		TestClass cls = new TestClass();
		for(int i=0; i<2; i++) {
			new Thread(cls).start();
		}
		Thread.sleep(500);
		System.out.println(cls.counter);
		assert cls.counter == 2000;
		
		Object x = null;
		try {
			synchronized(x) {
				System.out.println("should not happen");
			}
		} catch (NullPointerException e) {
			assert true;
			return;
		}
		assert false;
	}
}