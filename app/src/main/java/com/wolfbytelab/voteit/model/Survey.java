package com.wolfbytelab.voteit.model;

import java.util.ArrayList;

public class Survey {

    public String title;
    public String description;

    public User owner;
    public ArrayList<User> members;

    public long startDate;
    public long endDate = Long.MAX_VALUE;

}
