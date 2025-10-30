package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Office {
    private final String name;
    private final List<String> documentTypes;
    private final List<Counter> counters;
    private final BlockingQueue<Client> globalWaitingQueue;

    public Office(String name, List<String> documentTypes, int numberOfCounters) {
        this.name = name;
        this.documentTypes = documentTypes;
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
        return counters;
    }

    public boolean canIssue(String documentType) {
        return documentTypes.contains(documentType);
    }

    public void startCounters() {
        for (Counter counter : counters) {
            new Thread(counter).start();
        }
    }

    public void shutdownCounters() {
        for (Counter counter : counters) {
            counter.shutdown();
        }
    }

    public Counter assignClient(Client client) throws InterruptedException {
        globalWaitingQueue.put(client);

        synchronized (this) {
            notifyAll();
        }

        return null;
    }

    public Client getNextWaitingClient() {
        return globalWaitingQueue.poll();
    }

    public void waitForClients() throws InterruptedException {
        synchronized (this) {
            wait();
        }
    }

    public boolean hasWaitingClients() {
        return !globalWaitingQueue.isEmpty();
    }

    @Override
    public String toString() {
        return name + " (issues: " + documentTypes + ")";
    }
}