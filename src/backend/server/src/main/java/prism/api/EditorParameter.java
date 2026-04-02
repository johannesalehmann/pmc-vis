package prism.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EditorParameter {
    @JsonAlias({"name"})
    private String label;
    private String value;

    public EditorParameter() {
        // Jackson deserialization
    }

    public EditorParameter(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @JsonProperty
    public String getLabel() {
        return label;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }
}
