package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Office {
    private final String name;
    private final List<String> documentTypes;
    private final List<Counter> counters;
    private final AtomicInteger roundRobinIndex;

    public Office(String name, List<String> documentTypes, int numberOfCounters) {
        this.name = name;
        this.documentTypes = documentTypes;
        this.counters = new ArrayList<>();
        this.roundRobinIndex = new AtomicInteger(0);

        // Create counters
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
        // Find the counter with the shortest queue that is open
        Counter bestCounter = null;
        int minQueueSize = Integer.MAX_VALUE;

        for (Counter counter : counters) {
            if (counter.getStatus() == CounterStatus.OPEN && counter.getQueueSize() < minQueueSize) {
                bestCounter = counter;
                minQueueSize = counter.getQueueSize();
            }
        }

        // If all counters are on break, wait for any counter
        if (bestCounter == null) {
            bestCounter = counters.get(roundRobinIndex.getAndIncrement() % counters.size());
        }

        bestCounter.addClient(client);
        return bestCounter;
    }

    @Override
    public String toString() {
        return name + " (issues: " + documentTypes + ")";
    }
}

