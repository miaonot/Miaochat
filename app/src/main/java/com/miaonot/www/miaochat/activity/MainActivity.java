package com.miaonot.www.miaochat.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.service.SocketService;
import com.miaonot.www.miaochat.utils.SocketUtil;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int CONNECTION = 0;
    private final int ACCOUNT = 1;

    public static Handler handler;
    private Message message = new Message();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isServiceRunning = false;

        //make sure user is sign in, if not, turn to the LoginActivity
        final SharedPreferences sharedPreferences = getSharedPreferences("user", 0);

        if(!sharedPreferences.getBoolean("is_auto_sign_in", false)) {
            Log.d("MainActivity", "false");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
        }
        if(!sharedPreferences.getBoolean("is_auto_sign_in", false)) {
            finish();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ChatActivity.class));

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        if (sharedPreferences.getBoolean("is_auto_sign_in", false)) {
            //check if service is running to prevent service been killed when the activity is create, if not, sign in again
            ActivityManager myManager = (ActivityManager) this.getApplicationContext().getSystemService(
                    Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager
                    .RunningServiceInfo>) myManager
                    .getRunningServices(30);
            for (int i = 0; i < runningService.size(); i++) {
                if (runningService.get(i).service.getClassName()
                        .equals("com.miaonot.www.miaochat.service.SocketService")) {
                    Log.i("MainActivity", "service running");
                    isServiceRunning = true;
                }
            }
            if (!isServiceRunning) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!SocketUtil.signIn(sharedPreferences.getString("user_name", null),
                                    sharedPreferences.getString("user_password", null))) {
                                message.what = ACCOUNT;
                                message.obj = false;
                                MainActivity.handler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            message.what = CONNECTION;
                            message.obj = false;
                            MainActivity.handler.sendMessage(message);
                            e.printStackTrace();
                        }
                    }
                }).start();

                startService(new Intent(this, SocketService.class));
            }
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);
                if (message.what == CONNECTION) {
                    if (message.obj.equals(false)) {
                        Toast.makeText(getBaseContext(), "Connection error",
                                Toast.LENGTH_SHORT).show();
                        stopService(new Intent(getBaseContext(), SocketService.class));
                        startActivityForResult(new Intent(getBaseContext(), LoginActivity.class), 0);
                    }
                } else if (message.what == ACCOUNT) {
                    if (message.obj.equals(false)) {
                        Toast.makeText(getBaseContext(), "Account error",
                                Toast.LENGTH_SHORT).show();
                        stopService(new Intent(getBaseContext(), SocketService.class));
                        startActivityForResult(new Intent(getBaseContext(), LoginActivity.class), 0);
                    }
                }
            }
        };

        Log.d("MainActivity", "onCreate");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("MainActivity", "onDestroy");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sign_out) {

            SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("is_auto_sign_in",false);
            editor.apply();
            stopService(new Intent(this, SocketService.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            if (!this.equals(MainActivity.class)) {
                startActivity(new Intent(this, MainActivity.class));
            }
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            finish();
        }
    }

}
