package prism.core.Computation;

import prism.PrismException;
import prism.api.DataCategory;
import prism.api.DataEntry;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DataProviderGeneric<T extends DataProviderTask> implements DataProvider, Namespace {

    private final String name;
    private final Model parent;

    private Map<String, T> tasks;

    protected DataProviderGeneric(String name, Model parent, Constructor<T> taskConstructor) {
        this.name = name;
        this.parent = parent;
        this.tasks = new TreeMap<>();

        for (Property property : parent.getProperties()) {
            try {
                T task = taskConstructor.newInstance(name, parent, property);
                tasks.put(property.getName(), task);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if(parent.debug){
            System.out.println(String.format("Created Provider for %s", this.name));
        }
    }

    @Override
    public boolean isReady() {
        for (T task : tasks.values()) {
            if(!task.isReady()){
                return false;
            }
        }
        return true;
    }

    @Override
    public void compute(Property property, Map<String, Object> args){
        T task = tasks.get(property.getName());
        task.setArguments(args);
        parent.getInfo().getStateEntry(this.name, property.getName()).setStatus(DataEntry.Status.computing);
        runTask(task);
    }

    @Override
    public Map<String, String> getColumnMap() {
        Map<String, String> columnMap = new HashMap<>();
        for (T task : tasks.values()) {
            columnMap.put(task.getColumnName().toLowerCase(), task.getPropertyName());
        }

        return columnMap;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected void runTask(T task){
        try {
            parent.getModelChecker().buildModel();
        } catch (PrismException e) {
            throw new RuntimeException(e);
        }

        if (!task.computed()) {
            parent.getTaskManager().execute(task);
        }else{
            parent.getInfo().getStateEntry(this.name, task.getPropertyName()).setStatus(DataEntry.Status.ready);
        }
    }
}
