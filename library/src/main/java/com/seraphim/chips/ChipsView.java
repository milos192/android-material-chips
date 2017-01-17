package com.seraphim.chips;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChipsView<E extends ChipEntry> extends ScrollView implements ChipsEditText.InputConnectionWrapperInterface,
        ChipsEditText.ItemClickListener, TextView.OnEditorActionListener {

    // <editor-fold desc="Static Fields">
    private static final String TAG = "ChipsView";
    static final int CHIP_HEIGHT = 32; // dp
    private static final int SPACING_TOP = 4; // dp
    private static final int SPACING_BOTTOM = 4; // dp
    private static final int DEFAULT_MAX_HEIGHT = -1;
    public static final int DEFAULT_VERTICAL_SPACING = 1; // dp
    // </editor-fold>

    // <editor-fold desc="Resources">
    int chipsBgRes = R.drawable.amc_chip_background;
    // </editor-fold>

    // <editor-fold desc="Attributes">
    private int maxHeight; // px
    private int verticalSpacing;

    int chipsColor;
    int chipsColorClicked;
    int chipsColorErrorClicked;
    int chipsBgColor;
    int chipsBgColorClicked;
    int chipsBgColorErrorClicked;
    int chipsTextColor;
    int chipsTextColorClicked;
    int chipsTextColorErrorClicked;
    int chipsPlaceholderResId;
    int chipsDeleteResId;
    // </editor-fold>

    // <editor-fold desc="Private Fields">
    float density;
    private RelativeLayout chipsContainer;
    private ChipsListener<E> chipsListener;
    private ChipsEditText editText;
    private ChipsVerticalLinearLayout rootChipsLayout;
    private EditTextListener editTextListener;
    private List<Chip<E>> chipList = new ArrayList<>();
    private Object currentEditTextSpan;
    ChipValidator chipsValidator;
    private Mode mode = Mode.ALL;
    private ChipsFactory factory;
    boolean mAllowDeletions = true;
    private InputMethodManager mInputMethodManager;
    private String mHint = "";
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

    //<editor-fold desc="View overrides">
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
    //</editor-fold>

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

        // TODO: Allow dev to choose whether he wants this or not
        // Dummy item to prevent AutoCompleteTextView from receiving focus
        LinearLayout linearLayout = new LinearLayout(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        linearLayout.setLayoutParams(params);
        linearLayout.setFocusable(true);
        linearLayout.setFocusableInTouchMode(true);

        chipsContainer.addView(linearLayout);

        editText = new ChipsEditText(getContext(), this, this);
        RelativeLayout.LayoutParams editTextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextParams.topMargin = (int) (SPACING_TOP * density);
        editTextParams.bottomMargin = (int) (SPACING_BOTTOM * density) + verticalSpacing;
        editText.setLayoutParams(editTextParams);
        editText.setMinHeight((int) (CHIP_HEIGHT * density));
        editText.setPaddings(0, 0, 0, 0);
        // Seems to be causing an issue on an old Nexus 7, while not causing issues anywhere when commented out
        // editText.setLineSpacing(verticalSpacing, (CHIP_HEIGHT * density) / editText.getLineHeight());
        editText.setBackgroundColor(Color.argb(0, 0, 0, 0));
        editText.setHideUnderline(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setOnEditorActionListener(this);

        chipsContainer.addView(editText);

        rootChipsLayout = new ChipsVerticalLinearLayout(getContext(), verticalSpacing);
        rootChipsLayout.setOrientation(LinearLayout.VERTICAL);
        rootChipsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        chipsContainer.addView(rootChipsLayout);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rootChipsLayout.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

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
        Chip chip = new Chip(this, entry, isIndelible);
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
    public List<Chip<E>> getChips() {
        return Collections.unmodifiableList(chipList);
    }

    public boolean removeChipBy(ChipEntry entry) {
        for (int i = 0; i < chipList.size(); i++) {
            if (chipList.get(i).mEntry != null && chipList.get(i).mEntry.equals(entry)) {
                Chip chip = chipList.remove(i);
                if (chipsListener != null) {
                    chipsListener.onChipDeleted(chip);
                }
                onChipsChanged(true);
                return true;
            }
        }
        return false;
    }

    public boolean removeChipBy(E entry, EqualityFunction<E> equalityFunction) {
        for (int i = 0; i < chipList.size(); i++) {
            if (chipList.get(i).mEntry != null && equalityFunction.equal(chipList.get(i).mEntry, entry)) {
                Chip chip = chipList.remove(i);
                if (chipsListener != null) {
                    chipsListener.onChipDeleted(chip);
                }
                onChipsChanged(true);
                return true;
            }
        }
        return false;
    }

    public void setChipsListener(ChipsListener<E> chipsListener) {
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

    public void setText(@NonNull String text) {
        editText.setText(text);
    }

    public void setHint(@NonNull String hint) {
        mHint = hint;
        if (chipList.isEmpty()) {
            editText.setHint(hint);
        }
    }

    @Nullable
    public String getText() {
        return editText.getText().toString();
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

        if (chipList.isEmpty()) {
            editText.setHint(mHint);
        } else {
            editText.setHint("");
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

    void onChipInteraction(final Chip chip, int mode) {
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

    public interface EqualityFunction<E> {
        boolean equal(E e1, E e2);
    }
    // </editor-fold>
}
