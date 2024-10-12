package top.speedcubing.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.spigotmc.SpigotConfig;

public class CubingPaperConfig {

    public static JsonObject config;

    //CubingPaper - Configurations
    public static boolean cleanLogs = false;
    public static boolean commandOP = true;
    public static boolean limitBlockPlaceDistance = true;
    public static boolean opsJson = true;
    public static boolean playerDataSaving = true;
    public static String[] restartArgument;

    //FlamePaper
    public static boolean adaptativeChunkGC = false;
    public static boolean allowMapDecorations = true;
    public static int bookMaxPages = 5;

    //Taco
    public static boolean optimizeArmorStandMovement = false; //world
    public static boolean isRedstoneFireBPE = true; //world

    public static void init() {
        try {
            if (!new File("cubingpaper.json").exists()) {
                InputStream file = CubingPaperConfig.class.getClassLoader().getResourceAsStream("configurations/cubingpaper.json");
                Files.copy(file, Paths.get("cubingpaper.json"), StandardCopyOption.REPLACE_EXISTING);
            }
            config = JsonParser.parseReader(new FileReader("cubingpaper.json")).getAsJsonObject();
            JsonObject settings = config.getAsJsonObject("settings");

            //CubingPaper - Configurations
            cleanLogs = settings.get("cleanLogs").getAsBoolean();
            commandOP = settings.get("commandOP").getAsBoolean();
            limitBlockPlaceDistance = settings.get("limitBlockPlaceDistance").getAsBoolean();
            opsJson = settings.get("opsJson").getAsBoolean();
            playerDataSaving = settings.get("playerDataSaving").getAsBoolean();
            SpigotConfig.disableStatSaving = !playerDataSaving;

            String r = settings.get("restartCommand").getAsString();
            restartArgument = r.isEmpty() ? null : r.split(" ");

            //FlamePaper
            adaptativeChunkGC = settings.get("adaptativeChunkGC").getAsBoolean();
            allowMapDecorations = settings.get("allowMapDecorations").getAsBoolean();
            bookMaxPages = settings.get("bookMaxPages").getAsInt();

            optimizeArmorStandMovement = settings.get("armor-stand.optimize-movement").getAsBoolean();
            isRedstoneFireBPE = settings.get("redstone-fire-BlockPhysicsEvent").getAsBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void update() {
        try {
            FileWriter fileWriter = new FileWriter("cubingpaper.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
