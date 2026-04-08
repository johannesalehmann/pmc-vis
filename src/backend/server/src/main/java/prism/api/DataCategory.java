package prism.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import prism.core.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCategory implements Namespace {

    private String name;
    private String shortName;
    private Map<String, DataEntry> entries;

    public DataCategory(String name, List<DataEntry> entries) {
        this.name = name;
        this.entries = new HashMap<>();
        for (DataEntry entry : entries) {
            this.entries.put(entry.getEntryName(), entry);
        }
    }

    public DataCategory(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
        this.entries = new HashMap<>();
    }

    @JsonIgnore
    public void addEntry(DataEntry entry) {
        String name = entry.getEntryName();
        this.entries.put(name, entry);
    }

    @JsonIgnore
    public DataEntry getEntry(String variableName) {
        DataEntry entry = this.entries.get(variableName);
        return entry;
    }

    @JsonIgnore
    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public String getShortName() {
        return this.shortName;
    }

    @JsonIgnore
    public Map<String, DataEntry> getEntries() {
        return this.entries;
    }

    @JsonIgnore
    public DataCategory copy() {
        List<DataEntry> copiedEntries = new ArrayList<>();
        for (DataEntry entry : this.entries.values()) {
            copiedEntries.add(entry.copy());
        }
        return new DataCategory(name, copiedEntries);
    }
}
