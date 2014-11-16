package com.brew.brewshop.storage.inventory;

import android.os.Parcel;
import android.os.Parcelable;

import com.brew.brewshop.storage.Storeable;
import com.brew.brewshop.storage.recipes.Hop;
import com.brew.brewshop.storage.recipes.Ingredient;
import com.brew.brewshop.storage.recipes.Malt;
import com.brew.brewshop.storage.recipes.Weight;
import com.brew.brewshop.storage.recipes.Yeast;

public class InventoryItem implements Storeable {
    private int id;
    private Malt malt;
    private Hop hop;
    private Yeast yeast;
    private Weight quantity;

    @SuppressWarnings("unused")
    public InventoryItem() { }

    public InventoryItem(Ingredient item) {
        this.id = -1;
        this.quantity = new Weight(0, 0);
        setIngredient(item);
    }

    public InventoryItem(Parcel parcel) {
        id = parcel.readInt();
        Ingredient item =  parcel.readParcelable(null);
        setIngredient(item);
        quantity = parcel.readParcelable(Weight.class.getClassLoader());
    }

    @Override
    public void setId(int value) {
        id = value;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(getIngredient(), 0);
        parcel.writeParcelable(quantity, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setIngredient(Ingredient item) {
        if (item instanceof Malt) {
            malt = (Malt) item;
        } else if (item instanceof Hop) {
            hop = (Hop) item;
        } else if (item instanceof Yeast) {
            yeast = (Yeast) item;
        }
    }

    public Ingredient getIngredient() {
        if (malt != null) {
            return malt;
        } else if (hop != null) {
            return hop;
        } else if (yeast != null) {
            return yeast;
        }
        return null;
    }

    public Hop getHop() {
        return hop;
    }

    public Malt getMalt() {
        return malt;
    }

    public Yeast getYeast() {
        return yeast;
    }

    public void setQuantity(Weight value) { quantity = value; }
    public Weight getQuantity() { return quantity; }

    public static final Parcelable.Creator<InventoryItem> CREATOR = new Parcelable.Creator<InventoryItem>() {
        public InventoryItem createFromParcel(Parcel in) {
            return new InventoryItem(in);
        }
        public InventoryItem[] newArray(int size) {
            return new InventoryItem[size];
        }
    };
}
