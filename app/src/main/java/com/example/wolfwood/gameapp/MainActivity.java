package com.example.wolfwood.gameapp;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the gameEngine class
        gameEngine = new GameEngine(this, size);

        // Make gameEngine the view of the Activity
        setContentView(gameEngine);
    }
    // Start the thread in gameEngine
    @Override
    protected void onResume() {
        super.onResume();
        gameEngine.resume();
    }

    // Stop the thread in gameEngine
    @Override
    protected void onPause() {
        super.onPause();
        gameEngine.pause();
    }
}
