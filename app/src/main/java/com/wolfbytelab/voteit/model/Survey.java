package com.wolfbytelab.voteit.model;

import java.util.ArrayList;

public class Survey {

    public String title;
    public String description;

    public String owner;
    public ArrayList<User> members;

    public long startDate;
    public long endDate = Long.MAX_VALUE;

    public Type type;

    public enum Type {
        OWNER,
        INVITE,
        MEMBER
    }

}
