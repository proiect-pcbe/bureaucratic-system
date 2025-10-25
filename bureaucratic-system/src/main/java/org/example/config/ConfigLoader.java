package org.example.config;
import org.example.model.Client;
import org.example.model.Document;
import org.example.model.Office;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
public class ConfigLoader {

    private static final Map<String, Document> DOC_BY_NAME = new HashMap<>();
    private static final Map<String, Office>   OFFICE_BY_DOC = new HashMap<>();
    public static JSONArray readFromFiles(String fileName, String jsonArrayName) {
        JSONParser parser = new JSONParser();
        JSONObject root = new JSONObject();
        try (Reader reader = new FileReader(fileName)) {
            root = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return (JSONArray) root.get(jsonArrayName);
    }

    public static List<Document> getDocuments(String path) {
        JSONArray arr = readFromFiles(path, "documents");
        List<Document> docs = new ArrayList<>();
        if (arr == null) return docs;

        for (Object o : arr) {
            JSONObject jo = (JSONObject) o;
            String name = String.valueOf(jo.get("name"));
            List<String> reqs = new ArrayList<>();
            Object r = jo.get("reqs");
            if (r instanceof JSONArray) {
                JSONArray jr = (JSONArray) r;
                for (Object x : jr) reqs.add(String.valueOf(x));
            }
            Document d = new Document(name, reqs);
            docs.add(d);
            DOC_BY_NAME.put(name, d);
        }
        return docs;
    }

    public static List<Office> getOffices(String path) {
        JSONArray arr = readFromFiles(path, "offices");
        List<Office> offices = new ArrayList<>();
        if (arr == null) return offices;

        for (Object o : arr) {
            JSONObject jo = (JSONObject) o;
            String name = String.valueOf(jo.get("name"));
            int counters = toInt(jo.get("counters"));
            String document = String.valueOf(jo.get("document"));
            Office office = new Office(name, counters, document);
            offices.add(office);
            OFFICE_BY_DOC.put(document, office);
        }
        return offices;
    }
    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (NumberFormatException e) { return 0; }
    }
    public static List<Client> getClients(String pathClients) {
        JSONArray arr = readFromFiles(pathClients, "clients");
        List<Client> clients = new ArrayList<>();
        if (arr == null) return clients;

        for (Object o : arr) {
            JSONObject jo = (JSONObject) o;
            String name = String.valueOf(jo.get("name"));
            String document = String.valueOf(jo.get("document"));

            List<String> docOrder = computeDocOrderByInsertion(document);

            List<Office> route = new ArrayList<>();
            for (String doc : docOrder) {
                Office office = OFFICE_BY_DOC.get(doc);
                if (office == null) {
                    throw new IllegalStateException("No office releases document '" + doc + "'");
                }
                route.add(office);
            }

            Client c = new Client(name, document);
            c.setOfficeRoute(route);
            clients.add(c);
        }
        return clients;
    }

    private static List<String> computeDocOrderByInsertion(String targetDoc) {
        List<String> docOrder = new ArrayList<>();
        docOrder.add(targetDoc);

        int position = 0;
        while (position < docOrder.size()) {
            String doc = docOrder.get(position);
            List<String> requirements = DOC_BY_NAME.get(doc).getReqs();

            boolean changed = false;
            for (String req : requirements) {
                int reqIndex = docOrder.indexOf(req);

                if (reqIndex == -1) {
                    docOrder.add(position, req);
                    changed = true;
                    break;
                } else if (reqIndex > position) {
                    docOrder.remove(reqIndex);
                    docOrder.add(position, req);
                    changed = true;
                    break;
                }
            }

            if (!changed) {
                position++;
            }
        }
        return docOrder;
    }
}
