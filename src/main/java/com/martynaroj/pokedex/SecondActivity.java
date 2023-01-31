package com.martynaroj.pokedex;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.martynaroj.pokedex.fragments.PokemonDetails;

public class SecondActivity extends AppCompatActivity {

 TextView txt_currentAccel, txt_prevAccel, txt_acceleration;
 ProgressBar prog_shakeMeter;

    PokemonDetails fragment = new PokemonDetails();


 private SensorManager mSensorManager;
 private Sensor mAcceleometer;
    private double accelerationCurrentValue;
    private double accelerationPreviousValue;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
     @Override
     public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y =sensorEvent.values[1];
        float z =sensorEvent.values[2];

        accelerationCurrentValue = Math.sqrt((x*x+y*y+z*z));

        double changeInAccelleration = Math.abs(accelerationCurrentValue - accelerationPreviousValue);
         accelerationPreviousValue = accelerationCurrentValue;
        txt_currentAccel.setText("Current= "+(int) accelerationCurrentValue);
        txt_prevAccel.setText("Prev = "+(int) accelerationPreviousValue);
        txt_acceleration.setText("Acceleration chnge ="+(int) changeInAccelleration);

        prog_shakeMeter.setProgress((int)changeInAccelleration);

         if (changeInAccelleration>5){
            // Random random = new Random();

             // Update the UI to display the selected item
           //  getNavigationsInteractions().changeFragment(PokemonDetails.newInstance(pokemonsList.get(random.nextInt(pokemonsList.size()))), true);
         }

     }

     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {

     }
 };

 @Override
    protected void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_second);

     txt_acceleration = findViewById(R.id.txt_accel);
     txt_currentAccel = findViewById(R.id.txt_currentAccel);
     txt_prevAccel = findViewById(R.id.txt_prevAccel);

     prog_shakeMeter = findViewById(R.id.prog_shakeMeter);

     mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
     mAcceleometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 }
 protected void onResume(){
     super.onResume();
     mSensorManager.registerListener(sensorEventListener, mAcceleometer, SensorManager.SENSOR_DELAY_NORMAL);

 }
 protected void onPause(){
     super.onPause();
     mSensorManager.unregisterListener(sensorEventListener);
 }
}