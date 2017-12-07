package com.example.wolfwood.gameapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Wolfwood on 12/1/2017.
 */

public class EnemyShip implements Ship {
    private RectF rect;

    private RectF leftWing;
    private RectF shipBody;
    private RectF rightWing;
    private RectF stripe;

    private boolean isDead;
    private long lastFired;


    private float width;
    private float height;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public EnemyShip(int scrX, int scrY,int _x, int _y){
        width=(int)(scrX*.055);
        height=(int)(scrY*.07);
        x=_x;
        y=_y;
        rect = new RectF(x,y,x+width,y+height);
        leftWing = new RectF(x,y,(float)(x+width*.3),(float)(y+height*.7));
        shipBody = new RectF((float)(x+width*.3),(float)(y+height*.3),(float)(x+width*.7),y+height);
        rightWing = new RectF((float)(x+width*.7),y,x+width,(float)(y+height*.7));
        stripe = new RectF((float)(x+width*.45),y,(float)(x+width*.55),y+height);
        lastFired=System.currentTimeMillis();
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        rect.set(x, y, x + width, y + height);
        leftWing.set(x,y,(float)(x+width*.3),(float)(y+height*.7));
        shipBody.set((float)(x+width*.3),(float)(y+height*.3),(float)(x+width*.7),y+height);
        rightWing.set((float)(x+width*.7),y,x+width,(float)(y+height*.7));
        stripe.set((float)(x+width*.45),y,(float)(x+width*.55),y+height);

    }

    @Override
    public void drawShip(Canvas c, Paint p) {
        //draw ship
        if(!isDead) {
            p.setColor(Color.argb(255, 89, 89, 89));
            c.drawRect(leftWing, p);
            c.drawRect(shipBody, p);
            c.drawRect(rightWing, p);
            p.setColor(Color.argb(255,200,50,50));
            c.drawRect(stripe,p);


        }
    }

    @Override
    public void setVelocity(float _dx, float _dy) {
        dx = _dx;
        dy = _dy;
    }
    @Override
    public void setPosition(float _x, float _y){
        x=_x;
        y=_y;
    }

    public void kill(){
        isDead=true;
    }
    public void revive(){isDead=false;}
    public boolean canFire(int level){
        if(lastFired+5000/level<System.currentTimeMillis())return true;
        return false;
    }
    public void shoot(){
        lastFired=System.currentTimeMillis();
    }
    public float left(){
        return x;
    }
    public float right(){
        return x+width;
    }
    public boolean wasHit(RectF _rect){
        return rect.intersects(rect,_rect);
    }
    public void nudgeLeft(){
        x-=1;
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
    public void nudgeRight(){
        x+=1;
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
    public boolean isDead(){
        return isDead;
    }
    public float middleX(){return x+width/2;}

    public float bottomY(){return rect.bottom;}
}