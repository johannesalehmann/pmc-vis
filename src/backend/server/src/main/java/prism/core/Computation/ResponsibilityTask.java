package prism.core.Computation;

import prism.core.Model;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponsibilityTask extends DataProviderTask {

    private final static Pattern sVaBRespOutputPattern = Pattern.compile("\\((.*)\\)\\s*:\\s*(\\d+(.\\d+)?)");

    public ResponsibilityTask(String name, Model model, Property property) {
        super(name, model, property);
    }

    @Override
    public void callTool() {
        String modelPath = String.format("%s/%s", model.parent.getPath(), model.getModelFile().getName());
        String grouping = this.arguments.containsKey("grouping")?this.arguments.get("grouping").toString():null;

        Map<String, String> results = callSVAResp(modelPath, property.getExpression().toString(), grouping);
        Map<String, String> modifiedResults = new HashMap<>();
        for (Map.Entry<String, String> entry : results.entrySet()) {

            modifiedResults.put(mapStateToId(entry.getKey().replace("{", "(")), entry.getValue());
        }
        this.writeToDatabase(model.getTableStates(), this.getColumnName(), Namespace.ENTRY_S_ID, modifiedResults);
    }

    @Override
    public double getMin(){
        return 0.0;
    }

    @Override
    public double getMax(){
        return 1.0;
    }

    @Override
    public String shortName(){
        return "Resp";
    }

    private static Map<String, String> callSVAResp(String modelPath, String property, String grouping) {
        try {
            String p = property;
            if (property.contains("P>=1.0")) {
                p = property.replace("P>=1.0", "P=1");
            }
            String binaryLocation = "../SVaBResp/target/release/svabresp-cli";
            String call = String.format("./%s", binaryLocation);

            String g = grouping;
            if (g == null) {
                g = "individual";
            }

            System.out.println(modelPath);

            ProcessBuilder builder = new ProcessBuilder(call, "-o", "parsable", "-a", "refinement", "-g", g, modelPath, p);

            System.out.println(builder.command());

            Process process
                    = builder.start();

            StringBuilder logger = new StringBuilder();
            List<String> output = new ArrayList<>();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                logger.append(line + "\n");
                output.add(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                Map<String, String> map = new HashMap<>();
                if (g.equals("individual")) {
                    for (String l : output) {
                        Matcher m = sVaBRespOutputPattern.matcher(l);
                        if (m.find()) {
                            Map<String, String> key = new HashMap<>();
                            for (String pair : m.group(1).split(",")) {
                                String[] split = pair.split("=");
                                key.put(split[0].trim(), split[1].trim());
                            }
                            map.put(key.toString(), m.group(2));
                            System.out.println(key + ":" + m.group(2));
                        }
                    }
                } else {
                    for (String l : output) {
                        String[] pair = l.split(":");
                        map.put(pair[0].trim(), pair[1].trim());
                    }
                }
                return map;
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    logger.append(line + "\n");
                }
                throw new RuntimeException(String.format("Error in SVaBRsp with code %s: \n %s", exitVal, logger.toString()));
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

