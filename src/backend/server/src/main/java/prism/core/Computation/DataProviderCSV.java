package prism.core.Computation;

import prism.PrismException;
import prism.PrismLangException;
import prism.api.DataEntry;
import prism.core.Model;
import prism.core.Utility.Timer;
import prism.db.Batch;
import prism.server.DataProviderConfiguration;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataProviderCSV extends DataProviderGeneric<CSVTask>{

    private static Pattern line_pattern = Pattern.compile("^\\s*\\((.*)\\)\\s*(.*)$");

    protected DataProviderCSV(DataProviderConfiguration config, Model parent) {
        super(config, parent, new HashMap<>(), DataEntry.Type.TYPE_NUMBER, null);
        inititializeCSVs();
    }

    private void inititializeCSVs(){
        for(File csvFile : parent.parent.getCSVFiles()){
            addCSV(csvFile.getName().split("\\.")[0], csvFile);
        }
    }

    @Override
    public void addProperty(String name, String expression) {
        //Disable that
    }


    public void addCSV(String name, File csvFile) {

        String seperator = ",";
        if(this.extraArguments.containsKey("separator")){
            seperator = this.extraArguments.get("separator");
        }

        try(BufferedReader br = new BufferedReader(new FileReader(csvFile))){
            String[] head = br.readLine().split(seperator);
            String type = head[0];
            Map<String, Map<String, String>> properties = new HashMap<>();
            Map<String, Double> minimums = new HashMap<>();
            Map<String, Double> maximums = new HashMap<>();
            for(int i = 1; i < head.length; i++){
                properties.put(head[i], new HashMap<>());
                minimums.put(head[i], 0.0);
                maximums.put(head[i], 1.0);
            }

            String line;
            while(br.ready()){
                line = br.readLine();
                Matcher m = line_pattern.matcher(line);
                if(m.find()){
                    String state = m.group(1);
                    String stateID = this.mapStateToId(state);
                    String[] values = m.group(2).split(seperator);
                    if(values.length + 1 != head.length){
                        throw new IllegalArgumentException("Invalid CSV line: " + line);
                    }
                    for(int i = 0; i <values.length; i++){
                        try {
                            double value = Double.parseDouble(values[i]);
                            if(minimums.get(head[i+1]) > value){
                                minimums.put(head[i+1], value);
                            }
                            if(maximums.get(head[i+1]) < value){
                                maximums.put(head[i+1], value);
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid CSV line: " + line + "\n" + e.getMessage());
                        }
                        properties.get(head[i+1]).put(stateID, values[i]);
                    }
                }else{
                    throw new IllegalArgumentException("Invalid CSV line: " + line);
                }
            }
            for (Map.Entry<String, Map<String, String>> entry : properties.entrySet()) {
                CSVTask task = new CSVTask(this.name, tasks.size(), parent, entry.getKey(), Math.floor(minimums.get(entry.getKey())), Math.ceil(maximums.get(entry.getKey())), entry.getValue());

                DataEntry.Status status = DataEntry.Status.missing;
                if (task.computed()) {
                    status = DataEntry.Status.ready;
                }

                parent.getInfo().setStateEntry(this.name, new DataEntry(entry.getKey(), this.type, task.getMin(), task.getMax(), status, task.getHighlightName(), task.getHoverName()));

                tasks.put(entry.getKey(), task);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Utility Function that translates the prism state representation to the database ids
    //Takes inputs of one of the forms (v1;v2;v3...), (v1=x1;v2=x2;v3=x3;...), (v1,v2,v3...), (v1=x1,v2=x2,v3=x3,...)
    private String mapStateToId(String state){
        try{
            BigInteger identifier = parent.getModelParser().stateIdentifier(parent.getModelParser().parseState(state));
            return identifier.toString();
        } catch (PrismLangException e) {
            throw new RuntimeException("Failed to convert string to state: " + state, e);
        }
    }
}
