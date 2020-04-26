package com.bigbass.nep.recipes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.bigbass.nep.recipes.delivery.Importer;
import com.bigbass.nep.recipes.delivery.PeerConfig;
import com.bigbass.nep.recipes.elements.AElement;
import com.bigbass.nep.recipes.processing.Recipe;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Loader {
    private static void load(Path path) {
        try {
            JsonReader reader = Json.createReader(new FileReader(path.toString()));
            String group = path.getFileName().toString();
            for (JsonValue val : reader.readArray()) {
                Recipe.fromJson(val.asJsonObject(), group);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void load(PeerConfig config) throws IOException {
        final FileHandle handle = Gdx.files.local(Importer.getRoot() + config.alias + '/');

        JsonReader mendeleyReader = Json.createReader(new FileReader(handle.file().getPath() + "/mendeley.json"));
        for (JsonValue rawElem : mendeleyReader.readArray()) {
            AElement.fromJson(rawElem.asJsonObject());
        }

        Files.walk(Paths.get(handle.file().getPath() + "/sources/"))
                .filter(Files::isRegularFile)
                .forEach(Loader::load);
    }
}
