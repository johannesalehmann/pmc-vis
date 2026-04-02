package prism.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EditorHighlighting {
    @JsonAlias({"start", "from"})
    private int startPosition;
    @JsonAlias({"end", "to"})
    private int endPosition;

    @JsonAlias({"color", "colourHex", "colour"})
    private String colorHex;

    @JsonAlias({"tooltip", "hover", "tip"})
    private String hoverInfo;

    public EditorHighlighting(){
        // Jackson deserialization
    }

    public EditorHighlighting(int startPosition, int endPosition, String colorHex, String hoverInfo) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.colorHex = colorHex;
        this.hoverInfo = hoverInfo;
    }

    @JsonProperty
    public int getStartPosition() {
        return startPosition;
    }

    @JsonProperty
    public int getEndPosition() {
        return endPosition;
    }

    @JsonProperty
    public String getColorHex() {
        return colorHex;
    }

    @JsonProperty
    public String getHoverInfo() {
        return hoverInfo;
    }
}
