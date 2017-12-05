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
    private boolean isDead;
    private float lastFired;
    private RectF rect;
    private float width;
    private float height;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public EnemyShip(int scrX, int scrY) {
        width = ((int) (scrX * .2));
        height = (int) (scrY * .2);
        x = scrX / 2 - width / 2;
        y = height * 2;
        rect = new RectF(x, y, x + width, y + height);
        lastFired=System.currentTimeMillis();

    }
    public EnemyShip(int scrX, int scrY,int _x, int _y){
        width=(int)(scrX*.05);
        height=(int)(scrY*.05);
        x=_x;
        y=_y;
        rect = new RectF(x,y,x+width,y+height);
        lastFired=System.currentTimeMillis();
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }

    @Override
    public void drawShip(Canvas c, Paint p) {
        //draw ship
        if(!isDead) {
            p.setColor(Color.argb(255, 230, 150, 180));
            c.drawRect(rect, p);
        }
    }

    @Override
    public void setVelocity(float _dx, float _dy) {
        dx = _dx;
        dy = _dy;
    }
    public void setPosition(int _x, int _y){
        x=_x;
        y=_y;
    }
    public float width() {
        return width;
    }
    public float height(){
        return height;
    }
    public void kill(){
        isDead=true;
    }
    public boolean canFire(){
        if(lastFired+1000<System.currentTimeMillis())return true;
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
}