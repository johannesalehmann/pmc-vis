package prism.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Info {

    private String id;
    private TreeMap<String, DataCategory> stateInformation;
    private TreeMap<String, DataCategory> transitionInformation;
    private List<String> computable;

    public Info(String id){
        this.id = id;
        this.stateInformation = new TreeMap<>();
        this.transitionInformation = new TreeMap<>();
        this.computable = new ArrayList<>();
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public TreeMap<String, Map<String, DataEntry>> getS() {
        TreeMap<String, Map<String, DataEntry>> map = new TreeMap<>();
        for (Map.Entry<String, DataCategory> entry : stateInformation.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getEntries());
        }
        return map;
    }

    @JsonProperty
    public TreeMap<String, Map<String, DataEntry>> getT() {
        TreeMap<String, Map<String, DataEntry>> map = new TreeMap<>();
        for (Map.Entry<String, DataCategory> entry : transitionInformation.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getEntries());
        }
        return map;
    }

    @JsonProperty
    public TreeMap<String, Map<String, DataEntry>> getScheduler() {
        TreeMap<String, Map<String, DataEntry>> map = new TreeMap<>();
        return map;
    }

    @JsonProperty
    public List<String> getComputable() {
        return computable;
    }

    public void setStateEntry(String category, DataEntry entry) {
        if (!stateInformation.containsKey(category)) {
            stateInformation.put(category, new DataCategory(category));
        }
        stateInformation.get(category).addEntry(entry);
    }

    public void setTransitionEntry(String category, DataEntry entry) {
        if (!transitionInformation.containsKey(category)) {
            transitionInformation.put(category, new DataCategory(category));
        }
        transitionInformation.get(category).addEntry(entry);
    }

    public DataEntry getStateEntry(String category, String key) {
        return this.stateInformation.get(category).getEntry(key);
    }

    public DataEntry getTransitionEntry(String category, String key) {
        return this.transitionInformation.get(category).getEntry(key);
    }

    public void removeStateEntries(String category) {
        if (stateInformation.containsKey(category)) {
            this.stateInformation.remove(category);
        }

    }

    public void removeTransitionEntries(String category) {
        if (transitionInformation.containsKey(category)) {
            this.transitionInformation.remove(category);
        }
    }

    public void setComputable(String category) {
        this.computable.add(category);
    }

    public Info copy() {
        Info newInfo = new Info(id);
        newInfo.stateInformation = new TreeMap<>(stateInformation);
        newInfo.transitionInformation = new TreeMap<>(transitionInformation);
        return newInfo;
    }
}
