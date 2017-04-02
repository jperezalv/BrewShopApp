package com.brew.brewshop.storage.style;

import android.content.Context;

import com.brew.brewshop.R;
import com.brew.brewshop.storage.JsonReader;
import com.brew.brewshop.storage.NameableList;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BjcpCategoryStorage {
    private static Map<String, NameableList> sStyleCache;

    private final Context mContext;
    private final String mGuideline;

    public BjcpCategoryStorage(Context context, String guideline) {
        mContext = context;
        mGuideline = guideline;
        sStyleCache = new HashMap<>(2);
    }

    public BjcpCategoryList getStyles() {
        if (!sStyleCache.containsKey(mGuideline)) {
            try {
                JsonReader reader = new JsonReader(mContext, BjcpCategoryList.class);
                sStyleCache.put(mGuideline, reader.readAll(
                        mContext.getResources().getIdentifier(
                                mGuideline.toLowerCase(Locale.US),
                                "raw",
                                mContext.getPackageName())
                        , "beers"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (BjcpCategoryList)sStyleCache.get(mGuideline);
    }
}
