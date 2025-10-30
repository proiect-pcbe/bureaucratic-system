package org.example.graph;

import org.example.model.DocumentType;

import java.util.*;

public class DependencyGraph {
    private final Map<String, DocumentType> documents;
    private final Map<String, List<String>> adjacencyList;

    public DependencyGraph() {
        this.documents = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addDocument(DocumentType document) {
        documents.put(document.getName(), document);
        adjacencyList.putIfAbsent(document.getName(), new ArrayList<>());

        for (String requiredDoc : document.getRequiredDocuments()) {
            adjacencyList.putIfAbsent(requiredDoc, new ArrayList<>());
            adjacencyList.get(requiredDoc).add(document.getName());
        }
    }

    public DocumentType getDocument(String name) {
        return documents.get(name);
    }

    public List<String> getDocumentPath(String targetDocument) {
        if (!documents.containsKey(targetDocument)) {
            return Collections.emptyList();
        }

        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        if (hasCycle(targetDocument, visited, recursionStack)) {
            System.out.println("[WARNING] Circular dependency detected for " + targetDocument);
            return Collections.emptyList();
        }

        visited.clear();
        buildPath(targetDocument, visited, path);

        return path;
    }

    private void buildPath(String documentName, Set<String> visited, List<String> path) {
        if (visited.contains(documentName)) {
            return;
        }

        visited.add(documentName);
        DocumentType doc = documents.get(documentName);

        if (doc != null) {
            for (String requiredDoc : doc.getRequiredDocuments()) {
                buildPath(requiredDoc, visited, path);
            }
        }

        path.add(documentName);
    }

    private boolean hasCycle(String documentName, Set<String> visited, Set<String> recursionStack) {
        visited.add(documentName);
        recursionStack.add(documentName);

        DocumentType doc = documents.get(documentName);
        if (doc != null) {
            for (String requiredDoc : doc.getRequiredDocuments()) {
                if (!visited.contains(requiredDoc)) {
                    if (hasCycle(requiredDoc, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(requiredDoc)) {
                    return true;
                }
            }
        }

        recursionStack.remove(documentName);
        return false;
    }

    public void printDependencies() {
        System.out.println("\n*** Document Dependencies ***");
        for (DocumentType doc : documents.values()) {
            if (doc.getRequiredDocuments().isEmpty()) {
                System.out.println("  " + doc.getName() + " (base document)");
            } else {
                System.out.println("  " + doc.getName() + " requires: " + doc.getRequiredDocuments());
            }
        }
    }
}

