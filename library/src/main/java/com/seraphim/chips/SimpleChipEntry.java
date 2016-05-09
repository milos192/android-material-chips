package com.seraphim.chips;

import android.net.Uri;
import android.support.annotation.Nullable;

public class SimpleChipEntry implements ChipEntry {
    private int id = 0;
    private String name;
    private Uri imageUri;

    public SimpleChipEntry(String name, @Nullable Uri imageUri) {
        this.name = name;
        this.imageUri = imageUri;

        id += name.hashCode();
        if (imageUri != null) id += imageUri.hashCode();
    }

    @Override
    public String displayedName() {
        return name;
    }

    @Override
    public Uri avatarUri() {
        return imageUri;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SimpleChipEntry && ((SimpleChipEntry) o).id == id;
    }
}