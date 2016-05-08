package com.seraphim.chips;

public interface ChipsListener {
    void onChipAdded(ChipsView.Chip chip);

    void onChipDeleted(ChipsView.Chip chip);

    void onTextChanged(CharSequence text);
}