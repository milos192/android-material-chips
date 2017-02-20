package com.seraphim.chips;

import android.view.KeyEvent;
import android.widget.TextView;

public interface ChipsListener<E extends ChipEntry> {
    void onChipAdded(Chip<E> chip);

    void onChipDeleted(Chip<E> chip);

    void onTextChanged(CharSequence text);

    boolean onEditorAction(TextView v, int actionId, KeyEvent event);
}
