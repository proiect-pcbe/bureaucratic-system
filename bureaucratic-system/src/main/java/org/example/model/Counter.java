package org.example.model;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter implements Runnable {
    private static final AtomicInteger counterIdGenerator = new AtomicInteger(1);

    private final int id;
    private final Office office;
    private final BlockingQueue<Client> queue;
    private volatile CounterStatus status;
    private final Random random;
    private volatile boolean running;

    public Counter(Office office) {
        this.id = counterIdGenerator.getAndIncrement();
        this.office = office;
        this.queue = new LinkedBlockingQueue<>();
        this.status = CounterStatus.OPEN;
        this.random = new Random();
        this.running = true;
    }

    public int getId() {
        return id;
    }

    public CounterStatus getStatus() {
        return status;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public void addClient(Client client) throws InterruptedException {
        queue.put(client);
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("Counter " + id + " at " + office.getName() + " started.");

        while (running) {
            try {
                // Random coffee break
                if (status == CounterStatus.OPEN && random.nextInt(100) < 2) {
                    status = CounterStatus.COFFEE_BREAK;
                    System.out.println("[BREAK] Counter " + id + " at " + office.getName() + " taking coffee break!");
                    Thread.sleep(random.nextInt(2000) + 1000);
                    status = CounterStatus.OPEN;
                    System.out.println("[BACK] Counter " + id + " at " + office.getName() + " back from coffee break.");
                }

                if (status == CounterStatus.OPEN) {
                    Client client = queue.poll();
                    if (client != null) {
                        processClient(client);
                    } else {
                        Thread.sleep(100);
                    }
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
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

        // Simulate processing time
        Thread.sleep(random.nextInt(1000) + 500);

        // Issue document
        client.receiveDocument(documentName);
        System.out.println("[ISSUED] Counter " + id + " at " + office.getName() +
                         " issued " + documentName + " to " + client.getName());
    }
}

