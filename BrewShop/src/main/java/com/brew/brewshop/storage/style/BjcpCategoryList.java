package com.brew.brewshop.storage.style;

import android.text.TextUtils;

import com.brew.brewshop.storage.NameableList;

public class BjcpCategoryList extends NameableList<BjcpCategory> {
    public BjcpCategory findByName(String name) {
        if (!TextUtils.isEmpty(name)) {
            for (BjcpCategory category : this) {
                if (category.getName().equalsIgnoreCase(name)) {
                    return category;
                }
                if (category.findSubcategoryByName(name) != null) {
                    return category;
                }
            }
        }
        return null;
    }

    public BjcpCategory findByNumber(int number) {
        for (BjcpCategory category : this) {
            if (category.getId() == number) {
                return category;
            }
        }
        return null;
    }
}
