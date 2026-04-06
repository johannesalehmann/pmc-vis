package prism.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class DataProviderConfiguration extends Configuration {
    @Valid
    @NotNull
    private String name;

    @Valid
    @NotNull
    private String type;

    @Valid
    @NotNull
    private String executable;

    //Timeout in ms
    private long timeout = 60000;

    private Map<String, String> extraArguments = new HashMap<>();

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getType() {
        return type;
    }

    @JsonProperty
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty
    public String getExecutable() {
        return executable;
    }

    @JsonProperty
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @JsonProperty
    public long getTimeout() {
        return timeout;
    }

    @JsonProperty
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @JsonProperty
    public Map<String, String> getExtraArguments() {
        return extraArguments;
    }

    @JsonProperty
    public void setExtraArguments(Map<String, String> extraArguments) {
        this.extraArguments = extraArguments;
    }

}
