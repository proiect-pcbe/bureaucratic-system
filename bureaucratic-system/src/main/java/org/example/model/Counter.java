package org.example.model;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter implements Runnable {
    private static final AtomicInteger counterIdGenerator = new AtomicInteger(1);

    private final int id;
    private final Office office;
    private volatile CounterStatus status = CounterStatus.OPEN;
    private final Random random = new Random();
    private volatile boolean running = true;

    public Counter(Office office) {
        this.id = counterIdGenerator.getAndIncrement();
        this.office = office;
    }

    public int getId() {
        return id;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Counter " + id + " at " + office.getName() + " started.");
        try {
            while (running) {
                if (status == CounterStatus.OPEN && random.nextInt(100) < 25) {
                    status = CounterStatus.COFFEE_BREAK;
                    System.out.println("[BREAK] Counter " + id + " at " + office.getName() + " taking coffee break!");
                    try {
                        Thread.sleep(random.nextInt(2000) + 1000);
                    } catch (InterruptedException ie) {
                        if (!running) break;
                        Thread.currentThread().interrupt();
                    } finally {
                        status = CounterStatus.OPEN;
                        System.out.println("[BACK] Counter " + id + " at " + office.getName() + " back from coffee break.");
                    }
                }

                if (!running) break;

                Client client;
                try {
                    client = office.takeNextClient();
                } catch (InterruptedException ie) {
                    if (!running) break;
                    Thread.currentThread().interrupt();
                    continue;
                }

                if (client != null && running && status == CounterStatus.OPEN) {
                    processClient(client);
                }
            }
        } finally {
            System.out.println("Counter " + id + " at " + office.getName() + " closed.");
        }
    }

    private void processClient(Client client) {
        try {
            String documentName = client.getCurrentDocumentNeeded();
            System.out.println("[PROCESSING] Counter " + id + " at " + office.getName() +
                    " processing " + client.getName() + " for document: " + documentName);

            Thread.sleep(random.nextInt(1000) + 500);

            client.receiveDocument(documentName);
            System.out.println("[ISSUED] Counter " + id + " at " + office.getName() +
                    " issued " + documentName + " to " + client.getName());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
