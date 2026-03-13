package prism.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import prism.core.Namespace;

@Schema(description="Information Object for Variables and Properties")
public class DataEntry implements Namespace {

    public enum Type {TYPE_BLANK, TYPE_NUMBER, TYPE_BOOL, TYPE_OTHER};

    public enum Status {missing, ready, computing}

    private final String entryName;

    private final String type;

    private final double minValue;

    private final double maxValue;

    private Status status;

    public static DataEntry blank(String name){
        return new DataEntry(name, Type.TYPE_BLANK, 0, 0, Status.missing);
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

    public DataEntry(String name, Type type, double minValue, double maxValue){
        this(name, type, minValue, maxValue, Status.ready);
    }

    public DataEntry(String name, Type type, double minValue, double maxValue, Status status){
        this.entryName = name;
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

    @JsonIgnore
    public String getEntryName(){
        return entryName;
    }

    @JsonIgnore
    public void setStatus(Status status){
        this.status = status;
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

    public DataEntry copy(){
        return new DataEntry(this.entryName, parseType(this.type), this.minValue, this.maxValue, this.status);
    }
}
