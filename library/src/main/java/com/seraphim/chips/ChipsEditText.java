/*
 * Copyright (C) 2016 Doodle AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seraphim.chips;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

class ChipsEditText extends MaterialAutoCompleteTextView {
    private InputConnectionWrapperInterface inputConnectionWrapperInterface;
    private ItemClickListener itemClickListener;
    private ChipsAdapter adapter;

    public ChipsEditText(Context context, InputConnectionWrapperInterface inputConnectionWrapperInterface, final ItemClickListener itemClickListener) {
        super(context);
        this.inputConnectionWrapperInterface = inputConnectionWrapperInterface;
        this.itemClickListener = itemClickListener;
        adapter = new ChipsAdapter();
        setAdapter(adapter);
        setShowClearButton(true);
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ChipsView.ChipEntry entry = (ChipsView.ChipEntry) parent.getItemAtPosition(position);
                    setText("");
                    itemClickListener.clicked(entry);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addSuggestions(List<ChipsView.ChipEntry> entries) {
        adapter.addSuggestions(entries);
    }

    public void setSuggestions(List<ChipsView.ChipEntry> entries) {
        adapter.setSuggestions(entries);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (inputConnectionWrapperInterface != null) {
            return inputConnectionWrapperInterface.getInputConnection(super.onCreateInputConnection(outAttrs));
        }

        return super.onCreateInputConnection(outAttrs);
    }

    public interface InputConnectionWrapperInterface {
        InputConnection getInputConnection(InputConnection target);
    }

    public interface ItemClickListener {
        void clicked(ChipsView.ChipEntry entry);
    }

    private class ChipsAdapter extends BaseAdapter implements Filterable {
        private List<ChipsView.ChipEntry> chipEntries;

        public ChipsAdapter() {
        }

        public ChipsAdapter(List<ChipsView.ChipEntry> chipEntries) {
            this.chipEntries = chipEntries;
        }

        public void addSuggestions(List<ChipsView.ChipEntry> entries) {
            if (chipEntries == null) chipEntries = new ArrayList<>();
            chipEntries.addAll(entries);
        }

        public void setSuggestions(List<ChipsView.ChipEntry> entries) {
            chipEntries = new ArrayList<>(entries);
        }

        @Override
        public int getCount() {
            return chipEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return chipEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_list_item_with_avatar_1, parent, false);
            }
            ChipsView.ChipEntry chipEntry = chipEntries.get(position);
            Context context = convertView.getContext();
            if (chipEntry.avatarUri() != null) {
                Glide.with(context)
                        .load(chipEntry.avatarUri())
                        .asBitmap()
                        .transform(new CenterCrop(context))
                        .placeholder(R.color.paper)
                        .into((ImageView) convertView.findViewById(R.id.preview));
            }
            ((TextView) convertView.findViewById(R.id.primary_text)).setText(chipEntry.displayedName());
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                private List<ChipsView.ChipEntry> originalEntries;

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if (originalEntries == null) originalEntries = new ArrayList<>(chipEntries);

                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || constraint.length() == 0) {
                        filterResults.values = originalEntries;
                        filterResults.count = originalEntries.size();
                        originalEntries = null;
                        return filterResults;
                    } else {
                        List<ChipsView.ChipEntry> entries = new ArrayList<>();
                        for (ChipsView.ChipEntry entry : originalEntries) {
                            if (entry.displayedName().contains(constraint))
                                entries.add(entry);
                        }
                        filterResults.values = entries;
                        filterResults.count = entries.size();
                        return filterResults;
                    }
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        chipEntries = (List<ChipsView.ChipEntry>) results.values;
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }
}