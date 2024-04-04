package TestCaseDroid.test;

/**
 * 这个类是用来测试ICFG的生成效果的
 */
public class ICFG {
    public static void main(String[] args) {
        User user = new User(20, "Tom");
        user.getAge();
        user.getName();
        user.setAge(21);
        user.setName("Jerry");
        user.getAge();
        user.getName();
    }

}
