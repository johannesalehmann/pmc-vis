package prism.core.Computation;

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
            boolean b = random.nextBoolean();
            results.put(s, Double.toString(d));
            highlights2.put(s, b?"1":"0");
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
}

