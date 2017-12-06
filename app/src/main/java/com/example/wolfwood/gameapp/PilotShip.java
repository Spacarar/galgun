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

public class PilotShip implements Ship {
    private RectF rect;
    private long lastFired;
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
        lastFired=0;

    }

    @Override
    public void update(){
        x += dx;
        y += dy;
        rect.left=x;
        rect.right=x+width;
        rect.top=y;
        rect.bottom=y+height;
    }

    @Override
    public void drawShip(Canvas c, Paint p){
        //draw ship
        p.setColor(Color.argb(255,230,15,180));
        c.drawRect(rect,p);
    }

    @Override
    public void setVelocity(float _dx,float _dy){
        dx=_dx;
        dy=_dy;
    }
    public void setPosition(float _x, float _y){
        x=_x;
        y=_y;
    }

    public float width(){
        return width;
    }
    public float height(){
        return height;
    }
    public void shoot(){
        //Log.d("SHOOT", "shoot: was called");
        lastFired=System.currentTimeMillis()+500;
    }
    public boolean canFire(){
        if(System.currentTimeMillis()>=lastFired) {
           //Log.d("canFire is true", "current"+System.currentTimeMillis()+" vs estimated: "+lastFired);
            return true;
        }
        return false;
    }
    public float middleX(){
        return x+width/2;
    }
    public float topY(){
        return y;
    }
}
