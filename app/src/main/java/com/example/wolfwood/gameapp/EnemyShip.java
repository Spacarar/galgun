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
    private RectF rect;
    private float width;
    private float height;
    private float x;
    private float y;
    private float dx;
    private float dy;

    public EnemyShip(int scrX, int scrY) {
        width = ((int) (scrX * .10));
        height = (int) (scrY * .10);
        x = scrX / 2 - width / 2;
        y = scrY - height * 2;
        rect = new RectF(x, y, x + width, y + height);

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
}