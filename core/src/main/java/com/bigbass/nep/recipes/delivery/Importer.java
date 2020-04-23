package com.bigbass.nep.recipes.delivery;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.bigbass.nep.recipes.elements.AElement;
import com.bigbass.nep.recipes.elements.Pile;
import com.bigbass.nep.recipes.elements.usual.Fluid;
import com.bigbass.nep.recipes.elements.usual.Item;
import com.bigbass.nep.recipes.processing.Recipe;
import com.bigbass.nep.util.UJSON;

import javax.json.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Importer {
    private static class RecExKernel {
        static private void populateRecipeIO(Map<String, Pile> collection, JsonValue i, boolean isItem) {
            if (i == JsonValue.NULL) {
                return;
            }
            JsonObject json = i.asJsonObject();
            AElement element;
            if (isItem) {
                Map<String, String> nbt = new HashMap<>();
                if (json.containsKey("cfg")) {
                    nbt.put("cfg", Integer.toString(json.getInt("cfg")));
                }
                element = new Item(json.getString("uN"), json.getString("lN"), nbt);
            } else {
                element = new Fluid(json.getString("uN"), json.getString("lN"));
            }
            if (AElement.mendeley.containsKey(element.eid())) {
                element = AElement.mendeley.get(element.eid());
            } else {
                AElement.mendeley.put(element.eid(), element);
            }
            if (!collection.containsKey(element.eid())) {
                collection.put(element.eid(), new Pile(json.getInt("a"), element));
            } else {
                collection.get(element.eid()).amount += json.getInt("a");
            }
        }

        public static void imp(PeerConfig config) throws StageException {
            try {
                FileHandle dir = Gdx.files.local(Unpacker.getRoot() + config.alias);
                if (dir.list().length != 1) {
                    throw new StageException();
                }
                JsonReader reader = Json.createReader(new FileReader(dir.list()[0].file().getPath()));
                JsonObject root;
                root = reader.readObject();

                final String versionFolder = Importer.getRoot() + config.alias + "/";
                final String hrFolder = versionFolder + "sources/";
                for (JsonValue src : root.getJsonArray("sources")) {
                    JsonObject source = src.asJsonObject();

                    final String type = source.getJsonString("type").getString();
                    final String typeFolder = hrFolder + type + "/";
                    final FileHandle typeFolderHandler = Gdx.files.local(typeFolder);
                    typeFolderHandler.mkdirs();

                    if (type.equalsIgnoreCase("gregtech")) {
                        for (JsonValue mch : source.getJsonArray("machines")) {
                            JsonObject machine = mch.asJsonObject();
                            String name = machine.getString("n").trim();
                            JsonArrayBuilder list = Json.createArrayBuilder();
                            try {
                                for (JsonValue rv : machine.getJsonArray("recs")) {
                                    JsonObject json = rv.asJsonObject();
                                    Recipe recipe = new Recipe();
                                    recipe.duration = json.getInt("dur");
                                    Map<String, Pile> inputs = new HashMap<>();
                                    try {
                                        if (json.containsKey("iI")) {
                                            for (JsonValue i : json.getJsonArray("iI")) {
                                                populateRecipeIO(inputs, i, true);
                                            }
                                        }
                                        if (json.containsKey("fI")) {
                                            for (JsonValue i : json.getJsonArray("fI")) {
                                                populateRecipeIO(inputs, i, false);
                                            }
                                        }
                                        recipe.inputs.addAll(inputs.values());

                                        Map<String, Pile> outputs = new HashMap<>();
                                        if (json.containsKey("iO")) {
                                            for (JsonValue i : json.getJsonArray("iO")) {
                                                populateRecipeIO(outputs, i, true);
                                            }
                                        }
                                        if (json.containsKey("fO")) {
                                            for (JsonValue i : json.getJsonArray("fO")) {
                                                populateRecipeIO(outputs, i, false);
                                            }
                                        }
                                        recipe.outputs.addAll(outputs.values());
                                    } catch (Exception e) {
                                        System.out.println("error reading recipe:");
                                        System.out.println(name);
                                        System.out.println(json);
                                    }

                                    list.add(recipe.toJson());
                                }

                                FileWriter writer = new FileWriter(typeFolder + name + ".json");
                                writer.write(UJSON.prettyPrint(list.build()));
                                writer.close();
                            } catch (IOException e) {
                                System.out.println(e.toString());
                                throw new StageException();
                            }
                        }
                    } else if (!type.equals("shapedOreDict")) {
                        JsonArrayBuilder list = Json.createArrayBuilder();
                        try {
                            for (JsonValue rv : source.getJsonArray("recipes")) {
                                JsonObject json = rv.asJsonObject();
                                Recipe recipe = new Recipe();
                                recipe.duration = 0;
                                Map<String, Pile> inputs = new HashMap<>();
                                try {
                                    if (json.containsKey("iI")) {
                                        for (JsonValue i : json.getJsonArray("iI")) {
                                            populateRecipeIO(inputs, i, true);
                                        }
                                    }
                                    if (json.containsKey("fI")) {
                                        for (JsonValue i : json.getJsonArray("fI")) {
                                            populateRecipeIO(inputs, i, false);
                                        }
                                    }
                                    recipe.inputs.addAll(inputs.values());

                                    Map<String, Pile> outputs = new HashMap<>();
                                    if (json.containsKey("iO")) {
                                        for (JsonValue i : json.getJsonArray("iO")) {
                                            populateRecipeIO(outputs, i, true);
                                        }
                                    }
                                    if (json.containsKey("o")) {
                                        populateRecipeIO(outputs, json.getJsonObject("o"), true);
                                    }
                                    if (json.containsKey("fO")) {
                                        for (JsonValue i : json.getJsonArray("fO")) {
                                            populateRecipeIO(outputs, i, false);
                                        }
                                    }
                                    recipe.outputs.addAll(outputs.values());
                                } catch (Exception e) {
                                    System.out.println("error reading recipe:");
                                    System.out.println(e);
                                    System.out.println(type);
                                    System.out.println(json);
                                }

                                list.add(recipe.toJson());
                            }

                            FileWriter writer = new FileWriter(typeFolder + "recipes.json");
                            writer.write(UJSON.prettyPrint(list.build()));
                            writer.close();
                        } catch (IOException e) {
                            System.out.println(e.toString());
                            throw new StageException();
                        }
                    }
                }

                JsonArrayBuilder mendeteyBuilder = Json.createArrayBuilder();
                for (AElement element : AElement.mendeley.values()) {
                    mendeteyBuilder.add(element.toJson());
                }
                try {
                    FileWriter writer = new FileWriter(versionFolder + "mendeley.json");
                    writer.write(UJSON.prettyPrint(mendeteyBuilder.build()));
                    writer.close();
                } catch (IOException e) {
                    System.out.println("error writing items list");
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                System.out.println(String.format("could not find file at %s", Unpacker.getRoot() + config.alias));
                throw new StageException();
            }
        }
    }

    public static String getRoot() {
        return "cache/imported/";
    }

    private static Map<String, BackingUpCache.StageExceptionFunction> kernelMapping;
    static {
        Importer.kernelMapping = new HashMap<>();
        Importer.kernelMapping.put("recex", Importer.RecExKernel::imp);
    }

    public static void imp(PeerConfig config) throws StageException {
        if (Importer.kernelMapping.containsKey(config.importKernel)) {
            Importer.kernelMapping.get(config.importKernel).apply(config);
        } else {
            System.out.println(String.format("Unknown import kernel: '%s'", config.unpackKernel));
            throw new StageException();
        }
    }
}
