import org.json.JSONObject;
import org.json.JSONArray;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.File;
import java.util.stream.Collectors;
import java.io.IOException;

/**
 * @version 1.0
 * @author B. Clint Hall
 */
public class Sb2 {
    private static final String NO_JSON = "This project contains no "
        + "data.\nThe .sb2 archive contains no project.json file.";
    private static final String CORRUPT_JSON = "This project's data "
        + "is corrupt.\nThe project.json file in the .sb2 archive "
        + "is not parcable json text.";
    private static final String IO_PROBLEM = "We failed to read this "
        + "project from disk.\nPlease try again.";
    private JSONObject stage;
    private Sprites sprites;
    private String name;
    private String errorMessage = null;
    private Script[] stageScripts;
    /**
     * Construct an Sb2 object from a filePath.
     * @param filePath Path to sb2 file.
     */
    public Sb2(String filePath) {
        name = new File(filePath).getName();
        String jsonString = "";
        try {
            jsonString = Extractor.getProjectJSON(filePath);
        } catch (IOException e) {
            errorMessage = IO_PROBLEM;
            return;
        }
        if (jsonString == null) {
            errorMessage = NO_JSON;
            return;
        } 
        try {
            JSONObject jsonObject = createJSONObject(jsonString);
            configureWithJson(jsonObject);
        } catch (org.json.JSONException e) {
            errorMessage = CORRUPT_JSON;
        }
    }
    /**
     * Construct an Sb2 using a JSONObject and a name.  Used in testing.
     * @param stage The JSONObject.
     * @param name The name for the Scratch project. Normally the name of the .sb2 file.
     */
    public Sb2(JSONObject stage, String name) {
        this.name = name;
        configureWithJson(stage);
    }
    /**
     * Construct an Sb2 object from a JSONObject. Useful for testing.
     * @param stage JSONObject from which to construct Sb2
     */
    public Sb2(JSONObject stage) {
        this(stage, "ScratchProject");
    }
    
    /**
     * Function to be called from all constructors.
     * @param stage JSONObject which is underlying data structure for Sb2.
     */
    public void configureWithJson(JSONObject stage) {
        this.stage = stage;
        this.sprites = new Sprites(stage);
        extractStageScripts();
    }

    /**
     * Return underlying JSONObject.
     * @return The underlying JSONObject
     */
    public JSONObject getJSONObject() {
        return stage;
    }

    /**
     * The root object (the stage) can have scripts of it's own.
     * @return the array of scripts of the stage of this sb2.
     */
    public Script[] getScriptsForStage() {
        return stageScripts; 
    }
    /**
     * Called by the constructor.  Sets the private field {@code stageScripts}
     * from the {@code stage} field.
     */
    private void extractStageScripts() {
        JSONArray jsonArrayOfScriptTuples = stage.optJSONArray("scripts");
        stageScripts = Script.getScriptArray(jsonArrayOfScriptTuples);
    }
    /**
     * Given a file path return a String of file contents.
     * @param pathStr Path to the file
     * @return contents of file
     */
    public static String getFileContents(String pathStr) {
        Path path = Paths.get(pathStr);
        String fileString = null;
        try {
            fileString = Files.lines(path).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileString;
    }
    /**
     * Given a path to a json file, return a JSONObject.
     * @param jsonString Path to the json file
     * @return org.json.JSONObject
     */
    public static JSONObject createJSONObject(String jsonString) {
        return new JSONObject(jsonString);
    }

    /**
     * Print the current working directory.  Useful for debugging.
     * Thank you http://stackoverflow.com/a/15954821
     */
    public static void printCwd() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);
    }
    /**
     * Return the name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    //Sb2 has a Sprites. These are the sprite methods
    /**
     * Each sprite has a unique name.
     * This method returns these names in a array of strings.
     * @return Array of sprite names.
     */
    public String[] getSpriteNames() {
        return sprites.getSpriteNames();
    }
    /**
     * Return the number of Scripts associated with a particular Sprite.
     * @param spriteName The name of the Sprite.
     * @return The number of Scripts associated with the Sprite.
     */
    public int getScriptCountForSprite(String spriteName) {
        return sprites.getScriptCountForSprite(spriteName);
    }
    /**
     * Count the number of blocks in each script for the given Sprite.
     * @param spriteName The name of the sprite in question.
     * @return An array with the lengths of each script
     *         associated with the Sprite.
     */
    public int[] getScriptLengthsForSprite(String spriteName) {
        return sprites.getScriptLengthsForSprite(spriteName);
    }

    /**
     * Returns scripts for named sprite.
     * @param spriteName name of the sprite whose scripts you want.
     * @return array of Script objects
     */
    public Script[] getScriptsForSprite(String spriteName) {
        return sprites.getScriptsForSprite(spriteName);
    }
    /**
     * Gets the number of global variables within the stage object.
     * @return The number of global variables
     */
    public int getGlobalVariableCount() {
        JSONArray variables = stage.optJSONArray("variables");
        if (variables == null) {
            return 0;
        }
        return variables.length();
    }

    /**
     * If an error has occurred, return the error message to be printed in
     * the report.  Otherwise, return null.
     * @return Error message if any, otherwise null.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}

