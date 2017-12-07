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
    //ships simplified space
    private RectF rect;

    //ships cut out detail from simplified space
    private RectF leftWing;
    private RectF shipBody;
    private RectF cockpit;
    private RectF rightWing;

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
        leftWing = new RectF(x,(float)(y+height*.3),(float)(x+width*.3),y+height);
        shipBody = new RectF((float)(x+width*.3),y,(float)(x+width*.7),(float)(y+height*.8));
        cockpit = new RectF((float)(x+width*.45),y,(float)(x+width*.55),(float)(y+height*.3));
        rightWing = new RectF((float)(x+width*.7),(float)(y+height*.3),(float)(x+width),y+height);
        lastFired=0;

    }

    @Override
    public void update(){
        x += dx;
        y += dy;
        rect.set(x,y,x+width,y+height);
        leftWing.set(x,(float)(y+height*.3),(float)(x+width*.3),y+height);
        shipBody.set((float)(x+width*.3),y,(float)(x+width*.7),(float)(y+height*.7));
        cockpit.set((float)(x+width*.4),y,(float)(x+width*.6),(float)(y+height*.3));
        rightWing.set((float)(x+width*.7),(float)(y+height*.3),(float)(x+width),y+height);
    }

    @Override
    public void drawShip(Canvas c, Paint p){
        //draw ship
        p.setColor(Color.argb(255,220,220,120));
        c.drawRect(leftWing,p);
        c.drawRect(shipBody,p);
        c.drawRect(rightWing,p);
        p.setColor(Color.argb(255,100,100,100));
        c.drawRect(cockpit,p);

    }

    @Override
    public void setVelocity(float _dx,float _dy){
        dx=_dx;
        dy=_dy;
    }
    @Override
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
        lastFired=System.currentTimeMillis()+300;
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
    public RectF rect(){
        return rect;
    }
}
