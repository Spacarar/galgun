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

//TODO fix text size to be dynamic to screen size
    //TODO add enemy firing
    //TODO add hit detection
    //TODO add gameover
    //TODO fix ship design



public class GameEngine extends SurfaceView implements Runnable {
    //main game loop thread
    private Thread thread = null;
    //context

    //sounds
    private SoundPool soundPool;
    private MediaPlayer mediaPlayer;
    private int openingSound =-1;
    private int bigTextSize;
    private int smallTextSize;

    //FIXME MORE SOUNDS HERE

    //screen size
    private int screenX;
    private int screenY;
    private Context myContext;

    //player information
    private int lives;
    private int score;
    private final int basePoints=50;
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
    private Random starRandomizer;


    //game objects
    private PilotShip pilot;
    private EnemyShip[][] enemies;
    private int enemyBottomRow;
    private float enemyBaseSpeed;
    private long eFallTime;
    private boolean eMovingLeft;
    //projectiles thrown by enemies or players
    private Projectile[] eProj;
    private int eShotNumber;
    private Projectile[] pProj;
    private int pShotNumber;


    public GameEngine(Context context, Point size) {
        super(context);
        myContext=context;
        screenX = size.x;
        screenY = size.y;
        lives = 3;
        enemyBaseSpeed = 8;
        score = 0;
        gamestate=GAMESTATE.MAINMENU;

        //prepare the touch areas
        leftArrowRect = new RectF(0,(float)(screenY*.7),(float)(screenX*.2),screenY);
        rightArrowRect = new RectF((float)(screenX*.8),(float)(screenY*.7),screenX,screenY);
        pauseButtonRect =  new RectF((float)(screenX*.8),0,screenX,(float)(screenY*.3));
        resumeRect = new RectF(0,0,screenX,(float)(screenY*.6));

        //prepare text sizes
        bigTextSize=(int)(screenX*.18);
        smallTextSize=(int)(screenX*.05);

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

        currentLevel = 2;
        pilot = new PilotShip(screenX, screenY);
        initEnemyFleet();
        eFallTime=System.currentTimeMillis();
        eMovingLeft=false;
        //enemies[][]
        initProjectiles();
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
    private void initEnemyFleet(){
        enemies = new EnemyShip[3][5];
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                enemies[i][j]=new EnemyShip(screenX,screenY,(int)(screenX*.1*(j+1)),(int)(screenY*.1*(i+1)));
            }
        }
        //kill off the two corners to give a more triangle fleet feel to the enemy ships
        enemies[2][0].kill();//bottom left
        enemies[2][4].kill();//bottom right
        enemyBottomRow=2;
    }
    private void resetEnemyFleet(){
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                enemies[i][j].setPosition((int)(screenX*.1*(j+1)),(int)(screenY*.1*(i+1)));
                enemies[i][j].revive();
            }
        }
        enemies[2][0].kill();//bottom left
        enemies[2][4].kill();//bottom right
        enemyBottomRow=2;
    }
    private void initProjectiles(){
        eProj=new Projectile[15];
        eShotNumber=0;
        pProj=new Projectile[15];
        pShotNumber=0;
        for(int i=0;i<15;i++){
            pProj[i]=new Projectile(screenX,screenY,-100,-100,0,0);
            eProj[i]=new Projectile(screenX,screenY,-100,-100,0,0);
        }
    }
    private void clearProjectiles(){
        for(int i=0;i<15;i++){
            pProj[i]=new Projectile(screenX,screenY,-100,-100,0,0);
            eProj[i]=new Projectile(screenX,screenY,-100,-100,0,0);
        }
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
        initEnemyFleet();
        pilot.setPosition(screenX/2-pilot.width()/2,screenY-pilot.height()*2);
        currentLevel=1;
        lives=3;
        score=0;
        nextFrameTime=System.currentTimeMillis();
    }
    public void nextLevel(){
        if(lives<3)lives++;
        clearProjectiles();
        resetEnemyFleet();
        pilot.setPosition(screenX/2-pilot.width()/2,screenY-pilot.height()*2);
        currentLevel++;
        score+=100*currentLevel;
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
                if(lives>0) {
                    if(!allEnemiesDead()) {
                        pilot.update();
                        updateEnemies();
                        updateProjectiles();
                    }
                    else{
                        nextLevel();
                        //FIXME change level
                        //FIXME play sound
                    }
                }
                else{
                    gamestate=GAMESTATE.DEAD;
                }
                break;
            case PAUSED:
                break;
            case DEAD:
                break;
        }

        //update things
    }
    private void checkEnemiesHit(int row, int col){
                for(int p=0;p<15;p++){
                    if(enemies[row][col].wasHit(pProj[p].rect())&&!enemies[row][col].isDead()){
                        pProj[p]=new Projectile(screenX,screenY,-100,-100,0,0);
                        enemies[row][col].kill();
                        score+=basePoints*(currentLevel);
                    }
        }
    }
    private boolean allEnemiesDead(){
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                //if there is one enemy alive, all enemies are not dead
                if(!enemies[i][j].isDead())return false;
            }
        }
        return true;
    }

    private void updateEnemies(){
        //if it reaches an edge, fall and turn around
        checkFall();
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                //FIXME check projectile hit
                checkEnemiesHit(i,j);
                if(eFallTime>System.currentTimeMillis()){
                    enemies[i][j].setVelocity(0,10);
                }
                else{
                    if(eMovingLeft){
                        //Log.d("enemies moving","left");
                        enemies[i][j].setVelocity(-enemyBaseSpeed*(float)(1+currentLevel*.5),0);
                    }
                    else{
                       // Log.d("enemies moving","right");
                        enemies[i][j].setVelocity(enemyBaseSpeed*(float)(1+currentLevel*.5),0);
                    }
                }
                enemies[i][j].update();
            }
        }
    }
    private void checkFall(){
        if(enemies[2][0].left()<=screenX*.1){
            //Log.d("ENEMY LEFT BOUND","bounded by left side");
            for(int i=0;i<3;i++){
                for(int j=0;j<5;j++){
                    enemies[i][j].nudgeRight();
                }
            }
            eMovingLeft=false;
            eFallTime=System.currentTimeMillis()+2;
        }
        else if(enemies[2][4].right()>=screenX*.9){
            //Log.d("ENEMY RIGHT BOUND","bounded by right side");
            for(int i=0;i<3;i++){
                for(int j=0;j<5;j++){
                    enemies[i][j].nudgeLeft();
                }
            }
            eMovingLeft=true;
            eFallTime=System.currentTimeMillis()+2;
        }
    }
    private void updateProjectiles(){
        for(int i=0;i<15;i++){
            eProj[i].update();
            pProj[i].update();
        }
    }
    private void drawThings() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.argb(255, 20, 20, 20));
            //draw based on whats going on
            switch (gamestate) {
                case MAINMENU:
                    drawStars(paint,canvas);
                    paint.setTextSize(bigTextSize);
                    paint.setColor(Color.argb(255,240,200,240));
                    canvas.drawText("GalaGun", screenX / 2 - bigTextSize*2, screenY / 2 - 50, paint);
                    paint.setTextSize(smallTextSize);
                    canvas.drawText("tap to play!",screenX/2-smallTextSize,screenY/2+50,paint);
                    break;
                case PLAYING:
                    drawStars(paint,canvas);
                    drawPauseButton(paint,canvas);
                    drawLeftArrow(paint,canvas);
                    drawRightArrow(paint,canvas);
                    drawScore(paint,canvas);
                    drawLives(paint,canvas);
                    drawProjectiles(paint,canvas);
                    drawEnemies(paint,canvas);
                    pilot.drawShip(canvas, paint);
                    break;
                case PAUSED:
                    drawStars(paint,canvas);
                    paint.setTextSize(bigTextSize);
                    paint.setColor(Color.argb(255,240,200,240));
                    canvas.drawText("Resume",screenX / 2 - bigTextSize*2,screenY/4,paint);
                    paint.setTextSize(smallTextSize);
                    canvas.drawText("Quit",screenX/2-smallTextSize,screenY-100,paint);
                    break;
                case DEAD:
                    break;
                default:
                    Log.d("Switch Error!", "gamestate unknown!");

            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }
    private void drawEnemies(Paint p, Canvas c){
        for(int i=0;i<3;i++){
            for(int j=0;j<5;j++){
                enemies[i][j].drawShip(c,p);
            }
        }
    }
    private void drawStars(Paint p, Canvas c){

        int radius=4;
        int numStars=30;
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
    private void drawScore(Paint p,Canvas c){
        p.setTextSize(smallTextSize);
        paint.setColor(Color.argb(255,200,200,200));
        canvas.drawText("Score: "+score,(float)(screenX*.05),(float)(screenY*.08),paint);
    }
    private void drawLives(Paint p,Canvas c){
        p.setTextSize(smallTextSize);
        paint.setColor(Color.argb(255,200,200,200));
        canvas.drawText("lives: "+lives,(float)(screenX*.05),(float)(screenY*.75),paint);

    }

   private void drawPauseButton(Paint p, Canvas c){
        paint.setTextSize((int)(screenX*.05));
        paint.setColor(Color.argb(255,255,255,255));
        canvas.drawText("||",(float)(screenX*.9),(float)(screenY*.1),paint);
   }
   private void drawLeftArrow(Paint p, Canvas c){
       paint.setTextSize((int)(screenX*.1));
       paint.setColor(Color.argb(255,255,255,255));
       canvas.drawText("<<",(float)(screenX*.1),(float)(screenY*.9),paint);
   }
   private void drawRightArrow(Paint p, Canvas c){
       paint.setTextSize((int)(screenX*.1));
       paint.setColor(Color.argb(255,255,255,255));
       canvas.drawText(">>",(float)(screenX*.85),(float)(screenY*.9),paint);
   }
   private void drawProjectiles(Paint p, Canvas c){
       for(int i=0;i<15;i++){
           paint.setColor(Color.argb(255,255,100,100));
           eProj[i].draw(p,c);
           paint.setColor(Color.argb(255,100,255,100));
           pProj[i].draw(p,c);
       }
   }

    public void pause() {
        isPlaying = false;
        try {
            soundPool.autoPause();
            pauseMusic();
            gamestate=GAMESTATE.PAUSED;
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

    private void pauseMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }
    private void resumeMusic(){
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

    }
    private void stopMusic(){
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
                mediaPlayer.setLooping(true);
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
                    pilot.setVelocity(-25,0);
                }
                else if(rightArrowRect.contains(event.getRawX(),event.getRawY())) {
                    //move right
                    pilot.setVelocity(25,0);
                }
                else if(pauseButtonRect.contains(event.getRawX(),event.getRawY())){
                    gamestate=GAMESTATE.PAUSED;
                    stopMusic();
                    mediaPlayer = mediaPlayer.create(myContext,R.raw.menumusic);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                    //pause game
                }
                else {
                    if(pilot.canFire()){
                        //Log.d("SHOTS FIRED", "a shot should be fired here ");
                        pilot.shoot();
                        if(pShotNumber>=15)pShotNumber%=15;
                        pProj[pShotNumber]=new Projectile(screenX,screenY,pilot.middleX(),pilot.topY(),0,-30);
                        pShotNumber++;
                    }
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
                    mediaPlayer.setLooping(true);
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
