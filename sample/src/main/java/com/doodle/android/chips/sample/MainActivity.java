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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seraphim.chips.ChipsView;

public class MainActivity extends AppCompatActivity {
    ChipsView chipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chipsView = (ChipsView) findViewById(R.id.cv_contacts);

        // change EditText config
        chipsView.getEditText().setCursorVisible(true);

        /*List<ChipsView.ChipEntry> chipEntries = new ArrayList<>();
        chipEntries.add(new SimpleChipEntry("example 1", null));
        chipEntries.add(new SimpleChipEntry("example 2", Uri.parse("http://www.topofandroid.com/wp-content/uploads/2015/05/Android-L-Material-Design-Wallpapers-5.png")));
        chipEntries.add(new SimpleChipEntry("example 3", Uri.parse("https://appcyla.files.wordpress.com/2015/02/m1.jpg")));
        chipEntries.add(new SimpleChipEntry("example 4", Uri.parse("http://geekhounds.com/wp-content/uploads/2014/11/unnamed.jpg")));
        chipEntries.add(new SimpleChipEntry("example 5", null));
        chipEntries.add(new SimpleChipEntry("example 6", Uri.parse("http://lifehacker.ru/wp-content/uploads/2014/11/14-14.png")));
        chipEntries.add(new SimpleChipEntry("example 7", null));
        chipsView.setupWithRecyclerView((RecyclerView) findViewById(R.id.rv_contacts), chipEntries);*/
    }

    /*public class SimpleChipEntry implements ChipsView.ChipEntry {
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
    }*/
}