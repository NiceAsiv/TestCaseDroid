package tests;

public class HelloThread {
	static int x=1;
	public static void main(String[] args)	{
			TestThread t = new TestThread();
			t.start();
			//race here, may throw divide by zero exception
			int z = t.y+1/x;
			System.out.println(z);
	}
	static class TestThread extends Thread{

		int y;

		public void run(){
				x=0;
				y++;
		}
	}
}