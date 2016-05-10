package com.doodle.android.chips.sample;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seraphim.chips.ChipEntry;
import com.seraphim.chips.ChipsView;
import com.seraphim.chips.SimpleChipEntry;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ChipsView chipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chipsView = (ChipsView) findViewById(R.id.cv_contacts);
        chipsView.setMode(ChipsView.Mode.ONLY_SUGGESTIONS);

        final List<ChipEntry> entries = new ArrayList<>();
        entries.add(new SimpleChipEntry("example 1"));
        entries.add(new SimpleChipEntry("example 2", "http://www.topofandroid.com/wp-content/uploads/2015/05/Android-L-Material-Design-Wallpapers-5.png"));
        entries.add(new SimpleChipEntry("example 3", "https://appcyla.files.wordpress.com/2015/02/m1.jpg"));
        entries.add(new SimpleChipEntry("example 4", "http://geekhounds.com/wp-content/uploads/2014/11/unnamed.jpg"));
        entries.add(new SimpleChipEntry("example 5"));
        entries.add(new SimpleChipEntry("example 6", "http://lifehacker.ru/wp-content/uploads/2014/11/14-14.png"));
        entries.add(new SimpleChipEntry("example 7"));
        chipsView.setSuggestions(entries);
    }
}