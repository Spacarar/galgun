package com.example.wolfwood.gameapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import static java.lang.Math.floor;
import static java.lang.Math.toIntExact;

/**
 * Created by Wolfwood on 11/29/2017.
 */

public class PilotShip {
    private RectF rect;
    private float width;
    private float height;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public PilotShip(int scrX, int scrY){
        width = ((int) (scrX*.10));
        height=(int) (scrY*.10);
        x=scrX/2-width/2;
        y=scrY-height*2;
        rect = new RectF(x,y,x+width,y+height);

    }
    public void update(long fps){
        if (fps == 0){
            Log.d("/0 ERROR!","in update pilot ship");
        }
        else {
            x += dx;
            y += dy;
        }
        rect.left=x;
        rect.right=x+width;
        rect.top=y;
        rect.bottom=y+height;
    }
    public void drawShip(Canvas c, Paint p){
        //draw ship
        p.setColor(Color.argb(255,230,15,180));
        c.drawRect(rect,p);
    }
    public void setVelocity(float _dx,float _dy){
        dx=_dx;
        dy=_dy;
    }
    public float left(){
        return rect.left;
    }
    public float right(){
        return rect.right;
    }
    public float top(){
        return rect.top;
    }
    public float bottom(){
        return rect.bottom;
    }
}
