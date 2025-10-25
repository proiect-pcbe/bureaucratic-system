package org.example.model;

public class Counter {
    private String name;
    private Office office;

    public Counter(String name) {
        this.name = name;
    }
    public String getName() { return name; }
    public void setOffice(Office office) { this.office = office; }
}
