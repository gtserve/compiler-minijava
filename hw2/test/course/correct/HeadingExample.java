class HeadingExample {
    public static void main(String[] a) {
        System.out.println(2 + 1);
    }
}

class A{
    int i;
    boolean flag;
    int j;
    public int foo() {
        return 2;
    }
    public boolean fa() {
        return true;
    }
}

class B extends A{
    A type;
    int k;
    public int foo() {
        return 3;
    }
    public boolean bla() {
        return false;
    }
}
