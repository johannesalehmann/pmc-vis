package prism.core.Computation;

import prism.api.Transition;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MockTask2 extends DataProviderTask {

    public MockTask2(String name, int id, Model model, String property, String propertyExpression, Map<String, String> arguments) {
        super(name, id, model, property, propertyExpression, arguments, false, true);
    }

    @Override
    public void callTool() {

        Map<String, String> results = new HashMap<>();
        Map<String, String> hoverEntries = new HashMap<>();
        Random random = new Random(4356436);
        for(String s : model.getAllStateIDs()){
            double d = random.nextDouble();
            results.put(s, Double.toString(d));
        }
        for(String s : model.getAllStateIDs()){
            String hover = String.valueOf(random.nextDouble());
            hoverEntries.put(s, hover);
        }
        this.writeToDatabase(model.getTableStates(), this.getColumnName(), Namespace.ENTRY_S_ID, results);
        this.writeToDatabase(model.getTableStates(), this.getHoverCollumn(), Namespace.ENTRY_S_ID, hoverEntries);
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
        return "Mock2";
    }
}

