package prism.core.Computation;

import prism.PrismLangException;
import prism.api.DataEntry;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;
import prism.core.Utility.Timer;
import prism.db.Batch;
import prism.server.Task;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class DataProviderTask implements Task {

    protected String name;
    protected Model model;
    protected Property property;
    protected Map<String, Object> arguments;
    protected boolean computed;
    protected boolean highlighting;
    protected boolean hovering;

    public DataProviderTask(String name, Model model, Property property) {
        this(name, model, property, false, false);
    }

    public DataProviderTask(String name, Model model, Property property, boolean highlighting, boolean hovering) {
        this.name = name;
        this.model = model;
        this.property = property;
        this.arguments = new HashMap<>();
        this.computed = checkDB();
        this.highlighting = highlighting;
        this.hovering = hovering;
    }

    private boolean checkDB() {
        boolean entry = model.getDatabase().question(String.format("SELECT column_name FROM information_schema.columns WHERE table_schema = '%s' AND table_name = '%s' AND column_name = '%s'", model.getVersion(), Namespace.TABLE_STATES_BASE, this.getColumnName().toLowerCase()));
        return entry;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments.putAll(arguments);
    }

    protected String getColumnName(){
        return String.format("%s_%s", this.shortName(), property.getID());
    }

    protected String getHighlightName(){
        if (highlighting){
            return String.format("%s Highlighting", this.name);
        }
        return "";
    }

    protected String getHoverName(){
        if (hovering){
            return String.format("Hover %s", this.name);
        }
        return "";
    }

    protected String getHighlightCollumn(){
        if (highlighting){
            return String.format("color_%s_%s", this.shortName(), property.getID());
        }
        return "";
    }

    protected String getHoverCollumn(){
        if (hovering){
            return String.format("hover_%s_%s", this.shortName(), property.getID());
        }
        return "";
    }

    //Status Message visible while computing current Task
    @Override
    public String status(){
        return String.format("Computing %s...", this.name());
    }

    //Name of the Task
    @Override
    public String name(){
        return String.format("%s-%s", this.name, property.getID());
    }

    public abstract String shortName();

    @Override
    public Type type(){
        return Type.Check;
    }

    @Override
    public String projectID(){
        return model.getProjectID();
    }

    @Override
    public String version(){
        return model.getVersion();
    }

    @Override
    public void run(){
        callTool();
        computed = true;
        model.getInfo().getStateEntry(this.name, property.getName()).setStatus(DataEntry.Status.ready);
    }

    public boolean computed(){
        return computed;
    }

    public String getPropertyName(){
        return property.getName();
    }

    protected abstract void callTool();

    public abstract double getMin();

    public abstract double getMax();

    public abstract boolean isReady();

    protected void writeToDatabase(String tableName, String collumnName, String matchingIdentifier, Map<String, String> values){
        //Creates a new Collumn to store the new Values
        try {
            model.getDatabase().execute(String.format("ALTER TABLE %s ADD COLUMN %s TEXT", tableName, collumnName));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Adds value pairs to db
        try (Timer time = new Timer(String.format("Insert %s to db", collumnName), model.getLog())) {
            try (Batch toExecute = model.getDatabase().createBatch(String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, collumnName, matchingIdentifier), 2)) {
                for (Map.Entry<String, String> entry : values.entrySet()){
                    toExecute.addToBatch(entry.getValue(), entry.getKey());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Utility Function that translates the prism state representation to the database ids
    //Takes inputs of one of the forms (v1;v2;v3...), (v1=x1;v2=x2;v3=x3;...), (v1,v2,v3...), (v1=x1,v2=x2,v3=x3,...)
    protected String mapStateToId(String state){
        try{
            BigInteger identifier = model.getModelParser().stateIdentifier(model.getModelParser().parseState(state));
            return identifier.toString();
        } catch (PrismLangException e) {
            throw new RuntimeException("Failed to convert string to state: " + state, e);
        }
    }
}
