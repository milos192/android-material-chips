package com.seraphim.chips;

import android.net.Uri;
import android.support.annotation.Nullable;

public class SimpleChipEntry implements ChipEntry {
    private String mName;
    @Nullable
    private Uri mImageUri;

    public SimpleChipEntry(String name) {
        this(name, null);
    }

    public SimpleChipEntry(String name, @Nullable String imageUrl) {
        this.mName = name;
        if (imageUrl != null) {
            mImageUri = Uri.parse(imageUrl);
        }
    }

    @Override
    public String getDisplayName() {
        return mName;
    }

    @Override
    @Nullable
    public Uri getAvatarUri() {
        return mImageUri;
    }
}