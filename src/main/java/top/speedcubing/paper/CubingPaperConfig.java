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
import net.minecraft.server.MinecraftServer;
import org.github.paperspigot.KnockbackCommand;
import org.github.paperspigot.PaperSpigotConfig;
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
    public static double knockbackFriction = 2.0;
    public static double knockbackHorizontal = 0.4;
    public static double knockbackVertical = 0.4;
    public static double knockbackVerticalLimit = 0.4;
    public static double knockbackExtraHorizontal = 0.5;
    public static double knockbackExtraVertical = 0.1;

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
            cleanLogs = settings.get("clean-logs").getAsBoolean();
            commandOP = settings.get("command-op").getAsBoolean();
            limitBlockPlaceDistance = settings.get("block-place-distance-limit").getAsBoolean();
            opsJson = settings.get("ops.json").getAsBoolean();
            playerDataSaving = settings.get("player-data-saving").getAsBoolean();
            SpigotConfig.disableStatSaving = !playerDataSaving;

            String r = settings.get("restart-command").getAsString();
            restartArgument = r.isEmpty() ? null : r.split(" ");

            //FlamePaper
            adaptativeChunkGC = settings.get("adaptative-chunk-gc").getAsBoolean();
            allowMapDecorations = settings.get("allow-map-decorations").getAsBoolean();
            bookMaxPages = settings.get("book.max_pages").getAsInt();
            knockbackFriction = settings.get( "knockback.friction").getAsDouble();
            knockbackHorizontal = settings.get( "knockback.horizontal").getAsDouble();
            knockbackVertical = settings.get( "knockback.vertical").getAsDouble();
            knockbackVerticalLimit = settings.get( "knockback.vertical-limit").getAsDouble();
            knockbackExtraHorizontal = settings.get( "knockback.extra-horizontal").getAsDouble();
            knockbackExtraVertical = settings.get( "knockback.extra-vertical").getAsDouble();

            MinecraftServer.getServer().server.getCommandMap().register( "knockback", "PaperSpigot", new KnockbackCommand( "knockback", knockbackFriction,
                    knockbackHorizontal, knockbackVertical, knockbackVerticalLimit, knockbackExtraHorizontal,
                    knockbackExtraVertical));

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
