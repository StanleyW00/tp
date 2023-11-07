package essenmakanan.storage;

import essenmakanan.exception.EssenFileNotFoundException;
import essenmakanan.exception.EssenInvalidEnumException;
import essenmakanan.exception.EssenStorageFormatException;
import essenmakanan.ingredient.Ingredient;
import essenmakanan.logger.EssenLogger;
import essenmakanan.parser.RecipeParser;
import essenmakanan.recipe.Recipe;
import essenmakanan.recipe.RecipeIngredientList;
import essenmakanan.recipe.RecipeStepList;
import essenmakanan.recipe.Step;
import essenmakanan.ui.Ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipeStorage {
    private static Logger logger = Logger.getLogger("RecipeStorage");

    private String dataPath;

    private ArrayList<Recipe> recipeListPlaceholder;

    public RecipeStorage(String path) {
        recipeListPlaceholder = new ArrayList<>();
        dataPath = path;
        EssenLogger.setup(logger);
    }

    public String convertToString(Recipe recipe) {
        String recipeStepString;
        if (recipe.getRecipeSteps().getSteps().isEmpty()) {
            recipeStepString = "EMPTY";
        } else {
            recipeStepString = RecipeParser.convertSteps(recipe.getRecipeSteps().getSteps());
        }

        String ingredientString;
        if (recipe.getRecipeIngredients().getIngredients().isEmpty()) {
            ingredientString = "EMPTY";
        } else {
            ingredientString = RecipeParser.convertIngredient(recipe.getRecipeIngredients().getIngredients());
        }

        return recipe.getTitle() + " || " + recipeStepString + " || " + ingredientString;
    }

    public void saveData(ArrayList<Recipe> recipes)  {
        try {
            FileWriter writer = new FileWriter(dataPath, false);
            String dataString;

            logger.log(Level.INFO, "Transferring recipe data");
            for (Recipe recipe : recipes) {
                dataString = convertToString(recipe);
                writer.write(dataString);
                writer.write(System.lineSeparator());
            }

            writer.close();
            logger.log(Level.INFO, "Recipe data has been successfully saved");
        } catch (IOException exception) {
            Ui.handleIOException(exception);
            logger.log(Level.SEVERE, "Unable to save recipe data", exception);
        }
    }

    private void createNewData(Scanner scan) {
        String dataString = scan.nextLine();
        String[] parsedRecipe = dataString.trim().split(" \\|\\| ");

        logger.log(Level.INFO, "Retrieving recipe data");
        try {
            if (parsedRecipe.length != 3 || parsedRecipe[1].isEmpty()) {
                throw new EssenStorageFormatException();
            }

            String recipeDescription = parsedRecipe[0];

            RecipeStepList steps;
            if (parsedRecipe[1].equals("EMPTY")) {
                ArrayList<Step> emptyStepList = new ArrayList<>();
                steps = new RecipeStepList(emptyStepList);
            } else {
                steps = RecipeParser.parseDataSteps(parsedRecipe[1]);
            }

            RecipeIngredientList ingredientList;
            if (parsedRecipe[2].equals("EMPTY")) {
                ArrayList<Ingredient> emptyIngredientList = new ArrayList<>();
                ingredientList = new RecipeIngredientList(emptyIngredientList);
            } else {
                ingredientList = RecipeParser.parseDataRecipeIngredients(parsedRecipe[2]);
            }

            recipeListPlaceholder.add(new Recipe(recipeDescription, steps, ingredientList));
        } catch (EssenStorageFormatException exception) {
            exception.handleException(dataString);
            logger.log(Level.WARNING, "Data: " + dataString + " has an invalid format", exception);
        } catch (IllegalArgumentException exception) {
            EssenInvalidEnumException.handleException(dataString);
            logger.log(Level.WARNING, "Data: " + dataString + " has an invalid enum", exception);
        }
        logger.log(Level.INFO, "Saved recipe data has been received");
    }

    public ArrayList<Recipe> restoreSavedData() throws EssenFileNotFoundException {
        try {
            File file = new File(dataPath);
            Scanner scan = new Scanner(file);
            while (scan.hasNext()) {
                createNewData(scan);
            }
        } catch (FileNotFoundException exception) {
            logger.log(Level.WARNING, "Text file not found");
            throw new EssenFileNotFoundException();
        }

        return recipeListPlaceholder;
    }
}
