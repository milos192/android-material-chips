/*
 * Copyright (C) 2016 Doodle.
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

package com.doodle.android.chips.sample;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.seraphim.chips.ChipsView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView contacts;
    private ContactsAdapter adapter;
    private ChipsView chipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contacts = (RecyclerView) findViewById(R.id.rv_contacts);
        contacts.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter = new ContactsAdapter();
        contacts.setAdapter(adapter);

        chipsView = (ChipsView) findViewById(R.id.cv_contacts);

        // change EditText config
        chipsView.getEditText().setCursorVisible(true);

        chipsView.setChipsValidator(new ChipsView.ChipValidator() {
            @Override
            public boolean isValid(ChipsView.ChipEntry entry) {
                return !entry.displayedName().equals("asd@qwe.de");
            }
        });

        chipsView.setChipsListener(new ChipsView.ChipsListener() {
            @Override
            public void onChipAdded(ChipsView.Chip chip) {
                for (ChipsView.Chip chipItem : chipsView.getChips()) {
                    Log.d("ChipList", "chip: " + chipItem.toString());
                }
            }

            @Override
            public void onChipDeleted(ChipsView.Chip chip) {

            }

            @Override
            public void onTextChanged(CharSequence text) {
                adapter.filterItems(text);
            }
        });
    }

    public class ContactsAdapter extends RecyclerView.Adapter<CheckableContactViewHolder> {

        private String[] data = new String[]{
                "john@doe.com",
                "at@doodle.com",
                "asd@qwe.de",
                "verylongaddress@verylongserver.com",
                "thisIsMyEmail@address.com",
                "test@testeration.de",
                "short@short.com"
        };

        private List<String> filteredList = new ArrayList<>();

        public ContactsAdapter() {
            Collections.addAll(filteredList, data);
        }

        @Override
        public CheckableContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_checkable_contact, parent, false);
            return new CheckableContactViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(CheckableContactViewHolder holder, int position) {
            holder.name.setText(filteredList.get(position));
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public void filterItems(CharSequence text) {
            filteredList.clear();
            if (TextUtils.isEmpty(text)) {
                Collections.addAll(filteredList, data);
            } else {
                for (String s : data) {
                    if (s.contains(text)) {
                        filteredList.add(s);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return Math.abs(filteredList.get(position).hashCode());
        }
    }

    public class CheckableContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView name;
        public final CheckBox selection;

        public CheckableContactViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_contact_name);
            selection = (CheckBox) itemView.findViewById(R.id.cb_contact_selection);
            selection.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selection.performClick();
                }
            });
        }

        @Override
        public void onClick(View v) {
            String email = name.getText().toString();
            Uri imgUrl = Math.random() > .7d ? null : Uri.parse("https://robohash.org/" + Math.abs(email.hashCode()));
            SimpleChipEntry chipEntry = new SimpleChipEntry(email, imgUrl);

            if (selection.isChecked()) {
                chipsView.addChip(chipEntry);
            } else {
                chipsView.removeChipBy(chipEntry);
            }
        }
    }

    public class SimpleChipEntry implements ChipsView.ChipEntry {
        private int id = 0;
        private String email;
        private Uri imageUri;

        public SimpleChipEntry(String email, @Nullable Uri imageUri) {
            this.email = email;
            this.imageUri = imageUri;

            id += email.hashCode();
            if (imageUri != null) id += imageUri.hashCode();
        }

        @Override
        public String displayedName() {
            return email;
        }

        @Override
        public Uri avatarUri() {
            return imageUri;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SimpleChipEntry && ((SimpleChipEntry) o).id == id;
        }
    }
}
