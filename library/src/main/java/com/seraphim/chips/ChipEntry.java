package com.seraphim.chips;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ChipEntry {
    @NonNull
    String getDisplayName();

    @Nullable
    Uri getAvatarUri();

    @Nullable
    Bitmap getPreloadedBitmap();
}
