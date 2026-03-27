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

    protected final String name;
    protected final Model parent;
    protected final DataEntry.Type type;
    protected final Constructor<T> taskConstructor;

    protected Map<String, T> tasks;

    protected DataProviderGeneric(String name, Model parent, Constructor<T> taskConstructor) {
        this(name, parent, DataEntry.Type.TYPE_NUMBER, taskConstructor);
    }

    protected DataProviderGeneric(String name, Model parent, DataEntry.Type type, Constructor<T> taskConstructor) {
        this.name = name;
        this.parent = parent;
        this.taskConstructor = taskConstructor;
        this.type = type;
        this.tasks = new TreeMap<>();

        inititializeTasks();

        if(parent.debug){
            System.out.println(String.format("Created Provider for %s", this.name));
        }
    }

    @Override
    public boolean isReady() {
        if(tasks.isEmpty()){
            return false;
        }
        for (T task : tasks.values()) {
            if(!task.isReady()){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(String property){
        return tasks.containsKey(property);
    }

    @Override
    public void compute(String property, Map<String, Object> args){
        T task = tasks.get(property);
        task.setArguments(args);
        parent.getInfo().getStateEntry(this.name, property).setStatus(DataEntry.Status.computing);
        runTask(task);
    }

    protected void inititializeTasks(){
        for (Property property : parent.getProperties()) {
            this.addProperty(property);
        }
    }

    @Override
    public void addProperty(Property property) {
        try {
            T task = taskConstructor.newInstance(name, parent, property);
            if (task.isReady()){
                DataEntry.Status status = DataEntry.Status.missing;
                if (task.computed()) {
                    status = DataEntry.Status.ready;
                }
                parent.getInfo().setStateEntry(this.name, new DataEntry(property.getName(), this.type, task.getMin(), task.getMax(), status, task.getHighlightName(), task.getHoverName()));
                tasks.put(property.getName(), task);
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String[]> getColumnMap() {
        Map<String, String[]> columnMap = new HashMap<>();
        for (T task : tasks.values()) {
            String property = task.getPropertyName();
            String[] values = new String[] {this.getName(), property};
            columnMap.put(task.getColumnName().toLowerCase(), values);

            String highlightCollumn = task.getHighlightCollumn();
            if(!highlightCollumn.isEmpty()){
                values = new String[] {task.getHighlightName(), property};
                columnMap.put(highlightCollumn.toLowerCase(), values);
            }

            String hoverCollumn = task.getHoverCollumn();
            if(!hoverCollumn.isEmpty()){
                values = new String[] {task.getHoverName(), property};
                columnMap.put(hoverCollumn.toLowerCase(), values);
            }
        }

        return columnMap;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isBool(){
        return this.type.equals(DataEntry.Type.TYPE_BOOL);
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
