package com.seraphim.chips;


import java.util.List;

public interface ChipsEntriesFilter {
    List<ChipEntry> filter(CharSequence constraint, List<ChipEntry> suggestions);
}