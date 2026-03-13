package prism.core.Computation;

;
import prism.core.Model;
import prism.core.Property.Property;

import java.util.Map;

public interface DataProvider {

    //Used to call the constructor of your provider
    static DataProvider initialize(String type, Model parent){
        try{
        switch (type){
            case "responsibility":
                return new DataProviderGeneric<>("Responsibility", parent, ResponsibilityTask.class.getConstructor(String.class, Model.class, Property.class));
            case "mock":
                return new DataProviderGeneric<>("Mock Values", parent, MockTask.class.getConstructor(String.class, Model.class, Property.class));
        }
        }catch(NoSuchMethodException e){
            throw new RuntimeException("Could not find Constructor for: " + type, e);
        }
        throw new RuntimeException("Unsupported data provider type: " + type);
    }

    //Used to start computation of values the provider provides
    void compute(Property property, Map<String, Object> arguments);

    Map<String, String> getColumnMap();

    String getName();
}
