package tests;

interface I {
	I f();
}

class A implements I {
	public I f() { return new B(); }
}

class B implements I {
	public I f() { return new A(); }
}


public class MainCFA {
	public static void main(String[] args) {
		I i = new A();
		//if (i instanceof I)
		i = i.f();
		I x = i.f();
	}

}
