package org.example.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {
    private static final AtomicInteger clientIdGenerator = new AtomicInteger(1);

    private final int id;
    private final String name;
    private final String desiredDocument;
    private final List<String> documentPath;
    private final Set<String> obtainedDocuments;
    private final Map<String, Office> officeMap;
    private volatile String currentDocumentNeeded;
    private int currentStep;

    public Client(String desiredDocument, List<String> documentPath, Map<String, Office> officeMap) {
        this.id = clientIdGenerator.getAndIncrement();
        this.name = "Client " + id;
        this.desiredDocument = desiredDocument;
        this.documentPath = documentPath;
        this.obtainedDocuments = ConcurrentHashMap.newKeySet();
        this.officeMap = officeMap;
        this.currentStep = 0;
    }

    public String getName() {
        return name;
    }

    public String getCurrentDocumentNeeded() {
        return currentDocumentNeeded;
    }

    public synchronized void receiveDocument(String documentName) {
        obtainedDocuments.add(documentName);
        notifyAll();
    }

    @Override
    public void run() {
        System.out.println("[CLIENT] " + name + " wants to obtain: " + desiredDocument);
        System.out.println("         Path required: " + documentPath);

        try {
            for (String docName : documentPath) {
                currentDocumentNeeded = docName;

                Office targetOffice = findOfficeForDocument(docName);
                if (targetOffice == null) {
                    System.out.println("[ERROR] " + name + " ERROR: No office issues " + docName);
                    return;
                }

                System.out.println("[GOING] " + name + " going to " + targetOffice.getName() + " for " + docName);

                targetOffice.assignClient(this);

                synchronized (this) {
                    while (!obtainedDocuments.contains(docName)) {
                        wait();
                    }
                }

                currentStep++;
            }

            System.out.println("[COMPLETE] " + name + " successfully obtained " + desiredDocument + "!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[ERROR] " + name + " was interrupted.");
        }
    }

    private Office findOfficeForDocument(String documentName) {
        for (Office office : officeMap.values()) {
            if (office.canIssue(documentName)) {
                return office;
            }
        }
        return null;
    }
}