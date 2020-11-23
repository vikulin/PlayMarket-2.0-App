package com.blockchain.store.playmarket.views;

import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class FonAwesomeTextViewSolid extends AppCompatTextView {

    private Context context;

    public FonAwesomeTextViewSolid(Context context) {
        super(context);
        this.context = context;
        setUp();
    }

    public FonAwesomeTextViewSolid(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setUp();
    }

    public FonAwesomeTextViewSolid(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setUp();
    }

    private void setUp() {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/font_awesome_solid.ttf");
        setTypeface(typeface);

    }
}
