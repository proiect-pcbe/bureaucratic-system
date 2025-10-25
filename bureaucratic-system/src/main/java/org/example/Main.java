package org.example;

import org.example.config.ConfigLoader;   // folosește clasa ta existentă
import org.example.model.Client;
import org.example.model.Document;
import org.example.model.Office;

import java.io.File;
import java.net.URL;
import java.util.List;

public class Main {

    private static String resourcePath(String name) {
        URL url = Main.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Resource not found: " + name);
        }
        return new File(url.getFile()).getPath();
    }

    public static void main(String[] args) {
        String docsPath    = resourcePath("documents.json");
        String officesPath = resourcePath("offices.json");
        String clientsPath = resourcePath("clients.json");

        List<Document> documents = ConfigLoader.getDocuments(docsPath);
        List<Office>   offices   = ConfigLoader.getOffices(officesPath);
        List<Client>   clients   = ConfigLoader.getClients(clientsPath);

        System.out.println("Documents: " + documents.size());
        System.out.println("Offices: " + offices.size());
        System.out.println("Clients: " + clients.size());
        System.out.println();

        for (Client c : clients) {
            StringBuilder route = new StringBuilder();
            for (int i = 0; i < c.getOfficeRoute().size(); i++) {
                if (i > 0) route.append(" -> ");
                route.append(c.getOfficeRoute().get(i).getName());
            }
            System.out.println(c.getName() + " wants doc " + c.getDocument()
                    + " | route: " + route);
        }
    }
}
