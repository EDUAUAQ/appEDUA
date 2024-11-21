package com.topico1.appedua;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView mainLogo = findViewById(R.id.mainLogo);
        TextView welcomeText = findViewById(R.id.welcomeText);
        TextView subtitleText = findViewById(R.id.subtitleText);
        ProgressBar loadingSpinner = findViewById(R.id.loadingSpinner);

        loadingSpinner.setVisibility(View.GONE); // Ocultar el ProgressBar inicialmente

        // Animaciones
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        Animation textFadeIn = AnimationUtils.loadAnimation(this, R.anim.text_fade_in);

        // Iniciar animaciones para el logo y el texto al mismo tiempo
        mainLogo.startAnimation(logoAnimation);
        welcomeText.startAnimation(textFadeIn);
        subtitleText.startAnimation(textFadeIn);

        // Generar el QR al inicio
        generateQRCode();

        // Animaciones terminadas, realizar acciones posteriores
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // No hacer nada
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Mostrar el ProgressBar después de las animaciones
                new Handler().postDelayed(() -> loadingSpinner.setVisibility(View.VISIBLE), 800);

                // Cambiar a la pantalla de login después de un retraso total
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                    finish();
                }, 2500); // Retraso total para cambio de pantalla
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No hacer nada
            }
        };

        // Asignar el mismo listener a ambas animaciones
        logoAnimation.setAnimationListener(animationListener);
        textFadeIn.setAnimationListener(animationListener);
    }

    // Método para generar el QR y guardarlo en almacenamiento interno
    private void generateQRCode() {
        // URL del APK (cambiar por la URL real de tu APK)
        String apkUrl = "https://drive.google.com/file/d/1iyWNNUp_ir_KxC2ANIis36c0cLETCpD0/view?usp=sharing";

        try {
            // Usar ZXing para generar el código QR
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Generar el QR como un Bitmap
            Bitmap bitmap = barcodeEncoder.encodeBitmap(apkUrl, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);

            // Guardar la imagen del QR en la memoria interna
            saveQRCodeToFile(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para guardar el QR como una imagen en la memoria interna
    private void saveQRCodeToFile(Bitmap bitmap) throws IOException {
        // Establece el nombre del archivo de la imagen QR
        String filename = "qr.png";

        // Abrir un FileOutputStream para guardar el archivo
        FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);

        // Comprimir el Bitmap a PNG y guardarlo
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        // Cerrar el flujo de salida
        fos.close();
    }
}