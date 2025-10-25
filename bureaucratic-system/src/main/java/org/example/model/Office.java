package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Office {
    private String name;
    private int nrCounters;
    private String document;
    List<Counter> counters;

    public Office(String name, int nrCounters, String document) {
        this.name = name;
        this.nrCounters = nrCounters;
        this.document = document;
        counters = new ArrayList<Counter>(nrCounters);
        for (int i = 1; i <= nrCounters; i++)
            this.counters.add(new Counter("Counter " + i));
    }
    public String getName() { return name; }
    public int getCounters() { return nrCounters; }
    public String getDocument() { return document; }
    public List<Counter> getCountersList() { return counters; }
}