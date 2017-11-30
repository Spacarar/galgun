package com.example.wolfwood.gameapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by Wolfwood on 11/29/2017.
 */

public class GameEngine extends SurfaceView implements Runnable {
    //main game loop thread
    private Thread thread = null;
    //context
    private Context context;

    //sounds
    private SoundPool soundPool;
    private int music_1 = -1;

    //FIXME MORE SOUNDS HERE

    //screen size
    private int screenX;
    private int screenY;

    //player information
    private int lives;
    private int score;
    private int currentLevel;

    //button area's
    private RectF leftArrowRect;
    private RectF rightArrowRect;
    private RectF pauseButtonRect;

    //fps control
    private long nextFrameTime;
    private final long FPS = 15;
    private final long MILLIS_PER_SECOND = 1000;
    private volatile boolean isPlaying;

    public enum GAMESTATE {MAINMENU, PLAYING, PAUSED, DEAD}

    ;
    private GAMESTATE gamestate;

    //painting manipulation
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    Random starRandomizer;


    //game objects
    PilotShip pilot;
    //enemies[][]
    //eProj[];
    //pProj[];

    public GameEngine(Context context, Point size) {
        super(context);
        context = context;
        screenX = size.x;
        screenY = size.y;
        lives = 3;
        score = 0;
        gamestate=GAMESTATE.MAINMENU;
        //FIXE ME ADD SOUNDS
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            //add sound using
            descriptor = assetManager.openFd("music1.mp3");
            music_1 = soundPool.load(descriptor, 0);
            //descriptor = assetManager.openFd("get_mouse_sound.ogg");
            //eat_bob = soundPool.load(descriptor, 0);
        } catch (Exception ex) {
            Log.d("Error!", "" + ex);
        }

        surfaceHolder = getHolder();
        paint = new Paint();
        currentLevel = 1;
        pilot = new PilotShip(screenX, screenY);
        //enemies[][]
        //projectiles[]
        starRandomizer = new Random();
        starRandomizer.setSeed(System.currentTimeMillis());
        //When things are ready start game!
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                switch (sampleId) {
                    case 1:
                        Log.d("SoundPool 1","sound is ready, launching");
                        newGame();
                        break;
                }
            }
        });
    }

    @Override
    public void run() {
        if (currentLevel == 1) soundPool.play(music_1, 1, 1, 0, -1, 1);
        while (isPlaying) {
            if (updateRequired()) {
                update();
                drawThings();
            }
        }
       // soundPool.stop(music_1);
       // soundPool.release();
    }
    public void newGame(){
        currentLevel=1;
        lives=3;
        score=0;
        soundPool.play(music_1, 1, 1, 0, -1, 1);
        nextFrameTime=System.currentTimeMillis();
    }

    private boolean updateRequired() {
        // Are we due to update the frame
        if(gamestate==GAMESTATE.MAINMENU){
            if(nextFrameTime<=System.currentTimeMillis()){
                nextFrameTime=System.currentTimeMillis()+MILLIS_PER_SECOND/5;
                return true;
            }
        }
        else {
            if(nextFrameTime <= System.currentTimeMillis()) {
                // Tenth of a second has passed
                // Setup when the next update will be triggered
                nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;
                // Return true so that the update and draw
                // functions are executed
                return true;
            }
        }

        return false;
    }

    private void update() {
        switch(gamestate){
            case MAINMENU:
                break;
            case PLAYING:
                pilot.setVelocity(10, 0);
                pilot.update(FPS);
                break;
            case PAUSED:
                break;
            case DEAD:
                break;
        }

        //update things
    }

    private void drawThings() {
        //draw things!
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.argb(255, 26, 126, 228));
            switch (gamestate) {
                case MAINMENU:
                    canvas.drawColor(Color.argb(255,20,20,30));
                    drawStars(paint,canvas);
                    paint.setTextSize(200);
                    paint.setColor(Color.argb(255,240,200,240));
                    canvas.drawText("GalaGun", screenX / 2 - 420, screenY / 2 - 50, paint);
                    paint.setTextSize(60);
                    canvas.drawText("tap to play!",screenX/2-100,screenY/2+50,paint);
                    break;
                case PLAYING:
                    pilot.drawShip(canvas, paint);
                    break;
                case PAUSED:
                    break;
                case DEAD:
                    break;
                default:
                    Log.d("Switch Error!", "gamestate unknown!");

            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
    public void drawStars(Paint p, Canvas c){

        int radius=4;
        int numStars=50;
        int[] x = new int[numStars];
        int[] y= new int[numStars];
        for(int i=0;i<numStars;i++){
            x[i]=starRandomizer.nextInt(screenX);
            y[i]=starRandomizer.nextInt(screenY);
        }
        paint.setColor(Color.argb(255,255,255,100));
        for(int i=0;i<numStars;i++){
            canvas.drawCircle(x[i],y[i],radius,paint);
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            soundPool.autoPause();
            thread.join();
        } catch (Exception ex) {
            Log.d("Thread Error!", "" + ex);
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
        soundPool.autoResume();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (gamestate) {
            case MAINMENU:
                return HandleMenuEvent(event);
            case PLAYING:
                return HandleGameplayEvent(event);
            case PAUSED:
                return HandlePauseEvent(event);
            case DEAD:
                return HandleDeathEvent(event);
            default:
                Log.d("Switch Error!", "unknown gamestate in event handler");
                return false;
        }
    }

    public boolean HandleMenuEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                gamestate=GAMESTATE.PLAYING;
                newGame();
                break;
            case MotionEvent.ACTION_UP:
                //if from arrows stop player
        }
        return true;
    }
    public boolean HandleGameplayEvent(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //if player touches left arrow
                //if player touches right arrow
                //if player touches middle
                //if player touches pause
                break;
            case MotionEvent.ACTION_UP:
                //if from arrows stop player
        }
        return true;
    }
    public boolean HandlePauseEvent(MotionEvent event){
        return true;
    }
    public boolean HandleDeathEvent(MotionEvent event){
        return true;
    }
}
