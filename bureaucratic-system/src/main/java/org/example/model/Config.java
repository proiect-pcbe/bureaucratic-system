package org.example.model;

import java.util.List;
import java.util.Map;

public class Config {
    private Map<String, OfficeConfig> offices;
    private Map<String, DocumentConfig> documents;

    public Map<String, OfficeConfig> getOffices() {
        return offices;
    }

    public void setOffices(Map<String, OfficeConfig> offices) {
        this.offices = offices;
    }

    public Map<String, DocumentConfig> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, DocumentConfig> documents) {
        this.documents = documents;
    }

    public static class OfficeConfig {
        private List<String> issues;
        private int counters;

        public List<String> getIssues() {
            return issues;
        }

        public void setIssues(List<String> issues) {
            this.issues = issues;
        }

        public int getCounters() {
            return counters;
        }

        public void setCounters(int counters) {
            this.counters = counters;
        }
    }

    public static class DocumentConfig {
        private List<String> requires;

        public List<String> getRequires() {
            return requires;
        }

        public void setRequires(List<String> requires) {
            this.requires = requires;
        }
    }
}

