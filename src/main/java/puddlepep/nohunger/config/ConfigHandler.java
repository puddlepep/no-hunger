package puddlepep.nohunger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.*;
import puddlepep.nohunger.NoHunger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("no-hunger.json");

    public static Config loadConfig() {

        if (!Files.exists(configPath)) {
            buildConfig();
        }

        return readConfig();
    }

    private static Config readConfig() {

        Gson gson = new Gson();
        Config config;

        try {
            String json = Files.readString(configPath);
            config = gson.fromJson(json, Config.class);

        } catch (Exception e) {
            NoHunger.LOGGER.error("Error reading config file, using default values.");
            config = new Config();
        }

        return config;
    }

    private static void buildConfig() {

        Config defaultConfig = new Config();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(defaultConfig);

        try {
            FileWriter configWriter = new FileWriter(configPath.toString());
            configWriter.write(json);
            configWriter.close();

        } catch (IOException e) {
            NoHunger.LOGGER.error("Error when creating config file.");
        }
    }
}
