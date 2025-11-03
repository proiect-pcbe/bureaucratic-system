package org.example;

import org.example.config.ConfigLoader;
import org.example.graph.DependencyGraph;
import org.example.model.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n=== Bureaucratic System Coordinator Starting ===\n");

        try {
            Config config = ConfigLoader.loadConfig("config.yaml");

            DependencyGraph dependencyGraph = new DependencyGraph();
            for (Map.Entry<String, Config.DocumentConfig> entry : config.getDocuments().entrySet()) {
                String docName = entry.getKey();
                List<String> requires = entry.getValue().getRequires();
                DocumentType docType = new DocumentType(docName, requires);
                dependencyGraph.addDocument(docType);
            }

            Map<String, Office> offices = new HashMap<>();
            System.out.println("*** Creating Offices ***\n");
            for (Map.Entry<String, Config.OfficeConfig> entry : config.getOffices().entrySet()) {
                String officeName = entry.getKey();
                Config.OfficeConfig officeConfig = entry.getValue();

                Office office = new Office(officeName, officeConfig.getIssues(), officeConfig.getCounters());
                offices.put(officeName, office);
                System.out.println("  " + office);

                office.startCounters();
            }

            System.out.println("\n*** Simulating Clients ***\n");

            List<Thread> clientThreads = new ArrayList<>();

            List<String> availableDocuments = new ArrayList<>(config.getDocuments().keySet());
            List<String> validDocs = new ArrayList<>();
            Map<String, List<String>> docPaths = new HashMap<>();
            for (String doc : availableDocuments) {
                List<String> path = dependencyGraph.getDocumentPath(doc);
                if (!path.isEmpty()) {
                    validDocs.add(doc);
                    docPaths.put(doc, path);
                }
            }
            if (validDocs.isEmpty()) {
                throw new IllegalStateException("No documents have a valid dependency path. Check your config/graph.");
            }

            Random rand = new Random();
            int CLIENT_COUNT = 50;
            for (int i = 0; i < CLIENT_COUNT; i++) {
                String desiredDoc = validDocs.get(rand.nextInt(validDocs.size()));
                List<String> path = new ArrayList<>(docPaths.get(desiredDoc));

                Client client = new Client(desiredDoc, path, offices);
                Thread clientThread = new Thread(client, "Client " + (i + 1) + " -" + desiredDoc);
                clientThreads.add(clientThread);
                clientThread.start();

                Thread.sleep(100);
            }

            for (Thread clientThread : clientThreads) {
                clientThread.join();
            }

            System.out.println("\n[SUCCESS] All clients have been served!");
            System.out.println("Shutting down offices...\n");

            for (Office office : offices.values()) {
                office.shutdownCounters();
            }

            Thread.sleep(5000);
            System.out.println("\n=== Bureaucratic System Coordinator finished ===");

        } catch (Exception e) {
            System.err.println("[ERROR] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

