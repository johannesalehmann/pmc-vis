package prism.core.Computation;

import prism.core.Model;
import prism.core.Property.Property;

import java.util.List;
import java.util.Map;

public interface DataProvider {

    //Used to call the constructor of your provider
    static DataProvider initialize(String type, Model parent){
        DataProvider dataProvider = null;
        try{
        switch (type){
            case "responsibility":
                dataProvider = new DataProviderGeneric<>("Responsibility", parent, ResponsibilityTask.class.getConstructor(String.class, Model.class, Property.class));
                break;
            //case "causality":
            //    dataProvider = new DataProviderCausality("Causality", parent);
            //    break;
            case "mock":
                dataProvider = new DataProviderGeneric<>("Mock Values", parent, MockTask.class.getConstructor(String.class, Model.class, Property.class));
                break;
            case "mock2":
                dataProvider = new DataProviderGeneric<>("Mockier Values", parent, MockTask2.class.getConstructor(String.class, Model.class, Property.class));
                break;
            case "witness":
                dataProvider = new DataProviderGeneric<>("Witnessing Subsystems", parent, WitnessTask.class.getConstructor(String.class, Model.class, Property.class));
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

    void addProperty(Property property);

    boolean contains(String property);
    //Used to start computation of values the provider provides
    void compute(String property, Map<String, Object> arguments);

    Map<String, String[]> getColumnMap();

    Map<String, DataProviderTask> getProviderTasks();

    String getName();

    boolean isReady();

    boolean isBool();
}
