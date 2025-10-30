package org.example.model;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Counter implements Runnable {

    public enum CounterStatus { OPEN, BREAK }

    private final int id;
    private final Office office;
    private final Random random = new Random();

    private volatile boolean running = true;
    private volatile CounterStatus status = CounterStatus.OPEN;

    public Counter(int id, Office office) {
        this.id = id;
        this.office = Objects.requireNonNull(office, "office");
    }

    public int getId() { return id; }

    public Office getOffice() { return office; }

    public CounterStatus getStatus() { return status; }

    public void setStatus(CounterStatus status) {
        this.status = status;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Counter " + id + " at " + office.getName() + " started.");
        while (running) {
            try {
                if (status == CounterStatus.OPEN) {
                    Client client = office.getNextWaitingClientTimed(200, TimeUnit.MILLISECONDS);
                    if (client != null) {
                        processClient(client);
                    }
                } else {
                    Thread.sleep(200);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Counter " + id + " at " + office.getName() + " closed.");
    }

    private void processClient(Client client) throws InterruptedException {
        String documentName = client.getCurrentDocumentNeeded();
        System.out.println("[PROCESSING] Counter " + id + " at " + office.getName() +
                " processing " + client.getName() + " for document: " + documentName);

        // Simulate processing
        Thread.sleep(random.nextInt(1000) + 500);

        client.receiveDocument(documentName);
        System.out.println("[ISSUED] Counter " + id + " at " + office.getName() +
                " issued " + documentName + " to " + client.getName());
    }
}
