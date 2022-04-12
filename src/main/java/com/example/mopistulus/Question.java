package com.example.mopistulus;

public class Question {
    public String name, country, flag;

    public Question() {}

    public Question(String name, String country, String flag){
        this.name = name;
        this.country = country;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Question{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}
