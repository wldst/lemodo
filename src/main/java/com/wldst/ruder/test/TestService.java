package com.wldst.ruder.test;

public class TestService {
    private String name;
    private String id;
    public String getId() {
        return id;
    }    public void setId(String id) {
        this.id = id;
    }    public String getName() {
        return name;
    }    public void setName(String name) {
        this.name = name;
    }    public void print() {
        System.out.println("获取bean,name=" + name + ",id=" + id);
    }    @Override
    public String toString() {
        return "TestService{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
