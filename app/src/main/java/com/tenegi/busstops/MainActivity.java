package com.tenegi.busstops;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.tflService.tflService;

public class MainActivity extends AppCompatActivity {
    private TextView statusTextView;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String filePath = bundle.getString(tflService.FILEPATH);
                int resultCode = bundle.getInt(tflService.RESULT);
                if(resultCode == RESULT_OK)  {
                    Toast.makeText(MainActivity.this, "Download complete. Download URI: " + filePath,Toast.LENGTH_LONG).show();
                    statusTextView.setText("Download done");
                } else {
                    Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                    statusTextView.setText("Download failed");
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = (TextView) findViewById(R.id.status);
    }
    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, new IntentFilter(tflService.NOTIFICATION));
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }
    public void onClick(View view){
        Intent intent = new Intent(this, tflService.class);
        intent.putExtra(tflService.FILENAME, "x.csv");
        intent.putExtra(tflService.REMOTEURL, "http://maplyndon.azurewebsites.net/x.csv");
        startService(intent);
        statusTextView.setText("Service Started");
    }
}
