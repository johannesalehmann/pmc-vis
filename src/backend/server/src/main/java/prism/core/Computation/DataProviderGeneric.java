package prism.core.Computation;

import prism.PrismException;
import prism.api.DataCategory;
import prism.api.DataEntry;
import prism.api.EditorOption;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;
import prism.server.DataProviderConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DataProviderGeneric<T extends DataProviderTask> implements DataProvider, Namespace {

    protected final String name;
    protected final String executable;
    protected final long timeout;
    protected final Model parent;
    protected final DataEntry.Type type;
    protected final Constructor<T> taskConstructor;
    protected final Map<String, String> extraArguments;

    protected Map<String, T> tasks;

    protected DataProviderGeneric(DataProviderConfiguration config, Model parent, Map<String, String> properties, Constructor<T> taskConstructor) {
        this(config, parent, properties, DataEntry.Type.TYPE_NUMBER, taskConstructor);
    }

    protected DataProviderGeneric(DataProviderConfiguration config, Model parent, Map<String, String> properties, DataEntry.Type type, Constructor<T> taskConstructor) {
        this.name = config.getName();
        this.executable = config.getExecutable();
        this.timeout = config.getTimeout();
        this.extraArguments = config.getExtraArguments();
        this.parent = parent;
        this.taskConstructor = taskConstructor;
        this.type = type;
        this.tasks = new TreeMap<>();

        inititializeTasks(properties);

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
    public void compute(String property, Map<String, String> args){
        T task = tasks.get(property);
        task.setArguments(args);
        parent.getInfo().getStateEntry(this.name, property).setStatus(DataEntry.Status.computing);
        runTask(task);
    }

    protected void inititializeTasks(Map<String, String> properties){
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            this.addProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addProperty(String name, String expression) {
        try {
            int id = tasks.size();
            Map<String, String> args = new HashMap<>();
            args.put("executable", executable);
            args.put("timeout", String.valueOf(timeout));
            args.putAll(extraArguments);
            T task = taskConstructor.newInstance(this.name, id, parent, name, expression, args);
            if (task.isReady()){
                DataEntry.Status status = DataEntry.Status.missing;
                if (task.computed()) {
                    status = DataEntry.Status.ready;
                }
                parent.getInfo().setStateEntry(this.name, new DataEntry(name, task.shortName(), this.type, task.getMin(), task.getMax(), status, task.getHighlightName(), task.getHoverName()));
                tasks.put(name, task);
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

    @Override
    public Map<String, DataProviderTask> getProviderTasks(){
        return (Map<String, DataProviderTask>) this.tasks;
    }

    public Map<String, List<EditorOption>> getEditorOptions(){
        Map<String, List<EditorOption>> editorOptions = new HashMap<>();
        for (T task: tasks.values()) {
            if (task.getEditorOptions()!=null){
                editorOptions.put(task.getPropertyName(), task.getEditorOptions());
            }
        }
        return editorOptions;
    }

    @Override
    public void clear(){
        for (T task: tasks.values()) {
            task.clear();
        }
    }
}
