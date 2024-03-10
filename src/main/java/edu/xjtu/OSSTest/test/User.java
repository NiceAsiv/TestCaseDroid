package edu.xjtu.OSSTest.test;

public class User {
    public int age;
    public String name;

    public User(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        System.out.println(age);
        return age;

    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        System.out.println(name);
        return name;

    }

    public void setName(String name) {
        this.name = name;
    }
}
