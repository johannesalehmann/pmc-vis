package prism.resources;

import io.dropwizard.setup.Environment;
import prism.api.Message;
import prism.core.Namespace;
import prism.core.Project;
import prism.server.PRISMServerConfiguration;
import prism.server.TaskManager;

import javax.ws.rs.core.Response;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Resource {

    protected final Environment environment;
    protected final PRISMServerConfiguration configuration;

    protected final String rootDir;
    protected final long cuddMaxMem;

    protected final int maxIterations;
    protected final boolean debug;

    protected TaskManager tasks;
    protected Random random;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";

    protected Resource(Environment environment, PRISMServerConfiguration configuration, TaskManager tasks){
        this.environment = environment;
        this.configuration = configuration;
        this.rootDir = configuration.getPathTemplate();
        this.debug = configuration.getDebug();
        this.cuddMaxMem = configuration.getCUDDMaxMem();
        this.maxIterations = configuration.getIterations();
        this.tasks = tasks;
        this.random = new Random(Instant.now().getEpochSecond());
    }

    private Map<String, Project> currModels;

    protected static Response ok(Object o){
        return Response.ok(o).build();
    }

    protected static Response missing(Message m){
        return Response.status(Response.Status.NOT_FOUND).entity(m).build();
    }

    protected String getRandomNewVersionName(Project p){
        String versionName;
        do{
            versionName = getRandomString(10);
        }while(p.getVersions().contains(versionName));
        return versionName;

    }

    protected String getRandomString(int length){
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    protected static Response error(Object o){
        if (o instanceof Exception) {
            Exception e = (Exception) o;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            String out = String.format("Error %s:\n%s", e.toString(), sw.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(out).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(o).build();
    }

    protected static Response abstractionMissing(long abstractionID){
        return missing(new Message(String.format("AbstractionID %s has not been found", abstractionID)));
    }

    // save uploaded file to new location
    protected void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) throws IOException {
        int read;
        final int BUFFER_LENGTH = 1024;
        final byte[] buffer = new byte[BUFFER_LENGTH];
        OutputStream out = new FileOutputStream(uploadedFileLocation);
        while ((read = uploadedInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
        out.close();
    }

    protected void createStyleFile(String projectID) throws IOException {
        File style = new File(String.format("%s/%s/", rootDir, projectID) + Namespace.STYLE_FILE);
        if (style.createNewFile()){
            try(FileWriter w = new FileWriter(style)){
                w.write(Namespace.DEFAULT_STYLE);
            }
        }
    }

    protected static void createStyleFile(String projectID, String rootDir) throws IOException {
        File style = new File(String.format("%s/%s/", rootDir, projectID) + Namespace.STYLE_FILE);
        if (style.createNewFile()){
            try(FileWriter w = new FileWriter(style)){
                w.write(Namespace.DEFAULT_STYLE);
            }
        }
    }

    protected void refreshProject(String projectID){
        while (tasks.refreshing.getAndSet(true)){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (tasks.containsProject(projectID)) {
            //Project Already exists, refresh (model and) propertyfile
            Project project = tasks.getProject(projectID);
            project.refreshProject();
        }else{
            //Project not initialized, initialize with current Files
            loadProject(projectID);
        }
        tasks.refreshing.set(false);
    }

    protected void loadProject(String projectID){
        File projectdir = new File(String.format("%s/%s", rootDir, projectID));
        loadProject(projectdir);
    }

    protected void loadProject(File file){
        File projectModel = new File(String.format("%s/%s/%s", rootDir, file.getName(), Namespace.PROJECT_MODEL));
        if (file.isDirectory() && projectModel.isFile()){
            try {
                String projectID = file.getName();
                createStyleFile(projectID);
                tasks.createProject(projectID);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static void loadProject(TaskManager tasks, String projectID, PRISMServerConfiguration configuration) throws FileNotFoundException {
        String rootDir = configuration.getPathTemplate();
        File file = new File(String.format("%s/%s", rootDir, projectID));
        File projectModel = new File(String.format("%s/%s", file, Namespace.PROJECT_MODEL));
        if (file.isDirectory() && projectModel.isFile()){
            try {
                createStyleFile(projectID, rootDir);
                tasks.createProject(projectID);
            } catch (Exception e) {
                System.out.println(e);
            }
        }else{
            throw new FileNotFoundException("No Project found at " + file.getAbsoluteFile());
        }
    }

    public static String removePrefix(String name){
        String[] parts = name.split(":");
        if(parts.length == 2){
            return parts[1];
        }
        if(parts.length == 1){
            return parts[0];
        }
        StringBuffer out = new StringBuffer();
        for(int i = 1; i < parts.length; i++){
            if(i > 1){
                out.append(":");
            }
            out.append(parts[i]);
        }
        return out.toString();
    }
}
