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
        }
        }catch(NoSuchMethodException e){
            throw new RuntimeException("Could not find Constructor for: " + type, e);
        }
        if(dataProvider == null){
            throw new RuntimeException("Unsupported data provider type: " + type);
        }
        if(dataProvider.isReady()){
            return dataProvider;
        }else {
            System.out.println("DataProvider: " + dataProvider.getName() + " is not ready");
            parent.getInfo().removeStateEntries(dataProvider.getName());
            parent.getInfo().removeTransitionEntries(dataProvider.getName());
            return null;
        }
    }

    void addProperty(Property property);

    //Used to start computation of values the provider provides
    void compute(Property property, Map<String, Object> arguments);

    Map<String, String> getColumnMap();

    String getName();

    boolean isReady();
}
