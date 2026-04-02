package prism.core.Computation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import prism.api.EditorHighlighting;
import prism.api.EditorOption;
import prism.api.EditorParameter;
import prism.api.Transition;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockTask extends DataProviderTask {

    public MockTask(String name, Model model, Property property) {
        super(name, model, property, true, false);
    }

    @Override
    public void callTool() {

        Map<String, String> results = new HashMap<>();
        Map<String, String> highlights = new HashMap<>();
        Map<String, String> highlights2 = new HashMap<>();
        Random random = new Random(125324263);
        for(String s : model.getAllStateIDs()){
            double d = random.nextDouble();
            results.put(s, Double.toString(d));
            highlights2.put(s, d>0.1?"1":"0");
        }
        for(Transition t : model.getAllTransitions()){
            boolean b = random.nextBoolean();
            highlights.put(t.getNumId(), b?"1":"0");
        }
        this.writeToDatabase(model.getTableStates(), this.getColumnName(), Namespace.ENTRY_S_ID, results);
        this.writeToDatabase(model.getTableStates(), this.getHighlightCollumn(), Namespace.ENTRY_S_ID, highlights2);
        this.writeToDatabase(model.getTableTrans(), this.getHighlightCollumn(), Namespace.ENTRY_T_ID, highlights);
    }

    @Override
    public double getMin(){
        return 0.0;
    }

    @Override
    public double getMax(){
        return 1.0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public String shortName(){
        return "Mock";
    }

    @Override
    public List<EditorOption> getEditorOptions() {
        List<EditorOption> options = new ArrayList<>();
        options.add(new EditorOption("One", "-1"));
        List<EditorParameter> extra = new ArrayList<>();
        extra.add(new EditorParameter("one", "1"));
        extra.add(new EditorParameter("two", "2"));
        Map<String, List<EditorParameter>> parameters = new HashMap<>();
        parameters.put("extra", extra);
        options.add(new EditorOption("Two", "-2 $extra", parameters));
        return options;
    }

    @Override
    public List<EditorHighlighting> getEditorHighlighting(List<String> arguments){
        String exampleHighlighting = "[\n" +
                "    {\n" +
                "        \"from\": 0,\n" +
                "        \"to\": 3,\n" +
                "        \"tooltip\": \"Example tooltip\",\n" +
                "        \"colour\": \"#e25858\"\n" +
                "    }" +
                "]";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<EditorHighlighting> highlightings = objectMapper.readValue(exampleHighlighting, new TypeReference<List<EditorHighlighting>>(){});
            return highlightings;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

