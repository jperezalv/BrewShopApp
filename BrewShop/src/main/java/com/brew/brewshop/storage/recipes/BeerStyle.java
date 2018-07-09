package com.brew.brewshop.storage.recipes;

import android.os.Parcel;
import android.os.Parcelable;

import com.brew.brewshop.R;

@SuppressWarnings("unused")
public class BeerStyle implements Parcelable {
    public static final String BJCP_STYLE = "BJCP";
    public static final String BJCP_2015_STYLE = "BJCP2015";

    private String style;
    private String substyle;
    private String description;
    private String type;
    private String styleNumber;
    private String styleLetter;
    private String styleGuide;
    private double ogMin, ogMax;
    private double fgMin, fgMax;
    private double ibuMin, ibuMax;
    private double srmMin, srmMax;
    private double abvMin, abvMax;

    public BeerStyle() {
        style = "";
        substyle = "";
        description = "";
        type = "";
        styleNumber = "";
        styleLetter = "";
        styleGuide = BJCP_2015_STYLE;
    }

    public BeerStyle(Parcel parcel) {
        style = parcel.readString();
        substyle = parcel.readString();
        description = parcel.readString();
        ogMin = parcel.readDouble();
        ogMax = parcel.readDouble();
        fgMin = parcel.readDouble();
        fgMax = parcel.readDouble();
        ibuMin = parcel.readDouble();
        ibuMax = parcel.readDouble();
        srmMin = parcel.readDouble();
        srmMax = parcel.readDouble();
        abvMin = parcel.readDouble();
        abvMax = parcel.readDouble();
        styleNumber = parcel.readString();
        styleLetter = parcel.readString();
        styleGuide = parcel.readString();
        type = parcel.readString();
    }

    public String getDisplayName() {
        if (substyle == null || substyle.length() == 0) {
            return style;
        } else {
            return substyle;
        }
    }

    public String getStyleName() {
        return style;
    }

    public void setStyleName(String value) { style = value; }

    public String getSubstyleName() {
        return substyle;
    }

    public void setSubstyleName(String value) { substyle = value; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) { description = value; }

    public double getIbuMin() {
        return ibuMin;
    }

    public void setIbuMin(double value) { ibuMin = value; }

    public double getIbuMax() {
        return ibuMax;
    }

    public void setIbuMax(double value) { ibuMax = value; }

    public double getSrmMin() {
        return srmMin;
    }

    public void setSrmMin(double value) { srmMin = value; }

    public double getSrmMax() {
        return srmMax;
    }

    public void setSrmMax(double value) { srmMax = value; }

    public double getAbvMin() {
        return abvMin;
    }

    public void setAbvMin(double value) { abvMin = value; }

    public double getAbvMax() {
        return abvMax;
    }

    public void setAbvMax(double value) { abvMax = value; }

    public double getOgMin() {
        return ogMin;
    }

    public void setOgMin(double value) { ogMin = value; }

    public double getOgMax() {
        return ogMax;
    }

    public void setOgMax(double value) { ogMax = value; }

    public double getFgMin() {
        return fgMin;
    }

    public void setFgMin(double value) { fgMin = value; }

    public double getFgMax() {
        return fgMax;
    }

    public void setFgMax(double value) { fgMax = value; }

    public String getStyleGuide() { return styleGuide; }

    public void setStyleGuide(String value) { styleGuide = value; }

    public String getType() { return type; }

    public void setType(String value) { type = value; }

    public int getIconDrawable() {
        if (substyle.startsWith("Oktoberfest")) {
            return R.drawable.das_boot_cap;
        } else if (style.startsWith("German Wheat")) {
            return R.drawable.weizen_cap;
        } else if (style.startsWith("American Ale") || substyle.startsWith("American IPA")) {
            return R.drawable.american_cap;
        } else if (style.startsWith("Belgian Strong Ale")) {
            return R.drawable.goblet_cap;
        } else if (style.startsWith("Pilsner") || style.startsWith("Bock") || style.toLowerCase().contains("lager")) {
            return R.drawable.pilsner_cap;
        } else if (style.startsWith("Sour Ale") || style.startsWith("Fruit Beer") || style.startsWith("Belgian and French Ale")) {
            return R.drawable.tulip_cap;
        }
        return R.drawable.nonic_cap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(style);
        parcel.writeString(substyle);
        parcel.writeString(description);
        parcel.writeDouble(ogMin);
        parcel.writeDouble(ogMax);
        parcel.writeDouble(fgMin);
        parcel.writeDouble(fgMax);
        parcel.writeDouble(ibuMin);
        parcel.writeDouble(ibuMax);
        parcel.writeDouble(srmMin);
        parcel.writeDouble(srmMax);
        parcel.writeDouble(abvMin);
        parcel.writeDouble(abvMax);
        parcel.writeString(styleNumber);
        parcel.writeString(styleLetter);
        parcel.writeString(styleGuide);
        parcel.writeString(type);
    }

    public static final Parcelable.Creator<BeerStyle> CREATOR = new Parcelable.Creator<BeerStyle>() {
        public BeerStyle createFromParcel(Parcel in) {
            return new BeerStyle(in);
        }
        public BeerStyle[] newArray(int size) {
            return new BeerStyle[size];
        }
    };

    public boolean equals(BeerStyle other) {
        return style.equals(other.getStyleName()) && substyle.equals(other.getSubstyleName())
                && description.equals(other.getDescription())
                && ogMin == other.getOgMin() && ogMax == other.getOgMax()
                && fgMin == other.getFgMin() && fgMax == other.getFgMax()
                && ibuMin == other.getIbuMin() && ibuMax == other.getIbuMax()
                && srmMin == other.getSrmMin() && srmMax == other.getSrmMax()
                && abvMin == other.getAbvMin() && abvMax == other.getAbvMax();
    }
}
