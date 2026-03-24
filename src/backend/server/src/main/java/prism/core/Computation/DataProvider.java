package prism.core.Computation;

;
import prism.core.Model;
import prism.core.Property.Property;

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
            case "mock":
                dataProvider = new DataProviderGeneric<>("Mock Values", parent, MockTask.class.getConstructor(String.class, Model.class, Property.class));
                break;
            case "mock2":
                dataProvider = new DataProviderGeneric<>("Mockier Values", parent, MockTask2.class.getConstructor(String.class, Model.class, Property.class));
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

    //Used to start computation of values the provider provides
    void compute(Property property, Map<String, Object> arguments);

    Map<String, String[]> getColumnMap();

    String getName();

    boolean isReady();
}
