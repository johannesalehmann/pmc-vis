package prism.core.Computation;

import prism.core.Model;
import prism.core.Namespace;

import java.util.HashMap;
import java.util.Map;

public class CSVTask extends DataProviderTask {

    private final double minValue;
    private final double maxValue;

    private final Map<String, String> content;

    public CSVTask(String name, int id, Model model, String property, double minValue, double maxValue, Map<String, String> content) {
        super(name, id, model, property, "", new HashMap<>(), false, false);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.content = content;
    }

    @Override
    public String shortName() {
        return "csv";
    }

    @Override
    protected void callTool() {
        //Just writes its saved Values to database
        //Needs to happen here to allow the model to be build beforehand
        this.writeToDatabase(model.getTableStates(), this.getColumnName(), Namespace.ENTRY_S_ID, this.content);
    }

    @Override
    public double getMin() {
        return minValue;
    }

    @Override
    public double getMax() {
        return maxValue;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
