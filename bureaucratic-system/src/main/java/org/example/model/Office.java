package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Office {
    private final String name;
    private final List<String> documentTypes;
    private final List<Counter> counters = new ArrayList<>();
    private final BlockingQueue<Client> globalWaitingQueue;

    public Office(String name, List<String> documentTypes, int numberOfCounters) {
        this.name = Objects.requireNonNull(name, "name");
        this.documentTypes = List.copyOf(documentTypes);
        this.globalWaitingQueue = new LinkedBlockingQueue<>();

        for (int i = 0; i < numberOfCounters; i++) {
            counters.add(new Counter(i + 1, this));
        }
    }

    public String getName() { return name; }

    public List<String> getDocumentTypes() { return documentTypes; }

    public List<Counter> getCounters() { return counters; }

    public boolean canIssue(String documentName) {
        return documentTypes.contains(documentName);
    }

    public void assignClient(Client client) {
        try {
            globalWaitingQueue.put(client);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    Client getNextWaitingClientBlocking() throws InterruptedException {
        return globalWaitingQueue.take();
    }

    Client getNextWaitingClientTimed(long timeout, TimeUnit unit) throws InterruptedException {
        return globalWaitingQueue.poll(timeout, unit);
    }

    @Override
    public String toString() {
        return name + " (issues: " + documentTypes + ")";
    }


    private final List<Thread> workerThreads = new ArrayList<>();

    public void startCounters() {
        if (!workerThreads.isEmpty()) return;
        for (Counter c : counters) {
            Thread t = new Thread(c, "Counter-" + c.getId() + "@" + name);
            t.start();
            workerThreads.add(t);
        }
    }

    public void shutdownCounters() {
        for (Counter c : counters) {
            c.shutdown();
        }
        for (Thread t : workerThreads) {
            t.interrupt();
        }
        for (Thread t : workerThreads) {
            try {
                t.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        workerThreads.clear();
    }
}
