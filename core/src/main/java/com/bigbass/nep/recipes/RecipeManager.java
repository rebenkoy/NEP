package com.bigbass.nep.recipes;

import java.util.Hashtable;
import java.util.List;
import com.bigbass.nep.recipes.processing.Recipe;

public class RecipeManager {
	
	private static RecipeManager instance;
	
	/**
	 * <p>Table of recipes.</p>
	 * 
	 * <p>Please take care when adding recipes! The key is NOT the source name from the JSON data, but rather the
	 * name of the crafting/process in which the recipe goes. So for a Gregtech recipe in the Compressor,
	 * the key would be "Compressor", and all compressor recipes (in a list) would be the key's value.</p>
	 */
	public Hashtable<String, List<Recipe>> recipes;
	
	private RecipeManager(){
		recipes = new Hashtable<String, List<Recipe>>();
	}
	
	public static RecipeManager getInst(){
		if(instance == null){
			instance = new RecipeManager();
		}
		
		return instance;
	}

	/**
	 * Attempts to locate a particular recipe by comparing the provided hashCode with the hashCode of every loaded recipe.
	 * 
	 * This function may cause some lag depending on how fast it finds the recipe.
	 * 
	 * @param hashCode of the recipe
	 * @return the found recipe, or null
	 */
	public Recipe findRecipe(int hashCode){
		if(hashCode == -1){
			return null;
		}
		
		if(recipes != null){
			for(String key : recipes.keySet()){
				for(Recipe rec : recipes.get(key)){
					if(rec.hashCode() == hashCode){
						return rec;
					}
				}
			}
		}
		
		return null;
	}
}
