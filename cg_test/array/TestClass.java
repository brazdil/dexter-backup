public class TestClass {
	private static int eval(int x) {
		return x + 1;
	}
	
	public static void main(String[] arg) {
		long[] other0 = new long[] {4L,3L,1L,2L,5L};
		int[] other1 = new int[] {5,4,3,2,1};
		int[] data = new int[] {eval(1),eval(2),eval(3),eval(4),eval(5)};
		
		for (int i=0; i<data.length; i++)
			data[i] = ((int)(other0[i] + other1[i])) * data[i];
				
		int sum = 0;
		for (int i=0; i<data.length; i++)
			sum += data[i];
		System.out.println(sum);
	}
}