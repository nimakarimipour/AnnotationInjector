package edu.riple.annotationinjector.tools;

import edu.riple.annotationinjector.Fix;
import edu.riple.annotationinjector.Injector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class InjectorTestHelper {

    Map<String, String> fileMap;
    ArrayList<Fix> fixes;
    Injector injector;
    String rootPath;

    public InjectorTestHelper() {
        fixes = new ArrayList<>();
        fileMap = new HashMap<>();
    }

    public InjectorTestHelperOutput addInput(String path, String... input) {
        if(rootPath == null || rootPath.equals("")) throw new RuntimeException("Root path must be set before calling addInput");
        String inputFile = writeToFile("src/" + path, input);
        return new InjectorTestHelperOutput(this, fileMap, inputFile);
    }

    public InjectorTestHelper addFixes(Fix... fixes) {
        for(Fix f : fixes) f.uri = rootPath.concat("/src/").concat(f.uri);
        this.fixes.addAll(Arrays.asList(fixes));
        return this;
    }

    public InjectorTestHelper setRootPath(String path){
        this.rootPath = path;
        makeDirectories();
        return this;
    }

    public void start(){
        this.injector = Injector.builder(Injector.MODE.TEST)
                .setFixesJsonFilePath(rootPath + "/fix/fixes.json").build();
        writeFixes();
        injector.start();
        //todo: compare
    }

    private void writeFixes() {
        JSONArray array = new JSONArray();
        for(Fix fix: fixes){
            array.add(fix.getJson());
        }
        JSONObject obj = new JSONObject();
        obj.put("fixes", array);
        writeToFile("fix/fixes.json", obj.toJSONString());
    }

    private void makeDirectories() {
        String[] names = {"src", "out", "expected", "fix"};
        for (String name : names){
            String pathToDirectory = rootPath + "/" + name;
            try {
                Files.createDirectories(Paths.get(pathToDirectory + "/"));
            } catch (IOException e) {
                throw new RuntimeException("Could not create the directories for name: " + name);
            }
        }
    }

    String writeToFile(String relativePath, String[] input) {
        StringBuilder toWrite = new StringBuilder();
        for (String s : input) toWrite.append(s).append("\n");
        return writeToFile(relativePath, toWrite.toString());
    }

    String writeToFile(String relativePath, String input){
        input = input.replace("\\", "");
        relativePath = rootPath.concat("/").concat(relativePath);
        String pathToFileDirectory = relativePath.substring(0, relativePath.lastIndexOf("/"));
        try {
            Files.createDirectories(Paths.get(pathToFileDirectory + "/"));
            try (Writer writer = Files.newBufferedWriter(
                    Paths.get(relativePath), Charset.defaultCharset())) {
                writer.write(input);
                writer.flush();
                return relativePath;
            }
        } catch (IOException e) {
            throw new RuntimeException("Something terrible happened.");
        }
    }

    public class InjectorTestHelperOutput {

        private final InjectorTestHelper injectorTestHelper;
        private final String inputFile;
        private final Map<String, String> map;

        InjectorTestHelperOutput(InjectorTestHelper injectorTestHelper, Map<String, String> map, String inputFile) {
            this.map = map;
            this.inputFile = inputFile;
            this.injectorTestHelper = injectorTestHelper;
        }

        public InjectorTestHelper expectOutput(String path, String... input) {
            String output = writeToFile("expected/" + path, input);
            map.put(inputFile, output);
            return injectorTestHelper;
        }

        public InjectorTestHelper expectUnchanged() {
            map.put(inputFile, inputFile);
            return injectorTestHelper;
        }
    }
}
