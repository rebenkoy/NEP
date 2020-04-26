package com.bigbass.nep.gui.nodes;

import java.util.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bigbass.nep.gui.Path;
import com.bigbass.nep.gui.borders.BorderedTable;
import com.bigbass.nep.recipes.elements.Pile;
import com.bigbass.nep.recipes.processing.Recipe;
import com.bigbass.nep.skins.SkinManager;

/**
 * Represents a production node, most commonly a machine or a crafting table, with a particular recipe.
 */
public class Node {
	public final float width = 240;

	private BorderedTable table;
	private Recipe recipe;
	
	public Vector2 pos;
	
	private boolean shouldRemove;

	public UUID uuid;

	public Map<String, List<Path>> inputs;
	public Map<String, Path> outputs;

	private Map<String, Table> inputTables;
	private Map<String, Table> outputTables;

	public Node(float x, float y){
		this(x, y, null);
	}

	public Node(float x, float y, Recipe recipe) {
		this(x, y, recipe, UUID.randomUUID());
	}

	public Node(float x, float y, Recipe recipe, UUID uuid) {
		System.out.println("DEPRECATED Node Constructor");
		this.inputs = new HashMap<>();
		this.outputs = new HashMap<>();
		this.inputTables = new HashMap<>();
		this.outputTables = new HashMap<>();
		this.uuid = uuid;
		pos = new Vector2(x, y);
		
		table = new BorderedTable(SkinManager.getSkin("fonts/droid-sans-mono.ttf", 10)); // font doesn't really matter here, but skin necessary for other stuff
		
		shouldRemove = false;
		
		if(recipe != null){
			refresh(recipe);
		} else {
			refresh();
		}
	}
	
	public void refresh(){
		NodeTableBuilder.build(this, table);
		table.setPosition(pos.x, pos.y);
	}
	
	public void refresh(Recipe recipe){
		this.recipe = recipe;
		for (Pile pile : recipe.inputs) {
			this.inputs.put(pile.element.name(), new LinkedList<>());
		}
		refresh();
	}
	
	public Actor getActor(){
		return table;
	}
	
	public Recipe getRecipe(){
		return recipe;
	}

	public void setForRemoval(){
		shouldRemove = true;
	}
	public boolean shouldRemove(){
		return shouldRemove;
	}
	
	public JsonObject toJson(){
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("uuid", this.uuid.toString());

		builder.add("x", pos.x);
		builder.add("y", pos.y);
		builder.add("r", this.recipe.toJson(true));
		
		return builder.build();
	}

	public static Node fromJson(JsonObject jsonNode) {
		final UUID uuid = UUID.fromString(jsonNode.getJsonString("uuid").getString());
		final float x = (float) jsonNode.getJsonNumber("x").doubleValue();
		final float y = (float) jsonNode.getJsonNumber("y").doubleValue();
		final Recipe recipe = Recipe.fromJson(jsonNode.getJsonObject("r"));

		return new Node(
				x,
				y,
				recipe,
				uuid
		);
	}

	public Vector2 getConnectionPos(String name, boolean input) {
		float x, y;
		if (input) {
			Table tbl = this.inputTables.get(name);
			y = this.pos.y + tbl.getY() + tbl.getPrefHeight() / 2;
			x = this.pos.x;
		} else {
			Table tbl = this.outputTables.get(name);
			y = this.pos.y + tbl.getY() + tbl.getPrefHeight() / 2;
			x = this.pos.x + this.width;
		}
		return new Vector2(x, y);
	}


	public void addInputTable(String name, Table table) {
		this.inputTables.put(name, table);
	}

	public void addOutputTable(String name, Table table) {
		this.outputTables.put(name, table);
	}
}
