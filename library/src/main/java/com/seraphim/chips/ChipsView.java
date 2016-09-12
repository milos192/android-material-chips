package com.seraphim.chips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChipsView extends ScrollView implements ChipsEditText.InputConnectionWrapperInterface,
                                                     ChipsEditText.ItemClickListener,
                                                     TextView.OnEditorActionListener {

    // <editor-fold desc="Static Fields">
    private static final String TAG = "ChipsView";
    private static final int CHIP_HEIGHT = 32; // dp
    private static final int SPACING_TOP = 4; // dp
    private static final int SPACING_BOTTOM = 4; // dp
    private static final int DEFAULT_MAX_HEIGHT = -1;
    public static final int DEFAULT_VERTICAL_SPACING = 1; // dp
    // </editor-fold>

    // <editor-fold desc="Resources">
    private int chipsBgRes = R.drawable.amc_chip_background;
    // </editor-fold>

    // <editor-fold desc="Attributes">
    private int maxHeight; // px
    private int verticalSpacing;

    private int chipsColor;
    private int chipsColorClicked;
    private int chipsColorErrorClicked;
    private int chipsBgColor;
    private int chipsBgColorClicked;
    private int chipsBgColorErrorClicked;
    private int chipsTextColor;
    private int chipsTextColorClicked;
    private int chipsTextColorErrorClicked;
    private int chipsPlaceholderResId;
    private int chipsDeleteResId;
    // </editor-fold>

    // <editor-fold desc="Private Fields">
    private float density;
    private RelativeLayout chipsContainer;
    private ChipsListener chipsListener;
    private ChipsEditText editText;
    private ChipsVerticalLinearLayout rootChipsLayout;
    private EditTextListener editTextListener;
    private List<Chip> chipList = new ArrayList<>();
    private Object currentEditTextSpan;
    private ChipValidator chipsValidator;
    private Mode mode = Mode.ALL;
    private ChipsFactory factory;
    private boolean mAllowDeletions = true;
    private InputMethodManager mInputMethodManager;
    // </editor-fold>

    // <editor-fold desc="Constructors">
    public ChipsView(Context context) {
        super(context);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChipsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
        init();
    }
    // </editor-fold>

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight != DEFAULT_MAX_HEIGHT) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return true;
    }

    // <editor-fold desc="Initialization">
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ChipsView,
                0, 0);
        try {
            maxHeight = a.getDimensionPixelSize(R.styleable.ChipsView_cv_max_height, DEFAULT_MAX_HEIGHT);
            verticalSpacing = a.getDimensionPixelSize(R.styleable.ChipsView_cv_vertical_spacing,
                                                      (int) (DEFAULT_VERTICAL_SPACING * density));
            chipsColor = a.getColor(R.styleable.ChipsView_cv_color,
                                    ContextCompat.getColor(context, R.color.base30));
            chipsColorClicked = a.getColor(R.styleable.ChipsView_cv_color_clicked,
                                           ContextCompat.getColor(context, R.color.colorPrimaryDark));
            chipsColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_color_error_clicked,
                                                ContextCompat.getColor(context, R.color.color_error));

            chipsBgColor = a.getColor(R.styleable.ChipsView_cv_bg_color,
                                      ContextCompat.getColor(context, R.color.base10));
            chipsBgColorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                                             ContextCompat.getColor(context, R.color.blue));

            chipsBgColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_bg_color_clicked,
                                                  ContextCompat.getColor(context, R.color.color_error));

            chipsTextColor = a.getColor(R.styleable.ChipsView_cv_text_color,
                                        Color.BLACK);
            chipsTextColorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                                               Color.WHITE);
            chipsTextColorErrorClicked = a.getColor(R.styleable.ChipsView_cv_text_color_clicked,
                                                    Color.WHITE);

            chipsPlaceholderResId = a.getResourceId(R.styleable.ChipsView_cv_icon_placeholder,
                                                    R.drawable.ic_person_24dp);
            chipsDeleteResId = a.getResourceId(R.styleable.ChipsView_cv_icon_delete,
                                               R.drawable.ic_close_24dp);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        chipsContainer = new RelativeLayout(getContext());
        addView(chipsContainer);

        // Dummy item to prevent AutoCompleteTextView from receiving focus
        LinearLayout linearLayout = new LinearLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        linearLayout.setLayoutParams(params);
        linearLayout.setFocusable(true);
        linearLayout.setFocusableInTouchMode(true);

        chipsContainer.addView(linearLayout);

        editText = new ChipsEditText(getContext(), this, this);
        RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                                                                                             .MATCH_PARENT,
                                                                                     ViewGroup.LayoutParams
                                                                                             .WRAP_CONTENT);
        editTextParams.topMargin = (int) (SPACING_TOP * density);
        editTextParams.bottomMargin = (int) (SPACING_BOTTOM * density) + verticalSpacing;
        editText.setLayoutParams(editTextParams);
        editText.setMinHeight((int) (CHIP_HEIGHT * density));
        editText.setPaddings(0, 0, 0, 0);
        editText.setLineSpacing(verticalSpacing, (CHIP_HEIGHT * density) / editText.getLineHeight());
        editText.setBackgroundColor(Color.argb(0, 0, 0, 0));
        editText.setHideUnderline(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setOnEditorActionListener(this);

        chipsContainer.addView(editText);

        rootChipsLayout = new ChipsVerticalLinearLayout(getContext(), verticalSpacing);
        rootChipsLayout.setOrientation(LinearLayout.VERTICAL);
        rootChipsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.WRAP_CONTENT));
        rootChipsLayout.setPadding(0, (int) (SPACING_TOP * density), 0, 0);
        chipsContainer.addView(rootChipsLayout);

        factory = new DefaultFactory();

        mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        initListener();
    }

    private void initListener() {
        chipsContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                unselectAllChips();
            }
        });

        editTextListener = new EditTextListener();
        editText.addTextChangedListener(editTextListener);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    unselectAllChips();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold desc="Public Methods">
    public void addChip(ChipEntry entry) {
        addChip(entry, false);
    }

    public void addChip(ChipEntry entry, boolean isIndelible) {
        Chip chip = new Chip(entry, isIndelible);
        chipList.add(chip);
        // TODO: Investigate whether it would be better to call onChipAdded after it has been drawn
        if (chipsListener != null) {
            chipsListener.onChipAdded(chip);
        }

        onChipsChanged(true);
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @NonNull
    public List<Chip> getChips() {
        return Collections.unmodifiableList(chipList);
    }

    public boolean removeChipBy(ChipEntry entry) {
        for (int i = 0; i < chipList.size(); i++) {
            if (chipList.get(i).mEntry != null && chipList.get(i).mEntry.equals(entry)) {
                chipList.remove(i);
                onChipsChanged(true);
                return true;
            }
        }
        return false;
    }

    public void setChipsListener(ChipsListener chipsListener) {
        this.chipsListener = chipsListener;
    }

    public void setChipsValidator(ChipValidator chipsValidator) {
        this.chipsValidator = chipsValidator;
    }

    public void addSuggestions(List<ChipEntry> entries) {
        editText.addSuggestions(entries);
    }

    public void setSuggestions(List<ChipEntry> entries) {
        editText.setSuggestions(entries);
    }

    public void setChipsFilter(ChipsEntriesFilter chipsEntriesFilter) {
        editText.setChipsFilter(chipsEntriesFilter);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setAllowDeletions(boolean allow) {
        mAllowDeletions = allow;
    }

    public void setFactory(ChipsFactory factory) {
        this.factory = factory;
    }

    // </editor-fold>

    // <editor-fold desc="Private Methods">

    /**
     * rebuild all chips and place them right
     */
    private void onChipsChanged(final boolean moveCursor) {
        ChipsVerticalLinearLayout.TextLineParams textLineParams = rootChipsLayout.onChipsChanged(chipList);

        // if null then run another layout pass
        if (textLineParams == null) {
            post(new Runnable() {
                @Override
                public void run() {
                    onChipsChanged(moveCursor);
                }
            });
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText.getLayoutParams();
        params.topMargin = (int) ((SPACING_TOP + textLineParams.row * CHIP_HEIGHT) * density) + textLineParams.row *
                verticalSpacing;
        editText.setLayoutParams(params);
        addLeadingMarginSpan(textLineParams.lineMargin);
        if (moveCursor) {
            editText.setSelection(editText.length());
        }
    }

    private void addLeadingMarginSpan(int margin) {
        Editable text = editText.getText();
        if (currentEditTextSpan != null) {
            text.removeSpan(currentEditTextSpan);
        }
        currentEditTextSpan = new android.text.style.LeadingMarginSpan.LeadingMarginSpan2.Standard(margin + Math
                .round(4 * density), 0);
        text.setSpan(currentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void addLeadingMarginSpan() {
        Editable text = editText.getText();
        if (currentEditTextSpan != null) {
            text.removeSpan(currentEditTextSpan);
        }
        text.setSpan(currentEditTextSpan, 0, 0, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void selectOrDeleteLastChip() {
        if (chipList.size() > 0) {
            onChipInteraction(chipList.size() - 1);
        }
    }

    private void onChipInteraction(int position) {
        try {
            Chip chip = chipList.get(position);
            if (chip != null) {
                onChipInteraction(chip, Chip.ACTION_DELETE);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Out of bounds", e);
        }
    }

    private void onChipInteraction(final Chip chip, int mode) {
        unselectChipsExcept(chip);
        if (chip.isSelected() && mode == Chip.ACTION_DELETE) {
            chipList.remove(chip);
            if (chipsListener != null) {
                chipsListener.onChipDeleted(chip);
            }
            onChipsChanged(true);
        } else {
            chip.setSelected(true);
            // TODO: Maybe implement as an option
            //            postDelayed(new Runnable() {
            //                @Override
            //                public void run() {
            //                    if (chipList.contains(chip)) {
            //                        chip.setSelected(false);
            //                        onChipsChanged(false);
            //                    }
            //                }
            //            }, 2000);
            onChipsChanged(false);
        }
    }

    private void unselectChipsExcept(Chip rootChip) {
        for (Chip chip : chipList) {
            if (chip != rootChip) {
                chip.setSelected(false);
            }
        }
        onChipsChanged(false);
    }

    private void unselectAllChips() {
        unselectChipsExcept(null);
    }
    // </editor-fold>

    // <editor-fold desc="Listeners Implementations">
    @Override
    public InputConnection getInputConnection(InputConnection target) {
        return new KeyInterceptingInputConnection(target);
    }

    @Override
    public void clicked(ChipEntry entry) {
        addChip(entry);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (mode == Mode.ALL && !editText.getText().toString().isEmpty() && actionId == EditorInfo.IME_ACTION_DONE) {
            ChipEntry entry = factory.createChip(editText.getText().toString());
            editText.setText("");
            addChip(entry);
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold desc="Inner Classes / Interfaces">
    private class EditTextListener implements TextWatcher {
        private boolean mIsPasteTextChange = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 1) {
                mIsPasteTextChange = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mIsPasteTextChange) {
                mIsPasteTextChange = false;
                // todo handle copy/paste text here

            } else {
                // no paste text change
                if (s.toString().contains("\n")) {
                    String text = s.toString();
                    text = text.replace("\n", "");
                    while (text.contains("  ")) {
                        text = text.replace("  ", " ");
                    }
                    s.clear();
                    if (text.length() <= 1) {
                        s.append(text);
                    }
                }
            }
            if (chipsListener != null) {
                chipsListener.onTextChanged(s);
            }
        }
    }

    private class KeyInterceptingInputConnection extends InputConnectionWrapper {

        public KeyInterceptingInputConnection(InputConnection target) {
            super(target, true);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.d("Key", "action: " + event.getAction() + ", code: " + event.getKeyCode());
            if (editText.length() == 0) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    selectOrDeleteLastChip();
                    return true;
                }
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                Log.d(TAG, "Enter: " + editText.getText().toString());
                editText.append("\n");
                return true;
            }

            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (editText.length() == 0 && beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    public class Chip implements OnClickListener {

        private static final int MAX_LABEL_LENGTH = 30;
        private static final int ACTION_DELETE = 0;
        private static final int ACTION_OTHER = 1;

        public static final int UNDEFINED_CUSTOM_COLOR = 0;

        private String mLabel;
        private final Uri mPhotoUri;
        private final ChipEntry mEntry;
        private final boolean mIsIndelible;

        private RelativeLayout mView;
        private View mIconWrapper;
        private TextView mTextView;

        private ImageView mAvatarView;
        private ImageView mPersonIcon;
        private ImageView mCloseIcon;
        private ImageView mErrorIcon;

        private boolean mIsSelected;

        private int mCustomChipColor;

        public Chip(ChipEntry entry) {
            this(entry, false);
        }

        public Chip(ChipEntry entry, boolean isIndelible) {
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
                mView = (RelativeLayout) inflate(getContext(), R.layout.chips_view, null);
                mView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                      (int) (CHIP_HEIGHT * density)));
                mAvatarView = (ImageView) mView.findViewById(R.id.ri_ch_avatar);
                mIconWrapper = mView.findViewById(R.id.rl_ch_avatar);
                mTextView = (TextView) mView.findViewById(R.id.tv_ch_name);
                mPersonIcon = (ImageView) mView.findViewById(R.id.iv_ch_person);
                mCloseIcon = (ImageView) mView.findViewById(R.id.iv_ch_close);

                mErrorIcon = (ImageView) mView.findViewById(R.id.iv_ch_error);

                // set inital res & attrs
                mView.setBackgroundResource(chipsBgRes);
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.getBackground().setColorFilter(chipsBgColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                mIconWrapper.setBackgroundResource(R.drawable.amc_circle);
                mTextView.setTextColor(mCustomChipColor == UNDEFINED_CUSTOM_COLOR ? chipsTextColor : mCustomChipColor);

                // set icon resources
                mPersonIcon.setBackgroundResource(chipsPlaceholderResId);
                mCloseIcon.setBackgroundResource(chipsDeleteResId);

                mCloseIcon.setOnClickListener(this);
                mView.setOnClickListener(this);
                mIconWrapper.setOnClickListener(this);
            }
            updateViews();
            return mView;
        }

        private void updateViews() {
            mTextView.setText(mLabel);
            if (mPhotoUri != null) {
                Glide.with(getContext())
                     .load(mPhotoUri)
                     .asBitmap()
                     .transform(new CenterCrop(getContext()))
                     .into(new ImageViewTarget<Bitmap>(mAvatarView) {
                         @Override
                         protected void setResource(Bitmap resource) {
                             mAvatarView.setImageBitmap(resource);
                             mPersonIcon.setVisibility(INVISIBLE);
                         }
                     });
            }
            if (isSelected() && mAllowDeletions) {
                if (chipsValidator != null && !chipsValidator.isValid(mEntry)) {
                    // not valid & show error
                    mView.getBackground().setColorFilter(chipsBgColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    mTextView.setTextColor(chipsTextColorErrorClicked);
                    mIconWrapper.getBackground().setColorFilter(chipsColorErrorClicked, PorterDuff.Mode.SRC_ATOP);
                    mErrorIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                } else {
                    mView.getBackground().setColorFilter(chipsBgColorClicked, PorterDuff.Mode.SRC_ATOP);
                    mTextView.setTextColor(chipsTextColorClicked);
                    mIconWrapper.getBackground().setColorFilter(chipsColorClicked, PorterDuff.Mode.SRC_ATOP);
                }
                mPersonIcon.animate().alpha(0.0f).setDuration(200).start();
                mAvatarView.animate().alpha(0.0f).setDuration(200).start();
                mCloseIcon.animate().alpha(1f).setDuration(200).setStartDelay(100).start();

            } else {
                if (chipsValidator != null && !chipsValidator.isValid(mEntry)) {
                    // not valid & show error
                    mErrorIcon.setVisibility(View.VISIBLE);
                    mErrorIcon.setColorFilter(null);
                } else {
                    mErrorIcon.setVisibility(View.GONE);
                }
                mView.getBackground().setColorFilter(chipsBgColor, PorterDuff.Mode.SRC_ATOP);
                mTextView.setTextColor(mCustomChipColor == UNDEFINED_CUSTOM_COLOR ? chipsTextColor : mCustomChipColor);
                mIconWrapper.getBackground().setColorFilter(chipsColor, PorterDuff.Mode.SRC_ATOP);

                mPersonIcon.animate().alpha(0.3f).setDuration(200).setStartDelay(100).start();
                mAvatarView.animate().alpha(1f).setDuration(200).setStartDelay(100).start();
                mCloseIcon.animate().alpha(0.0f).setDuration(200).start();
            }
        }

        @Override
        public void onClick(View v) {
            onChipInteraction(this, translateIdToConst(v));
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

        public ChipEntry getEntry() {
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
         * @param color A color integer, or {@link #UNDEFINED_CUSTOM_COLOR}, if you want the chip to use the color that
         *              the {@link ChipsView} has defined for all chips.
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
         * @return The current custom color of this chip; {@link #UNDEFINED_CUSTOM_COLOR} means that this chip uses the
         * value from the {@link ChipsView} control.
         */
        public int getCustomTextColor() {
            return mCustomChipColor;
        }

        @Override
        public String toString() {
            return "{" + "[Entry: " + mEntry + "]" + "}";
        }
    }

    public enum Mode {
        ONLY_SUGGESTIONS,
        ALL
    }

    private class DefaultFactory implements ChipsFactory {
        @Override
        public ChipEntry createChip(String text) {
            return new SimpleChipEntry(text, null);
        }
    }
    // </editor-fold>
}