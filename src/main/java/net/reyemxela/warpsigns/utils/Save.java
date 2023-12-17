package net.reyemxela.warpsigns.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.WorldSavePath;
import net.reyemxela.warpsigns.Coords;
import net.reyemxela.warpsigns.PairingInfo;
import net.reyemxela.warpsigns.WarpSigns;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * The Save class is responsible for handling the serialization and deserialization of data for the WarpSigns mod.
 */
public class Save {
    private static final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    private static Gson gson;
    public static File dataFile = null;

    private static final JsonSerializer<PairingInfo> serializer = (src, typeOfSrc, context) -> {
        JsonObject jsonPairingInfo = new JsonObject();
        jsonPairingInfo.addProperty("pairedSign", src.pairedSign.getKey());
        jsonPairingInfo.addProperty("pairedSignDest", src.pairedSignDest.getKey());
        jsonPairingInfo.addProperty("facing", src.facing);
        return jsonPairingInfo;
    };

    private static final JsonDeserializer<PairingInfo> deserializer = (json, typeOfT, context) -> {
        JsonObject jsonObject = json.getAsJsonObject();

        Coords pairedSign = new Coords(jsonObject.get("pairedSign").getAsString());
        Coords pairedSignDest = new Coords(jsonObject.get("pairedSignDest").getAsString());
        int facing = jsonObject.get("facing").getAsInt();

        return new PairingInfo(pairedSign, pairedSignDest, facing);
    };

    /**
     * Initializes the WarpSigns module.
     * - Registers type adapters for PairingInfo serialization and deserialization
     * - Creates a Gson instance
     * - Sets the dataFile to the location 'warpSignData.json' in the server's save path
     * - Loads the data from the dataFile
     */
    public static void initialize() {
        gsonBuilder.registerTypeAdapter(PairingInfo.class, serializer);
        gsonBuilder.registerTypeAdapter(PairingInfo.class, deserializer);
        gson = gsonBuilder.create();

        dataFile = new File(WarpSigns.serverInstance.getSavePath(WorldSavePath.ROOT).toString(), "warpSignData.json");
        loadData();
    }

    /**
     * Loads the data from the dataFile.
     * - Attempts to read the dataFile using a FileReader
     * - Converts the data read from the file into a HashMap with String keys and PairingInfo values
     * - Closes the FileReader
     * - Logs a message indicating that the warpSign save was successfully loaded
     * <p>
     * If an exception occurs during the loading process, the following actions are taken:
     * - Logs a message indicating that a new save file will be created
     * - Creates a new empty HashMap for the warpSignData
     * - Calls the saveData method to save the newly created empty data
     */
    private static void loadData() {
        try {
            final var reader = new FileReader(dataFile);
            final var mapType = new TypeToken<HashMap<String, PairingInfo>>() {
            }.getType();
            WarpSigns.warpSignData = gson.fromJson(reader, mapType);
            reader.close();
            WarpSigns.LOGGER.info("Loaded warpSign save from file");
        } catch (IOException | JsonSyntaxException | JsonIOException err) {
            WarpSigns.LOGGER.info("Creating new save file");
            WarpSigns.warpSignData = new HashMap<>();
            saveData();
        }
    }

    /**
     * Saves the warpSign data to the dataFile.
     * - Attempts to write the warpSignData using a FileWriter
     * - Closes the FileWriter
     * - Logs a message indicating that the warpSign save file was successfully saved
     * <p>
     * If an exception occurs during the saving process, the following action is taken:
     * - Logs an error message indicating that the warpSign save file creation was unsuccessful
     */
    public static void saveData() {
        try {
            final var writer = new FileWriter(dataFile);
            gson.toJson(WarpSigns.warpSignData, writer);
            writer.close();
            WarpSigns.LOGGER.info("Saved warpSign save file");
        } catch (IOException | JsonIOException err) {
            WarpSigns.LOGGER.error("Unable to create warpSign save file!");
        }
    }
}
