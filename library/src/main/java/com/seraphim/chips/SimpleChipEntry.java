package com.seraphim.chips;

import android.net.Uri;
import android.support.annotation.Nullable;

public class SimpleChipEntry implements ChipEntry {
    private String name;
    private Uri imageUri;

    public SimpleChipEntry(String name) {
        this.name = name;
    }

    public SimpleChipEntry(String name, @Nullable String imageUrl) {
        this.name = name;
        imageUri = Uri.parse(imageUrl);
    }

    @Override
    public String displayedName() {
        return name;
    }

    @Override
    public Uri avatarUri() {
        return imageUri;
    }
}