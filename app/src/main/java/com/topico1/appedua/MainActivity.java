package com.topico1.appedua;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView mainLogo = findViewById(R.id.mainLogo);
        TextView welcomeText = findViewById(R.id.welcomeText);

        ProgressBar loadingSpinner = findViewById(R.id.loadingSpinner);
        loadingSpinner.setVisibility(View.GONE);  // Ocultar el ProgressBar

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        mainLogo.startAnimation(fadeInAnimation);
        welcomeText.startAnimation(fadeInAnimation);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // No hacer nada al inicio de la animación
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, login.class);
                        startActivity(intent);
                        finish();
                    }
                }, 500);  // Añade un pequeño retraso (500 ms) para asegurar que la animación termine
            }


            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

}