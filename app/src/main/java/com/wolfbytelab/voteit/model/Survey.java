package com.wolfbytelab.voteit.model;

import java.util.ArrayList;

public class Survey {

    private User owner;
    private ArrayList<User> members;

    private long startDate;
    private long endDate = Long.MAX_VALUE;

}
