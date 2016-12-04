package ru.ssau.mobile.lab2.models;

import java.util.List;

/**
 * Created by Pavel Chursin on 20.11.2016.
 */
public class Meeting {
    String subject;
    String summary;
    long startTime;
    long endTime;
    List<String> members;
    int priority;

    public int getPriority() {
        return priority;
    }

    public List<String> getMembers() {
        return members;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getSummary() {
        return summary;
    }

    public String getSubject() {
        return subject;
    }

    public Meeting(){};

    public Meeting(String subject, String summary, long startTime, long endTime, List<String> members, int priority) {
        this.subject = subject;
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
        this.members = members;
        this.priority = priority;
    }
}
