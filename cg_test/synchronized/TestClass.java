public class TestClass implements Runnable{

	private int counter;
	private synchronized void increment() {
		counter++;
	}
	
	public void run () {
		for(int i=0; i<1000; i++)
			increment();
	}
	
	public static void main(String[] arg) {
		TestClass cls = new TestClass();
		for(int i=0; i<2; i++) {
			new Thread(cls).start();
		}
		
		assert cls.counter == 2000;
	}
}