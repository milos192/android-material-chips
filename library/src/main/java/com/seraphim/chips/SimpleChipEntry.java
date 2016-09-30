package com.seraphim.chips;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SimpleChipEntry implements ChipEntry {
    private String mName;
    @Nullable
    private Uri mImageUri;

    public SimpleChipEntry(@NonNull String name) {
        this(name, null);
    }

    public SimpleChipEntry(@NonNull String name, @Nullable String imageUrl) {
        this.mName = name;
        if (imageUrl != null) {
            mImageUri = Uri.parse(imageUrl);
        }
    }

    @Override
    @NonNull
    public String getDisplayName() {
        return mName;
    }

    @Override
    @Nullable
    public Uri getAvatarUri() {
        return mImageUri;
    }

    @Nullable
    @Override
    public Bitmap getPreloadedBitmap() {
        return null;
    }
}
