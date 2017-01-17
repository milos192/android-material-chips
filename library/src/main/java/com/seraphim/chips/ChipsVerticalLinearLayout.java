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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

class ChipsVerticalLinearLayout<E extends ChipEntry> extends LinearLayout {
    private List<LinearLayout> mLinearLayouts = new ArrayList<>();
    private float mDensity;
    private int mRowSpacing;

    public ChipsVerticalLinearLayout(Context context, int rowSpacing) {
        super(context);

        mDensity = getResources().getDisplayMetrics().density;
        mRowSpacing = rowSpacing;

        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }

    public TextLineParams onChipsChanged(List<ChipsView<E>.Chip<E>> chips) {
        clearChipsViews();

        int width = getWidth();
        if (width == 0) {
            return null;
        }
        int widthSum = 0;
        int rowCounter = 0;

        LinearLayout linearLayout = createHorizontalView();

        for (ChipsView.Chip chip : chips) {
            View view = chip.getView();
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            // if width exceed current width. create a new LinearLayout
            if (widthSum + view.getMeasuredWidth() > width) {
                rowCounter++;
                widthSum = 0;
                linearLayout = createHorizontalView();
            }

            widthSum += view.getMeasuredWidth();
            linearLayout.addView(view);
        }

        // check if there is enough space left
        if (width - widthSum < width * 0.1f) {
            widthSum = 0;
            rowCounter++;
        }
        widthSum += Math.round(linearLayout.getChildCount() * (float) 8 * mDensity);
        return new TextLineParams(rowCounter, widthSum);
    }

    private LinearLayout createHorizontalView() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setPadding(0, 0, 0, mRowSpacing);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setDividerDrawable(ContextCompat.getDrawable(getContext(), R.drawable.amc_empty_vertical_divider));
        linearLayout.setShowDividers(SHOW_DIVIDER_MIDDLE);
        addView(linearLayout);
        mLinearLayouts.add(linearLayout);
        return linearLayout;
    }

    private void clearChipsViews() {
        for (LinearLayout linearLayout : mLinearLayouts) {
            linearLayout.removeAllViews();
        }
        mLinearLayouts.clear();
        removeAllViews();
    }

    public static class TextLineParams {
        public int row;
        public int lineMargin;

        public TextLineParams(int row, int lineMargin) {
            this.row = row;
            this.lineMargin = lineMargin;
        }
    }
}
