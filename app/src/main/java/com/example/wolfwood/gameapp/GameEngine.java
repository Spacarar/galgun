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
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Wolfwood on 11/29/2017.
 */

public class GameEngine extends SurfaceView implements Runnable {
    //main game loop thread
    private Thread thread = null;
    //context

    //sounds
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private int openingSound =-1;
   // private int music_1 = -1;

    //FIXME MORE SOUNDS HERE

    //screen size
    private int screenX;
    private int screenY;
    private Context myContext;

    //player information
    private int lives;
    private int score;
    private int currentLevel;

    //button area's
    private RectF leftArrowRect;
    private RectF rightArrowRect;
    private RectF pauseButtonRect;
    private RectF resumeRect;

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
        myContext=context;
        screenX = size.x;
        screenY = size.y;
        lives = 3;
        score = 0;
        gamestate=GAMESTATE.MAINMENU;

        //prepare the touch areas
        leftArrowRect = new RectF(0,(float)(screenY*.7),(float)(screenX*.2),screenY);
        rightArrowRect = new RectF((float)(screenX*.8),(float)(screenY*.7),screenX,screenY);
        pauseButtonRect =  new RectF((float)(screenX*.8),0,screenX,(float)(screenY*.3));
        resumeRect = new RectF(0,0,screenX,(float)(screenY*.6));

        //Media player is used for background music (large sounds)
        mediaPlayer=mediaPlayer.create(myContext,R.raw.menumusic);
        mediaPlayer.setLooping(true);

        //FIXME add sound effects
        //soundpool is for small sounds & effects
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            //add sound using
            descriptor = assetManager.openFd("opening.ogg");
            openingSound = soundPool.load(descriptor,0);
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


        //this is called when a sound from soundpool completes loading
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                switch (sampleId) {
                    case 1:
                        Log.d("SoundPool 1","Sound is ready...");
                        break;
                    case 2:
                        Log.d("SoundPool 2","sound is ready, launching");
                        //soundPool.play(menuMusic,1,1,0,-1,1);
                        break;
                }
            }
        });

        //start the game
        newGame();
    }

    @Override
    public void run() {
        while (isPlaying) {
            //controls fps
            if (updateRequired()) {
                update();
                drawThings();
            }
        }
       // soundPool.stop(music_1);
       // soundPool.release();
    }
    public void newGame(){
        resumeMusic();
        currentLevel=1;
        lives=3;
        score=0;
        nextFrameTime=System.currentTimeMillis();
    }

    // Are we due to update the frame
    private boolean updateRequired() {
        //why not lower the framerate in the menus
        if(gamestate==GAMESTATE.MAINMENU||gamestate==GAMESTATE.PAUSED){
            if(nextFrameTime<=System.currentTimeMillis()){
                nextFrameTime=System.currentTimeMillis()+MILLIS_PER_SECOND/3;
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
                pilot.update();
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
            //grab the canvas
            canvas = surfaceHolder.lockCanvas();
            //color base black
            canvas.drawColor(Color.argb(255, 20, 20, 20));
            //draw based on whats going on
            switch (gamestate) {
                case MAINMENU:
                    drawStars(paint,canvas);
                    paint.setTextSize(200);
                    paint.setColor(Color.argb(255,240,200,240));
                    canvas.drawText("GalaGun", screenX / 2 - 420, screenY / 2 - 50, paint);
                    paint.setTextSize(60);
                    canvas.drawText("tap to play!",screenX/2-100,screenY/2+50,paint);
                    break;
                case PLAYING:
                    drawStars(paint,canvas);
                    pilot.drawShip(canvas, paint);
                    break;
                case PAUSED:
                    paint.setTextSize(200);
                    paint.setColor(Color.argb(255,240,200,240));
                    canvas.drawText("Resume",screenX / 2 - 420,screenY/4,paint);
                    paint.setTextSize(60);
                    canvas.drawText("Quit",screenX/2-100,screenY-100,paint);
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
            pauseMusic();
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
        resumeMusic();
    }

    public void pauseMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }
    public void resumeMusic(){
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

    }
    public void stopMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer=null;
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
                gamestate = GAMESTATE.PLAYING;
                stopMusic();
                mediaPlayer = mediaPlayer.create(myContext, R.raw.music1);
                soundPool.play(openingSound,1,1,0,0,1);
                newGame();
                return true;
            case MotionEvent.ACTION_UP:
                //probably dont do anything        }
                return true;
        }
        return true;
    }
    public boolean HandleGameplayEvent(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(leftArrowRect.contains(event.getRawX(),event.getRawY())){
                    //move left
                    pilot.setVelocity(-15,0);
                }
                else if(rightArrowRect.contains(event.getRawX(),event.getRawY())) {
                    //move right
                    pilot.setVelocity(15,0);
                }
                else if(pauseButtonRect.contains(event.getRawX(),event.getRawY())){
                    gamestate=GAMESTATE.PAUSED;
                    stopMusic();
                    mediaPlayer = mediaPlayer.create(myContext,R.raw.menumusic);
                    mediaPlayer.start();
                    //pause game
                }
                else {
                    //shoot
                }
                break;
            case MotionEvent.ACTION_UP:
                pilot.setVelocity(0,0);
                //if from arrows stop player
        }
        return true;
    }
    public boolean HandlePauseEvent(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(resumeRect.contains(event.getRawX(),event.getRawY())) {
                    gamestate = GAMESTATE.PLAYING;
                    stopMusic();
                    mediaPlayer = mediaPlayer.create(myContext, R.raw.music1);
                    mediaPlayer.start();
                    return true;
                }
                else{
                    gamestate=GAMESTATE.MAINMENU;


                }
            case MotionEvent.ACTION_UP:
                //probably dont do anything        }
                return true;
        }
        return true;
    }
    public boolean HandleDeathEvent(MotionEvent event){
        return true;
    }
}
