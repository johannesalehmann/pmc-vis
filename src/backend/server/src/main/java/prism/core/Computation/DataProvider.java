package prism.core.Computation;

import prism.api.EditorOption;
import prism.core.Model;
import prism.core.Property.Property;
import prism.server.DataProviderConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DataProvider {

    //Used to call the constructor of your provider
    static DataProvider initialize(DataProviderConfiguration config, Model parent){

        String type = config.getType();

        Map<String, String> properties = new HashMap<>();
        properties.putAll(parent.getInertProperties());

        for (Property p : parent.getProperties()){
            properties.put(p.getName(), p.getExpression().toString());
        }

        DataProvider dataProvider = null;
        try{
        switch (type){
            case "responsibility":
                dataProvider = new DataProviderGeneric<>(config, parent, properties, ResponsibilityTask.class.getConstructor(String.class, int.class, Model.class, String.class, String.class, Map.class));
                break;
            //case "causality":
            //    dataProvider = new DataProviderCausality("Causality", parent);
            //    break;
            case "mock":
                dataProvider = new DataProviderGeneric<>(config, parent, properties, MockTask.class.getConstructor(String.class, int.class, Model.class, String.class, String.class, Map.class));
                break;
            case "mock2":
                dataProvider = new DataProviderGeneric<>(config, parent, properties, MockTask2.class.getConstructor(String.class, int.class, Model.class, String.class, String.class, Map.class));
                break;
            case "witness":
                dataProvider = new DataProviderGeneric<>(config, parent, properties, WitnessTask.class.getConstructor(String.class, int.class, Model.class, String.class, String.class, Map.class));
                break;
            case "csv":
                dataProvider = new DataProviderCSV(config, parent);
                break;
        }
        }catch(NoSuchMethodException e){
            System.out.println("Could not find Constructor for: " + type);
            return null;
        }
        if(dataProvider == null){
            System.out.println("Unsupported data provider type: " + type);
            return null;
        }
        return dataProvider;
    }

    void addProperty(String name, String expression);

    boolean contains(String property);
    //Used to start computation of values the provider provides
    void compute(String property, Map<String, String> arguments);

    Map<String, String[]> getColumnMap();

    Map<String, DataProviderTask> getProviderTasks();

    Map<String, List<EditorOption>> getEditorOptions();

    String getName();

    boolean isReady();

    boolean isBool();
}
