package ru.ssau.mobile.lab2.models;

import java.util.HashMap;

/**
 * Created by Pavel Chursin on 20.11.2016.
 */
public class Member {
    public String name;
    public String position;

    public Member() {};

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public Member(String name, String position) {
        this.name = name;
        this.position = position;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", name, position);
    }

    public static Member fromMap(HashMap<String, String> map){
        String name = map.get("name");
        String position = map.get("position");
        return new Member(name, position);
    }
}
