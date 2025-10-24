package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class DocumentType {
    private final String name;
    private final List<String> requiredDocuments;

    public DocumentType(String name, List<String> requiredDocuments) {
        this.name = name;
        this.requiredDocuments = requiredDocuments != null ? requiredDocuments : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getRequiredDocuments() {
        return requiredDocuments;
    }

    @Override
    public String toString() {
        return name;
    }
}

