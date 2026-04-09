package prism.core.Computation;

import prism.PrismLangException;
import prism.api.DataEntry;
import prism.api.EditorHighlighting;
import prism.api.EditorOption;
import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;
import prism.core.Utility.Timer;
import prism.db.Batch;
import prism.server.Task;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataProviderTask implements Task {

    protected String name;
    protected int id;
    protected Model model;
    protected String property;
    protected String propertyExpression;
    protected Map<String, String> arguments;
    protected boolean computed;
    protected boolean highlighting;
    protected boolean hovering;

    public DataProviderTask(String name, int id, Model model, String property, String propertyExpression, Map<String, String> arguments) {
        this(name, id, model, property, propertyExpression, arguments, false, false);
    }

    public DataProviderTask(String name, int id, Model model, String property, String propertyExpression, Map<String, String> arguments, boolean highlighting, boolean hovering) {
        this.name = name;
        this.id = id;
        this.model = model;
        this.property = property;
        this.propertyExpression = propertyExpression;
        this.arguments = new HashMap<>(arguments);
        this.computed = checkDB();
        this.highlighting = highlighting;
        this.hovering = hovering;
    }

    private boolean checkDB() {
        boolean entry = model.getDatabase().question(String.format("SELECT column_name FROM information_schema.columns WHERE table_schema = '%s' AND table_name = '%s' AND column_name = '%s'", model.getVersion(), Namespace.TABLE_STATES_BASE, this.getColumnName().toLowerCase()), false);
        return entry;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments.putAll(arguments);
    }

    protected String getColumnName(){
        return String.format("%s_%s", this.shortName(), id);
    }

    public String getHighlightName(){
        if (highlighting){
            return String.format("%s Highlighting", this.name);
        }
        return "";
    }

    public String getHoverName(){
        if (hovering){
            return String.format("Hover %s", this.name);
        }
        return "";
    }

    public String getHighlightCollumn(){
        if (highlighting){
            return String.format("color_%s_%s", this.shortName(), id);
        }
        return "";
    }

    public String getHoverCollumn(){
        if (hovering){
            return String.format("hover_%s_%s", this.shortName(), id);
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
        return String.format("%s-%s", shortName(), id);
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
        System.out.println("Running " + name());
        try {
            callTool();
        }catch (Exception e){
            model.getInfo().getStateEntry(this.name, this.getPropertyName()).setError(String.format("Failed to run %s:\n%s", this.name, e.getMessage()));
            return;
        }
        computed = true;
        model.getInfo().getStateEntry(this.name, this.getPropertyName()).setStatus(DataEntry.Status.ready);
    }

    public boolean computed(){
        return computed;
    }

    public String getPropertyName(){
        return property;
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
            String intern =state;

            // Remove parentheses if necessary
            if (intern.startsWith("(")) {
                intern = state.substring(1, state.length() - 1);
                //System.out.println(intern);
            }

            //Replace , by ;
            if (!intern.contains(";")) {
                intern = intern.replace(",", ";");
                //System.out.println(intern);
            }

            if (intern.contains("=")) {
                String[] parts = intern.split(";");
                String[] newParts = new String[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    String key = parts[i].trim();
                    if(!key.contains("=")){
                        if (key.startsWith("!")){
                            newParts[i] = key.substring(1) + "=false";
                        }else{
                            newParts[i] = key + "=true";
                        }
                    }else{
                        newParts[i] = key;
                    }
                }
                intern = String.join(";", newParts);
                System.out.println(intern);
            }
            BigInteger identifier = model.getModelParser().stateIdentifier(model.getModelParser().parseState(intern));
            return identifier.toString();
        } catch (PrismLangException e) {
            throw new RuntimeException("Failed to convert string to state: " + state, e);
        }
    }

    public List<String> getHighlightedStates() {
        String query = String.format("SELECT %s FROM %s WHERE %s = '1'", Namespace.ENTRY_S_ID, model.getTableStates(), this.getHighlightCollumn());
        return model.getDatabase().executeCollectionQuery(query, String.class);
    }

    public List<EditorOption> getEditorOptions(){
        return null;
    }

    public List<EditorHighlighting> getEditorHighlighting(List<String> arguments){
        return null;
    }

    public void clear(){
        this.computed = false;
        model.getInfo().getStateEntry(this.name, this.getPropertyName()).setStatus(DataEntry.Status.missing);
    }
}
