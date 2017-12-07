package com.example.wolfwood.gameapp;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Wolfwood on 12/1/2017.
 */

public interface Ship {

    public void update();
    public void drawShip(Canvas c, Paint p);
    public void setVelocity(float dx, float dy);
    public void setPosition(float _x, float _y);
}
