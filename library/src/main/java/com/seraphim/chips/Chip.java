package com.seraphim.chips;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.ImageViewTarget;

public class Chip<E extends ChipEntry> implements View.OnClickListener {

    private static final int MAX_LABEL_LENGTH = 30;
    static final int ACTION_DELETE = 0;
    static final int ACTION_OTHER = 1;

    public static final int UNDEFINED_CUSTOM_COLOR = 0;

    ChipsView mChipsView;
    String mLabel;
    final Uri mPhotoUri;
    final E mEntry;
    final boolean mIsIndelible;

    RelativeLayout mView;
    View mIconWrapper;
    TextView mTextView;

    ImageView mAvatarView;
    ImageView mPersonIcon;
    ImageView mCloseIcon;
    ImageView mErrorIcon;

    private boolean mIsSelected;

    private int mCustomChipColor;

    public Chip(final ChipsView chipsView, E entry) {
        this(chipsView, entry, false);
    }

    public Chip(final ChipsView chipsView, E entry, boolean isIndelible) {
        mChipsView = chipsView;
        mLabel = entry.getDisplayName();
        mPhotoUri = entry.getAvatarUri();
        mEntry = entry;
        mIsIndelible = isIndelible;

        if (mLabel == null) {
            mLabel = entry.getDisplayName();
        }

        if (mLabel.length() > MAX_LABEL_LENGTH) {
            mLabel = mLabel.substring(0, MAX_LABEL_LENGTH) + "...";
        }
    }

    public View getView() {
        if (mView == null) {
            mView = (RelativeLayout) View.inflate(mChipsView.getContext(), R.layout.chips_view, null);
            mView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    (int) (ChipsView.CHIP_HEIGHT * mChipsView.density)));
            mAvatarView = (ImageView) mView.findViewById(R.id.ri_ch_avatar);
            mIconWrapper = mView.findViewById(R.id.rl_ch_avatar);
            mTextView = (TextView) mView.findViewById(R.id.tv_ch_name);
            mPersonIcon = (ImageView) mView.findViewById(R.id.iv_ch_person);
            mCloseIcon = (ImageView) mView.findViewById(R.id.iv_ch_close);

            mErrorIcon = (ImageView) mView.findViewById(R.id.iv_ch_error);

            // set inital res & attrs
            mView.setBackgroundResource(mChipsView.chipsBgRes);
            mView.post(new Runnable() {
                @Override
                public void run() {
                    mView.getBackground().setColorFilter(mChipsView.chipsBgColor, PorterDuff.Mode.SRC_ATOP);
                }
            });
            mIconWrapper.setBackgroundResource(R.drawable.amc_circle);
            mTextView.setTextColor(mCustomChipColor == UNDEFINED_CUSTOM_COLOR ? mChipsView.chipsTextColor : mCustomChipColor);

            // set icon resources
            mPersonIcon.setBackgroundResource(mChipsView.chipsPlaceholderResId);
            mCloseIcon.setBackgroundResource(mChipsView.chipsDeleteResId);

            mCloseIcon.setOnClickListener(this);
            mView.setOnClickListener(this);
            mIconWrapper.setOnClickListener(this);
        }
        updateViews();
        return mView;
    }

    private void updateViews() {
        mTextView.setText(mLabel);
        if (mEntry.getPreloadedBitmap() != null) {
            mAvatarView.setImageBitmap(mEntry.getPreloadedBitmap());
            mPersonIcon.setVisibility(View.INVISIBLE);
        } else if (mPhotoUri != null) {
            Glide.with(mChipsView.getContext())
                 .load(mPhotoUri)
                 .asBitmap()
                 .transform(new CenterCrop(mChipsView.getContext()))
                 .into(new ImageViewTarget<Bitmap>(mAvatarView) {
                     @Override
                     protected void setResource(Bitmap resource) {
                         mAvatarView.setImageBitmap(resource);
                         mPersonIcon.setVisibility(View.INVISIBLE);
                     }
                 });
        }
        if (isSelected() && mChipsView.mAllowDeletions) {
            if (mChipsView.chipsValidator != null && !mChipsView.chipsValidator.isValid(mEntry)) {
                // not valid & show error
                mView.getBackground().setColorFilter(mChipsView.chipsBgColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                mTextView.setTextColor(mChipsView.chipsTextColorErrorClicked);
                mIconWrapper.getBackground().setColorFilter(mChipsView.chipsColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                mErrorIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                mView.getBackground().setColorFilter(mChipsView.chipsBgColorClicked, PorterDuff.Mode.SRC_ATOP);
                mTextView.setTextColor(mChipsView.chipsTextColorClicked);
                mIconWrapper.getBackground().setColorFilter(mChipsView.chipsColorClicked, PorterDuff.Mode.SRC_ATOP);
            }
            mPersonIcon.animate().alpha(0.0f).setDuration(200).start();
            mAvatarView.animate().alpha(0.0f).setDuration(200).start();
            mCloseIcon.animate().alpha(1f).setDuration(200).setStartDelay(100).start();

        } else {
            if (mChipsView.chipsValidator != null && !mChipsView.chipsValidator.isValid(mEntry)) {
                // not valid & show error
                mErrorIcon.setVisibility(View.VISIBLE);
                mErrorIcon.setColorFilter(null);
            } else {
                mErrorIcon.setVisibility(View.GONE);
            }
            mView.getBackground().setColorFilter(mChipsView.chipsBgColor, PorterDuff.Mode.SRC_ATOP);
            mTextView.setTextColor(mCustomChipColor == UNDEFINED_CUSTOM_COLOR ? mChipsView.chipsTextColor : mCustomChipColor);
            mIconWrapper.getBackground().setColorFilter(mChipsView.chipsColor, PorterDuff.Mode.SRC_ATOP);

            mPersonIcon.animate().alpha(0.3f).setDuration(200).setStartDelay(100).start();
            mAvatarView.animate().alpha(1f).setDuration(200).setStartDelay(100).start();
            mCloseIcon.animate().alpha(0.0f).setDuration(200).start();
        }
    }

    @Override
    public void onClick(View v) {
        mChipsView.onChipInteraction(this, translateIdToConst(v));
    }

    private int translateIdToConst(View v) {
        if (v.getId() == R.id.iv_ch_close) {
            return ACTION_DELETE;
        } else {
            return ACTION_OTHER;
        }
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        if (mIsIndelible) {
            return;
        }
        mIsSelected = isSelected;
    }

    public E getEntry() {
        return mEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (mEntry != null && o instanceof ChipEntry) {
            return mEntry.equals(o);
        }
        return super.equals(o);
    }

    /**
     * Define a custom text color for this chip only.
     *
     * @param color A color integer, or {@link #UNDEFINED_CUSTOM_COLOR}, if you want the chip to use the color that the {@link ChipsView} has defined for
     * all chips.
     */
    public void setCustomTextColor(int color) {
        mCustomChipColor = color;
        if (mTextView != null) {
            // Probably not the best solution, but calling updateViews() just for this seems expensive
            mTextView.setTextColor(color);
        }
    }

    /**
     * Retrieves the integer color value of this chip.
     *
     * @return The current custom color of this chip; {@link #UNDEFINED_CUSTOM_COLOR} means that this chip uses the value from the {@link ChipsView}
     * control.
     */
    public int getCustomTextColor() {
        return mCustomChipColor;
    }

    @Override
    public String toString() {
        return "{" + "[Entry: " + mEntry + "]" + "}";
    }
}
