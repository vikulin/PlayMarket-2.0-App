package com.blockchain.store.playmarket.utilities.drawable;

import android.content.Context;
import android.graphics.Canvas;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;

import com.blockchain.store.playmarket.R;

/**
 * Created by samsheff on 21/09/2017.
 */

public class HamburgerDrawable extends DrawerArrowDrawable {

    public HamburgerDrawable(Context context){
        super(context);
        setColor(context.getResources().getColor(R.color.drawer_toggle_color));
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

//        setBarLength(85.0f);
//        setBarThickness(12.0f);
//        setGapSize(15.0f);

    }
}
