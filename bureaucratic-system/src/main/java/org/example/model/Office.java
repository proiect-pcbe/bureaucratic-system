package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Office {
    private final String name;
    private final List<String> documentTypes;
    private final List<Counter> counters;
    private final BlockingQueue<Client> globalWaitingQueue;
    private final List<Thread> counterThreads = new ArrayList<>();

    public Office(String name, List<String> documentTypes, int numberOfCounters) {
        this.name = name;
        this.documentTypes = List.copyOf(documentTypes);
        this.counters = new ArrayList<>();
        this.globalWaitingQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < numberOfCounters; i++) {
            counters.add(new Counter(this));
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getDocumentTypes() {
        return documentTypes;
    }

    public List<Counter> getCounters() {
        return Collections.unmodifiableList(counters);
    }

    public boolean canIssue(String documentType) {
        return documentTypes.contains(documentType);
    }

    public void startCounters() {
        for (Counter counter : counters) {
            Thread t = new Thread(counter, "Counter-" + name + "-" + counter.getId());
            counterThreads.add(t);
            t.start();
        }
    }

    public void shutdownCounters() {
        for (Counter counter : counters) {
            counter.shutdown();
        }
        for (Thread t : counterThreads) {
            t.interrupt();
        }
        for (Thread t : counterThreads) {
            try {
                t.join(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void assignClient(Client client) throws InterruptedException {
        globalWaitingQueue.put(client);
    }

    Client takeNextClient() throws InterruptedException {
        return globalWaitingQueue.take();
    }

    @Override
    public String toString() {
        return name + " (issues: " + documentTypes + ")";
    }
}
