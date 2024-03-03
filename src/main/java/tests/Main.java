package tests;

public class Main {
	
	private static int sqr(int x) {
		return x * x;
	}
	
	public static void main(String[] args) {
		int sum = 0;
		for (int i = 0; i < 100; i++)
			sum += sqr(i);
		System.out.println(sum);
	}

}
