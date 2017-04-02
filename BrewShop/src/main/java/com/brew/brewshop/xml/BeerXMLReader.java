package com.brew.brewshop.xml;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.brew.brewshop.R;
import com.brew.brewshop.fragments.RecipeListFragment;
import com.brew.brewshop.storage.recipes.BeerStyle;
import com.brew.brewshop.storage.recipes.Hop;
import com.brew.brewshop.storage.recipes.HopAddition;
import com.brew.brewshop.storage.recipes.HopUsage;
import com.brew.brewshop.storage.recipes.Malt;
import com.brew.brewshop.storage.recipes.MaltAddition;
import com.brew.brewshop.storage.recipes.Quantity;
import com.brew.brewshop.storage.recipes.Recipe;
import com.brew.brewshop.storage.recipes.Weight;
import com.brew.brewshop.storage.recipes.Yeast;
import com.brew.brewshop.storage.style.BjcpCategory;
import com.brew.brewshop.storage.style.BjcpCategoryList;
import com.brew.brewshop.storage.style.BjcpCategoryStorage;
import com.brew.brewshop.storage.style.BjcpSubcategory;
import com.brew.brewshop.storage.style.VitalStatistics;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class BeerXMLReader extends AsyncTask<InputStream, Integer, Recipe[]> {
    private static final String TAG = BeerXMLReader.class.getName();

    private ProgressDialog dialog;
    private Context mContext = null;
    private RecipeListFragment parentFragment;

    public BeerXMLReader(RecipeListFragment parentFragment) {
        this.parentFragment = parentFragment;
        mContext = parentFragment.getActivity();
        dialog = new ProgressDialog(mContext);
    }

    public BeerXMLReader(Context context) {
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage(mContext.getString(R.string.open_recipe_progress));
        dialog.show();
    }

    @Override
    protected Recipe[] doInBackground(InputStream... inputStreams) {
        return readInputStream(inputStreams[0]);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        int current = progress[0] + 1;
        int total = progress[1];
        String message;

        if (total > 1) {
            message = String.format(
                    mContext.getString(R.string.open_recipes_progress),
                    current, total);
        } else {
            message = mContext.getString(R.string.open_recipe_progress);
        }
        if (dialog != null) {
            dialog.setMessage(message);
        } else {
            Log.i(TAG, message);
        }

    }

    @Override
    protected void onPostExecute(final Recipe[] recipes) {
        parentFragment.addRecipes(recipes);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private Recipe[] readInputStream(InputStream inputStream) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            Log.e(TAG, "Couldn't create DocumentBuilderFactory", e1);
            return null;

        }

        Document recipeDocument = null;
        XPath xp = null;
        try {
            recipeDocument = dBuilder.parse(inputStream);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't read XML File", e);
            return null;
        }

        return readDocument(recipeDocument);
    }

    public Recipe[] readFile(File beerXMLFile) {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            Log.e(TAG, "Couldn't create DocumentBuilderFactory", e1);
            return null;
        }

        Document recipeDocument = null;
        try {
            recipeDocument = dBuilder.parse(beerXMLFile);
        } catch (Exception e) {
            Log.e(TAG, beerXMLFile + " isn't an XML File");
            return null;
        }

        return readDocument(recipeDocument);
    }

    private Recipe[] readDocument(Document recipeDocument) {
        XPath xp = null;
        try {
            xp = XPathFactory.newInstance().newXPath();
            NodeList recipeList =
                    (NodeList) xp.evaluate(
                            "/RECIPES/RECIPE", recipeDocument, XPathConstants.NODESET);
            if (recipeList.getLength() == 0) {
                Log.i("BrewShop", "No Recipes found in file");
                return null;
            }

            return readRecipe(recipeDocument, null);
        } catch (XPathException xpe) {
            Log.e(TAG, "Couldn't run XPATH", xpe);
            return null;
        }
    }

    private Recipe[] readRecipe(Document beerDocument, String name) throws XPathException {
        String recipeSelector = "";

        if (name != null) {
            recipeSelector = "[NAME[text()=\"" + name + "\"]]";
        }

        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList recipeData =
                (NodeList) xp.evaluate(
                        "/RECIPES/RECIPE" + recipeSelector, beerDocument, XPathConstants.NODESET);

        ArrayList<Recipe> recipeList = new ArrayList<Recipe>();

        for (int i = 0; i < recipeData.getLength(); i++) {
            publishProgress(i, recipeData.getLength());
            try {
                recipeList.add(readSingleRecipe(recipeData.item(i)));
            } catch (XPathException xpe) {
                Log.e(TAG, "Couldn't read the recipe at index " + i, xpe);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Couldn't read the recipe at index " + i + " due to a bad number", nfe);
            }
        }

        return recipeList.toArray(new Recipe[recipeList.size()]);
    }

    private Recipe readSingleRecipe(Node recipeNode) throws XPathException, NumberFormatException {
        XPath xp = XPathFactory.newInstance().newXPath();
        Recipe recipe = new Recipe();

        // otherwise get the details from the recipe
        String recipeName = (String) xp.evaluate("NAME/text()", recipeNode, XPathConstants.STRING);
        String brewerName = (String) xp.evaluate("BREWER/text()", recipeNode, XPathConstants.STRING);
        String notes = (String) xp.evaluate("NOTES/text()", recipeNode, XPathConstants.STRING);

        double efficiency = getDouble(recipeNode, "EFFICIENCY", xp);
        double batchSize = getDouble(recipeNode, "BATCH_SIZE", xp);
        double boilSize = getDouble(recipeNode, "BOIL_SIZE", xp);
        double boilTime = getDouble(recipeNode, "BOIL_TIME", xp);

        recipe.setName(recipeName);
        recipe.setBrewerName(brewerName);
        recipe.setNotes(notes);
        recipe.setBatchVolume(Quantity.convertUnit("litres", "gallons US", batchSize));
        recipe.setBoilVolume(Quantity.convertUnit("litres", "gallons US", boilSize));
        recipe.setBoilTime(boilTime);
        recipe.setEfficiency(efficiency);

        NodeList hopsList = (NodeList) xp.evaluate("HOPS", recipeNode, XPathConstants.NODESET);
        parseHops(recipe, hopsList);
        NodeList maltList = (NodeList) xp.evaluate("FERMENTABLES", recipeNode, XPathConstants.NODESET);
        parseMalts(recipe, maltList);
        NodeList yeastList = (NodeList) xp.evaluate("YEASTS", recipeNode, XPathConstants.NODESET);
        parseYeasts(recipe, yeastList);
        Node styleList = (Node) xp.evaluate("STYLE", recipeNode, XPathConstants.NODE);
        parseStyle(recipe, styleList);

        return recipe;
    }

    private void parseHops(Recipe recipe, NodeList hops) throws XPathException, NumberFormatException {
        if (hops == null || hops.getLength() == 0) {
            return;
        }
        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList hopList = (NodeList) xp.evaluate("HOP", hops.item(0), XPathConstants.NODESET);

        for (int i = 0; i < hopList.getLength(); i++) {
            Node hop = hopList.item(i);

            // Get the values
            String name = (String) xp.evaluate("NAME", hop, XPathConstants.STRING);
            String temp = (String) xp.evaluate("AMOUNT", hop, XPathConstants.STRING);
            double amount = Double.parseDouble(temp);
            temp = (String) xp.evaluate("ALPHA", hop, XPathConstants.STRING);
            double alpha = Double.parseDouble(temp);

            temp = (String) xp.evaluate("TIME", hop, XPathConstants.STRING);
            int time = (int) Math.round(Double.parseDouble(temp));
            String use = (String) xp.evaluate("USE", hop, XPathConstants.STRING);

            Hop hopObject = new Hop();
            hopObject.setName(name);
            hopObject.setPercentAlpha(alpha);

            HopAddition hopAddition = new HopAddition();
            hopAddition.setHop(hopObject);

            hopAddition.setWeight(Weight.fromKg(amount));

            // Not all of these are used by beerxml 1.0, but we can change as and when
            if (use.equalsIgnoreCase("boil")) {
                hopAddition.setUsage(HopUsage.BOIL);
                hopAddition.setBoilTime(time);
            } else if (use.equalsIgnoreCase("dry hop")) {
                hopAddition.setUsage(HopUsage.DRY_HOP);
                int days = (time / 60) / 24;
                hopAddition.setDryHopDays(days);
            } else if (use.equalsIgnoreCase("mash")) {
                hopAddition.setUsage(HopUsage.MASH);
                hopAddition.setBoilTime(time);
            } else if (use.equalsIgnoreCase("first wort")) {
                hopAddition.setUsage(HopUsage.FIRST_WORT);
                hopAddition.setBoilTime(time);
            } else if (use.equalsIgnoreCase("aroma") || use.equalsIgnoreCase("whirlpool")) {
                hopAddition.setUsage(HopUsage.WHIRLPOOL);
                hopAddition.setBoilTime(time);
            }

            // Everything is OK here, so add it in.
            recipe.addHop(hopAddition);
        }
    }

    private void parseMalts(Recipe recipe, NodeList malts) throws XPathException, NumberFormatException {
        if (malts == null || malts.getLength() == 0) {
            return;
        }

        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList fermentableList = (NodeList) xp.evaluate("FERMENTABLE", malts.item(0), XPathConstants.NODESET);

        for (int i = 0; i < fermentableList.getLength(); i++) {
            try {
                Node fermentable = fermentableList.item(i);

                // Get the values
                String name = (String) xp.evaluate("NAME", fermentable, XPathConstants.STRING);
                String type = (String) xp.evaluate("TYPE", fermentable, XPathConstants.STRING);
                type = type.toLowerCase();
                boolean mashed = type.contains("malt") || type.contains("grain");

                double amount = getDouble(fermentable, "AMOUNT", xp);
                double color = getDouble(fermentable, "COLOR", xp);
                double yield = getDouble(fermentable, "YIELD", xp);


                Malt malt = new Malt();
                malt.setName(name);
                malt.setGravity(1 + yield * .01 * (BeerXMLCommon.SUCROSE_GRAVITY - 1));
                malt.setColor(color);
                malt.setMashed(mashed);

                MaltAddition maltAddition = new MaltAddition();
                maltAddition.setWeight(Weight.fromKg(amount));
                maltAddition.setMalt(malt);

                recipe.addFermentable(maltAddition);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Couldn't parse a number", nfe);
            } catch (Exception e) {
                if (e instanceof XPathException) {
                    throw (XPathException) e;
                } else {
                    Log.e(TAG, "Couldn't read the weight for a malt", e);
                }
            }
        }
    }

    private void parseYeasts(Recipe recipe, NodeList yeasts) throws XPathException, NumberFormatException {
        if (yeasts == null || yeasts.getLength() == 0) {
            return;
        }

        XPath xp = XPathFactory.newInstance().newXPath();
        NodeList yeastList = (NodeList) xp.evaluate("YEAST", yeasts.item(0), XPathConstants.NODESET);

        for (int i = 0; i < yeastList.getLength(); i++) {
            try {
                Node yeastItem = yeastList.item(i);

                String name = (String) xp.evaluate("NAME", yeastItem, XPathConstants.STRING);
                String type = (String) xp.evaluate("TYPE", yeastItem, XPathConstants.STRING);
                String form = (String) xp.evaluate("FORM", yeastItem, XPathConstants.STRING);
                String attenuation = (String) xp.evaluate("ATTENUATION", yeastItem, XPathConstants.STRING);

                Yeast yeast = new Yeast();
                yeast.setName(name);
                yeast.setAttenuation(Double.parseDouble(attenuation));
                recipe.addYeast(yeast);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Couldn't parse a number", nfe);
            } catch (Exception e) {
                if (e instanceof XPathException) {
                    throw (XPathException) e;
                } else {
                    Log.e("BrewShop", "Couldn't read the weight for a malt", e);
                }
            }
        }
    }

    private void parseStyle(Recipe recipe, Node style) throws XPathExpressionException {
        if (style == null) {
            return;
        }

        XPath xp = XPathFactory.newInstance().newXPath();

        String name = (String) xp.evaluate("NAME", style, XPathConstants.STRING);
        String notes = (String) xp.evaluate("NOTES", style, XPathConstants.STRING);
        int categoryNumber = getInteger(style, "CATEGORY_NUMBER", xp);
        String styleLetter = (String) xp.evaluate("STYLE_LETTER", style, XPathConstants.STRING);
        String styleGuide = (String) xp.evaluate("STYLE_GUIDE", style, XPathConstants.STRING);
        String type = (String) xp.evaluate("TYPE", style, XPathConstants.STRING);

        double ogMin = getDouble(style, "OG_MIN", xp);
        double ogMax = getDouble(style, "OG_MAX", xp);
        double fgMin = getDouble(style, "FG_MIN", xp);
        double fgMax = getDouble(style, "FG_MAX", xp);
        double ibuMin = getDouble(style, "IBU_MIN", xp);
        double ibuMax = getDouble(style, "IBU_MAX", xp);
        double colorMin = getDouble(style, "COLOR_MIN", xp);
        double colorMax = getDouble(style, "COLOR_MAX", xp);
        double abvMin = getDouble(style, "ABV_MIN", xp);
        double abvMax = getDouble(style, "ABV_MAX", xp);

        // Check to see if we have this style
        BjcpCategoryList mBjcpCategoryList = new BjcpCategoryStorage(mContext, styleGuide).getStyles();
        BjcpCategory bjcpCategory = mBjcpCategoryList.findByName(name);

        if (bjcpCategory == null && name.contains("&amp")) {
            bjcpCategory = mBjcpCategoryList.findByName(name.replace("&amp", "and"));
        }

        if (bjcpCategory == null) {
            return;
        }

        BjcpSubcategory bjcpSubcategory = bjcpCategory.findSubcategoryByLetter(styleLetter);

        VitalStatistics vitalStatistics = null;
        if (bjcpCategory.getSubcategories() == null || bjcpCategory.getSubcategories().isEmpty()) {
            vitalStatistics = bjcpCategory.getGuidelines().getVitalStatistics();
        } else if (bjcpSubcategory != null) {
            vitalStatistics = bjcpSubcategory.getGuidelines().getVitalStatistics();
        }

        BeerStyle beerStyle = new BeerStyle();
        beerStyle.setStyleName(bjcpCategory.getName());
        if (bjcpSubcategory != null) {
            beerStyle.setSubstyleName(bjcpSubcategory.getName());
        }
        beerStyle.setStyleGuide(styleGuide);
        beerStyle.setDescription(notes);
        beerStyle.setType(type);

        if (vitalStatistics == null) {

            if (abvMax == abvMin || abvMax == 0.0 || abvMin == 0.0) {
                abvMax = (76.08 * (ogMax - fgMin) / (1.775 - ogMax)) * (fgMin / 0.794);
                abvMin = (76.08 * (ogMin - fgMax) / (1.775 - ogMin)) * (fgMax / 0.794);
            }

            beerStyle.setAbvMax(abvMax);
            beerStyle.setAbvMin(abvMin);
            beerStyle.setFgMax(fgMax);
            beerStyle.setFgMin(fgMin);
            beerStyle.setOgMax(ogMax);
            beerStyle.setOgMin(ogMin);
            beerStyle.setSrmMax(colorMax);
            beerStyle.setSrmMin(colorMin);
            beerStyle.setIbuMax(ibuMax);
            beerStyle.setIbuMin(ibuMin);
        } else {
            beerStyle.setOgMin(vitalStatistics.getOgMin());
            beerStyle.setOgMax(vitalStatistics.getOgMax());
            beerStyle.setFgMin(vitalStatistics.getFgMin());
            beerStyle.setFgMax(vitalStatistics.getFgMax());
            beerStyle.setIbuMin(vitalStatistics.getIbuMin());
            beerStyle.setIbuMax(vitalStatistics.getIbuMax());
            beerStyle.setSrmMin(vitalStatistics.getSrmMin());
            beerStyle.setSrmMax(vitalStatistics.getSrmMax());
            beerStyle.setAbvMin(vitalStatistics.getAbvMin());
            beerStyle.setAbvMax(vitalStatistics.getAbvMax());
        }
        recipe.setStyle(beerStyle);
    }

    private double getDouble(NodeList element, String name, XPath xp) {
        try {
            String temp = (String) xp.evaluate(name.toUpperCase(), element, XPathConstants.STRING);
            return Double.parseDouble(temp);
        } catch (XPathException xpe) {
            Log.e(TAG, "Failed to run XPATH", xpe);
            return 0.0;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Failed to parse string", nfe);
            return 0.0;
        }
    }

    private double getDouble(Node element, String name, XPath xp) {
        try {
            String temp = (String) xp.evaluate(name.toUpperCase(), element, XPathConstants.STRING);
            return Double.parseDouble(temp);
        } catch (XPathException xpe) {
            Log.e(TAG, "Failed to run XPATH", xpe);
            return 0.0;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Failed to parse string", nfe);
            return 0.0;
        }
    }

    private int getInteger(Node element, String name, XPath xp) {
        try {
            String temp = (String) xp.evaluate(name.toUpperCase(), element, XPathConstants.STRING);
            return Integer.parseInt(temp);
        } catch (XPathException xpe) {
            Log.e(TAG, "Failed to run XPATH", xpe);
            return 0;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "Failed to parse string", nfe);
            return 0;
        }
    }
}
