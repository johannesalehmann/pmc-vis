package prism.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import prism.core.Namespace;

public class AP extends DataEntry {

    private String identifier;

    private boolean icon;

    public AP(String name, String identifier, boolean icon){
        super(name, "AP", Type.TYPE_BOOL, 0.0, 1.0);
        this.identifier = identifier;
        this.icon = icon;
    }

    @Schema(description = "Identifier used (either URL if icon true, otherwise Name)")
    @JsonProperty
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty
    public String getType() {
        return Namespace.TYPE_BOOLEAN;
    }

    @Schema(description = "Does this AP have an icon?")
    @JsonProperty
    public boolean isIcon() {
        return icon;
    }


}
