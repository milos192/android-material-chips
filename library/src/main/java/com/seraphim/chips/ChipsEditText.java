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
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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

class ChipsEditText extends MaterialAutoCompleteTextView implements AdapterView.OnItemClickListener {

    private InputConnectionWrapperInterface mInputConnectionWrapperInterface;
    private ItemClickListener mItemClickListener;
    private ChipsAdapter mAdapter;
    private ChipsFilter mChipsFilter;

    ChipsEditText(Context context,
            InputConnectionWrapperInterface inputConnectionWrapperInterface,
            final ItemClickListener itemClickListener) {
        super(context);
        mInputConnectionWrapperInterface = inputConnectionWrapperInterface;
        mItemClickListener = itemClickListener;
        mChipsFilter = new ChipsFilter().wrap(new DefaultChipsEntriesFilter());
        mAdapter = new ChipsAdapter();
        setAdapter(mAdapter);
        setOnItemClickListener(this);
        setThreshold(1);
    }

    void addSuggestions(List<ChipEntry> entries) {
        mAdapter.addSuggestions(entries);
    }

    void setSuggestions(List<ChipEntry> entries) {
        mAdapter.setSuggestions(entries);
    }

    void setChipsFilter(ChipsEntriesFilter chipsFilter) {
        mChipsFilter.wrap(chipsFilter);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mInputConnectionWrapperInterface != null) {
            return mInputConnectionWrapperInterface.getInputConnection(super.onCreateInputConnection(outAttrs));
        }

        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            ChipEntry entry = (ChipEntry) parent.getItemAtPosition(position);
            setText("");
            mItemClickListener.clicked(entry);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean enoughToFilter() {
        return getText().length() >= getThreshold();
    }

    /**
     * Implementing classes wrap an incoming {@link InputConnection} with {@link #getInputConnection(InputConnection)}.
     */
    public interface InputConnectionWrapperInterface {

        InputConnection getInputConnection(InputConnection target);
    }

    public interface ItemClickListener {

        void clicked(ChipEntry entry);
    }

    private class ChipsFilter extends Filter {

        private ChipsEntriesFilter mChipsEntriesFilter;

        public ChipsFilter wrap(ChipsEntriesFilter chipsFilter) {
            mChipsEntriesFilter = chipsFilter;
            return this;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<ChipEntry> filteredSuggestions = mChipsEntriesFilter.filter(constraint, mAdapter.getSuggestions());
            filterResults.count = filteredSuggestions.size();
            filterResults.values = filteredSuggestions;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                //noinspection unchecked
                mAdapter.setCurrentEntries((List<ChipEntry>) results.values);
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.notifyDataSetInvalidated();
            }
        }
    }

    private class DefaultChipsEntriesFilter implements ChipsEntriesFilter {

        private String mLastFiltered;

        @Override
        public List<ChipEntry> filter(CharSequence constraint, List<ChipEntry> suggestions) {
            if (constraint != null && !constraint.toString().equals(mLastFiltered) && constraint.length() != 0) {
                List<ChipEntry> entries = new ArrayList<>();
                for (ChipEntry entry : suggestions) {
                    if (entry.getDisplayName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        entries.add(entry);
                    }
                }
                mLastFiltered = constraint.toString();
                return entries;
            } else {
                if (constraint != null) {
                    mLastFiltered = constraint.toString();
                } else {
                    mLastFiltered = null;
                }
                return suggestions;
            }
        }
    }

    private final class ChipsAdapter extends BaseAdapter implements Filterable {

        private final List<ChipEntry> mSuggestions;
        private final List<ChipEntry> mCurrentEntries;

        ChipsAdapter() {
            mSuggestions = new ArrayList<>();
            mCurrentEntries = new ArrayList<>();
        }

        void addSuggestions(List<ChipEntry> entries) {
            mSuggestions.addAll(entries);
            notifyDataSetChanged();
        }

        void setSuggestions(List<ChipEntry> entries) {
            mSuggestions.clear();
            mSuggestions.addAll(entries);
            notifyDataSetChanged();
        }

        void setCurrentEntries(List<ChipEntry> entries) {
            mCurrentEntries.clear();
            mCurrentEntries.addAll(entries);
        }

        @Override
        public int getCount() {
            return mCurrentEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurrentEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.material_list_item_with_avatar_1, parent, false);
            }
            ChipEntry chipEntry = mCurrentEntries.get(position);
            Context context = convertView.getContext();
            ImageView imageView = (ImageView) convertView.findViewById(R.id.preview);
            if (chipEntry.getPreloadedBitmap() != null) {
                imageView.setImageBitmap(chipEntry.getPreloadedBitmap());
            } else if (chipEntry.getAvatarUri() != null) {
                Glide.with(context)
                     .load(chipEntry.getAvatarUri())
                     .asBitmap()
                     .transform(new CenterCrop(context))
                     .placeholder(R.color.paper)
                     .into(imageView);
            } else {
                Drawable drawable = ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_person_24dp);
                drawable.setAlpha(150);
                imageView.setImageDrawable(drawable);
                imageView.setPadding(8, 8, 8, 8);
            }
            ((TextView) convertView.findViewById(R.id.primary_text)).setText(chipEntry.getDisplayName());
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return mChipsFilter;
        }

        List<ChipEntry> getSuggestions() {
            return mSuggestions;
        }
    }
}
