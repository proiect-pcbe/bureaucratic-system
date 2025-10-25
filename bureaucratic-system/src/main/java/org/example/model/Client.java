package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private String name;
    private String document;
    private List<Office> officeRoute;

    public Client(String name, String document) {
        this.name = name;
        this.document = document;
        this.officeRoute = new ArrayList<>();

    }

    public String getName() { return name; }
    public String getDocument() { return document; }
    public List<Office> getOfficeRoute() { return officeRoute; }
    public void setOfficeRoute(List<Office> route) {
        this.officeRoute = (route == null) ? new ArrayList<>() : route;
    }
}