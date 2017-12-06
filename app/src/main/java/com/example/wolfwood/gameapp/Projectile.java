package com.example.wolfwood.gameapp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by Wolfwood on 12/4/2017.
 */

public class Projectile {
    private RectF rect;
    private float width;
    private float height;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public Projectile(float screenX, float screenY, float _x, float _y, float _dx, float _dy){
        x=_x;
        y=_y;
        width=screenX/100;
        height=screenY/50;
        dx=_dx;
        dy=_dy;
        rect = new RectF();
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
    public RectF rect(){
        return rect;
    }
    public void update(){
        x+=dx;
        y+=dy;
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
    public void draw(Paint p, Canvas c){
        c.drawRect(rect,p);
    }
}
