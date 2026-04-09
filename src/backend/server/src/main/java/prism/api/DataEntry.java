package prism.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import prism.core.Namespace;

@Schema(description="Information Object for Variables and Properties")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DataEntry implements Namespace {

    public enum Type {TYPE_BLANK, TYPE_NUMBER, TYPE_BOOL, TYPE_OTHER};

    public enum Status {missing, ready, computing, failed}

    private final String entryName;
    private final String prefix;

    private final String type;

    private final double minValue;

    private final double maxValue;

    private Status status;

    private String highlightEntry = "";
    private String hoverEntry = "";
    private String errorMessage = "";

    public static DataEntry blank(String name, String prefix){
        return new DataEntry(name, prefix, Type.TYPE_BLANK, 0, 0, Status.missing);
    }

    public static Type parseType(String typeName){
        switch (typeName){
            case "double":
            case "integer":
            case "int":
            case "number":
                return Type.TYPE_NUMBER;
            case "boolean":
            case "bool":
                return Type.TYPE_BOOL;
            case "string":
            case "complex":
                return Type.TYPE_OTHER;
            default:
                return Type.TYPE_BLANK;
        }
    }

    public DataEntry(String name, String prefix, Type type, double minValue, double maxValue){
        this(name, prefix, type, minValue, maxValue, Status.ready);
    }

    public DataEntry(String name, String prefix, Type type, double minValue, double maxValue, String highlightEntry, String hoverEntry){
        this(name, prefix, type, minValue, maxValue, Status.ready, highlightEntry, hoverEntry);
    }

    public DataEntry(String name, String prefix, Type type, double minValue, double maxValue, Status status){
        this.entryName = name;
        this.prefix = prefix;
        switch(type){
            case TYPE_NUMBER:
                this.type = TYPE_NUMBER;
                break;
            case TYPE_BOOL:
                this.type = TYPE_BOOLEAN;
                break;
            case TYPE_OTHER:
                this.type = TYPE_NOMINAL;
                break;
            case TYPE_BLANK:
            default:
                this.type = TYPE_BLANK;
                break;
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.status = status;
    }

    public DataEntry(String name, String prefix, Type type, double minValue, double maxValue, Status status, String highlightEntry, String hoverEntry){
        this.entryName = name;
        this.prefix = prefix;
        switch(type){
            case TYPE_NUMBER:
                this.type = TYPE_NUMBER;
                break;
            case TYPE_BOOL:
                this.type = TYPE_BOOLEAN;
                break;
            case TYPE_OTHER:
                this.type = TYPE_NOMINAL;
                break;
            case TYPE_BLANK:
            default:
                this.type = TYPE_BLANK;
                break;
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.status = status;
        this.highlightEntry = highlightEntry;
        this.hoverEntry = hoverEntry;
    }

    @JsonIgnore
    public String getEntryName(){
        return String.format("%s:%s", prefix, entryName);
    }

    @JsonIgnore
    public void setStatus(Status status){
        this.status = status;
    }

    @JsonIgnore
    public void setError(String error){
        this.errorMessage = error;
        this.status = Status.failed;
    }

    @JsonIgnore
    public void setHighlightEntry(String highlightEntry){
        this.highlightEntry = highlightEntry;
    }

    @JsonIgnore
    public void setHoverEntry(String hoverEntry){
        this.hoverEntry = hoverEntry;
    }

    @JsonProperty
    public String getPrefix(){
        return prefix;
    }

    @JsonProperty
    public String getType() {
        return type;
    }

    @JsonProperty
    public double getMin() {
        return minValue;
    }

    @JsonProperty
    public double getMax() {
        return maxValue;
    }

    @JsonProperty
    public String status() {
        return status.toString();
    }

    @JsonProperty
    public String highlightEntry() {
        return highlightEntry;
    }

    @JsonProperty
    public String hoverEntry() {
        return hoverEntry;
    }

    @JsonProperty
    public String errorMessage() {
        return errorMessage;
    }

    public DataEntry copy(){
        return new DataEntry(this.entryName, this.prefix, parseType(this.type), this.minValue, this.maxValue, this.status);
    }
}
