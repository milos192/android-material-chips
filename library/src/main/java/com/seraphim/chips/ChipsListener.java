package com.seraphim.chips;

public interface ChipsListener<E extends ChipEntry> {
    void onChipAdded(Chip<E> chip);

    void onChipDeleted(Chip<E> chip);

    void onTextChanged(CharSequence text);
}
