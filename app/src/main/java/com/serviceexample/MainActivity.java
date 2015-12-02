package com.serviceexample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection{
    private MyService mMyService;
    private boolean isServiceBounded;
    private Button button1, button2;
    private TextView textView;
    private Handler mMainHandler;
    private static HandlerThread mWorkerThread;
    private TestClass mWorkerHandler;

    static {
        mWorkerThread = new HandlerThread(MainActivity.class.getSimpleName());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);
        textView = (TextView)findViewById(R.id.random_number_text);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        mMainHandler = new Handler();
        if (!mWorkerThread.isAlive()) {
            mWorkerThread.start();
        }
        mWorkerHandler = new TestClass(mWorkerThread.getLooper(), mMainHandler,textView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        startService();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopService();
    }

    private void startService() {
        startService(new Intent(this, MyService.class));
    }

    private void stopService () {
        stopService(new Intent(this, MyService.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:{
                mWorkerHandler.obtainMessage(1).sendToTarget();
                break;
            }
            case R.id.button2: {
                mWorkerHandler.obtainMessage(2).sendToTarget();
            }
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        MyService.MyBinder myBinder = (MyService.MyBinder)service;
        mMyService = myBinder.getService();
        isServiceBounded = true;

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

        isServiceBounded = false;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBounded) {
            unbindService(this);
        }
    }

    private static class TestClass extends Handler {

        Looper mLooper;
        Handler mMainThreadHandler;
        TextView mTextView;
        public TestClass(Looper lopper, Handler mainThreadHandler, TextView textView) {
            super(lopper);
            this.mLooper = lopper;
            this.mMainThreadHandler = mainThreadHandler;
            this.mTextView = textView;

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {

                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText("1 called");
                        }
                    });
                    break;

                }
                case 2: {
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText("2 called");
                        }
                    });
                    break;
                }
            }
        }
    }
}
