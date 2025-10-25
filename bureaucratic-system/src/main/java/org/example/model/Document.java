package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private String name;
    private List<String> reqs;

    public Document(String name, List<String> reqs) {
        this.name = name;
        this.reqs = (reqs == null) ? new ArrayList<>() : reqs;
    }
    public String getName() { return name; }
    public List<String> getReqs() { return reqs; }
}