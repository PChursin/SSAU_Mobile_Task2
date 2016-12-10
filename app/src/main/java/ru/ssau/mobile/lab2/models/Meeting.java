package ru.ssau.mobile.lab2.models;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Pavel Chursin on 20.11.2016.
 */
public class Meeting implements Comparable<Meeting> {
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

    public void replace(Meeting other) {
        this.subject = other.subject;
        this.summary = other.summary;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.members = other.members;
        this.priority = other.priority;
    }

    /**
     * Inverted comparison. Because we want newest meetings
     * to be at the top.
     * @param meeting
     * @return
     */
    @Override
    public int compareTo(Meeting meeting) {
        Calendar cur = Calendar.getInstance();
        cur.setTimeInMillis(startTime);
        Calendar other = Calendar.getInstance();
        other.setTimeInMillis(meeting.startTime);
        if (cur.get(Calendar.DAY_OF_YEAR) > other.get(Calendar.DAY_OF_YEAR))
            return -1;
        if (cur.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)) {
            if (startTime < meeting.getStartTime())
                return -1;
            if (startTime == meeting.getStartTime())
                return 0;
        }
        return 1;
    }
}
