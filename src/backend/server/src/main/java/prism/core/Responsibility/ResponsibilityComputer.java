package prism.core.Responsibility;

import prism.api.VariableInfo;
import prism.core.ModelChecker;
import prism.core.Project;
import prism.core.Property.Property;
import prism.db.Batch;
import prism.server.Task;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static prism.core.Namespace.OUTPUT_RESPONSIBILITY;

public class ResponsibilityComputer {
    private final Project project;

    public ResponsibilityComputer(Project project) {
        this.project = project;
    }

    public void computeResponsibility(String propertyName) {

        Optional<Property> p = project.getProperty(propertyName);
        if(p.isPresent()) {
            //Make sure model and database are properly build
            try{
            project.buildModel();
            }catch(Exception e){
                throw new RuntimeException("Error building prism model", e);
            }

            Property property = p.get();
            Map<String, VariableInfo> info = (Map<String, VariableInfo>) project.getInfo().getStateEntry(OUTPUT_RESPONSIBILITY);
            info.get(propertyName).setStatus(VariableInfo.Status.computing);
            project.getInfo().setStateEntry(OUTPUT_RESPONSIBILITY, info);
            //  project.getInfo().setTransitionEntry(OUTPUT_RESPONSIBILITY, info);
            project.getTaskManager().execute(new ResponsibilityComputer.responsibilityComputationTask(property));
        }
    }

    private class responsibilityComputationTask implements Task {
        Property property;

        public responsibilityComputationTask(Property property) {
            this.property = property;
        }

        @Override
        public void run() {
            //compute values and write them in the database
            VariableInfo newInfo = property.computeResponsibility();
            //Replace the info entry for responsibilityInfo
            Map<String, VariableInfo> info = (Map<String, VariableInfo>) project.getInfo().getStateEntry(OUTPUT_RESPONSIBILITY);
            info.replace(property.getName(), newInfo);
            project.getInfo().setStateEntry(OUTPUT_RESPONSIBILITY, info);
        }

        @Override
        public String status() {
            return "Computing responsibility for " + property.getName() + " in Project " + project.getID();
        }

        @Override
        public String name() {
            return "ComputeResponsibility_" + property.getName() + "_" + project.getID();
        }

        @Override
        public Type type() {
            return Type.Responsibility;
        }

        @Override
        public String projectID() {
            return project.getID();
        }
    }
}
