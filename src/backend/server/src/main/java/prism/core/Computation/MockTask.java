package prism.core.Computation;

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
        super(name, model, property);
    }

    @Override
    public void callTool() {

        Map<String, String> results = new HashMap<>();
        Random random = new Random(125324263);
        for(String s : model.getAllStateIDs()){
            double d = random.nextDouble();
            results.put(s, Double.toString(d));
        }
        this.writeToDatabase(model.getTableStates(), this.getColumnName(), Namespace.ENTRY_S_ID, results);
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

