package com.example.camerax;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Size;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 83854;

    private ImageView preview;

    ListenableFuture<ProcessCameraProvider> cameraProvidedFuture;

    YUVtoRGB translator = new YUVtoRGB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) //проверка на разрешение использования камеры
                != PackageManager.PERMISSION_GRANTED){
            //если нет разрешения, то запрашиваем у пользователя
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
        else{ // иначе инициализируем камеру
            initializeCamera();
        }
    }

    @Override // обрабатываем результат - получили или не получили доступ к камере
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){ // если пользователь дал разрешение
            initializeCamera(); // то инициализируем камеру
        }
    }

    private void initializeCamera(){
        cameraProvidedFuture = ProcessCameraProvider.getInstance(this); // запуск инициализации
        cameraProvidedFuture.addListener(new Runnable() {
            @Override
            public void run() { // что сделать после окончания инициализации (нужно тк инициализация занимает время)
                try {
                    ProcessCameraProvider cameraProvider = cameraProvidedFuture.get();

                    // формируем сценарий
                    // динамическая обработка изображения с камеры

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1024, 768)) // целевое разрешение
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // что делать с изображениями (только последнее изображение будет сохранятся)
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder() // выбор камеры
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK) // куда смотрит камера
                            .build();

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this), // что делать с изображением
                            new ImageAnalysis.Analyzer() {
                                @Override
                                public void analyze(@NonNull ImageProxy image) {
                                    Image img = image.getImage();
                                    Bitmap bitmap = translator.translateYUV(img, MainActivity.this);

                                    // переведем изображение в чб
                                    int size = bitmap.getWidth() * bitmap.getHeight(); // сколько пикселей на изображении
                                    int [] pixels = new int[size];
                                    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                                            bitmap.getWidth(), bitmap.getHeight());
                                    for (int i = 0; i < size; i++){
                                        int color = pixels[i];
                                        int r = color >> 16 & 0xff;
                                        int g = color >> 8 & 0xff;
                                        int b = color & 0xff;
                                        int gray =  (r + g + b) / 3;
                                        pixels[i] = 0xff000000 | gray << 16 | gray << 8 | gray; // обратно записываем результат
                                    }

                                    bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                                            bitmap.getWidth(), bitmap.getHeight());

                                    preview.setRotation(image.getImageInfo().getRotationDegrees()); // повренуть изображение как надо (тк картинка может прийти под неправильным углом)
                                    preview.setImageBitmap(bitmap);
                                    image.close(); // закончили работу с текущим изображением и можно получить другое
                                }
                            });

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, imageAnalysis);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
}