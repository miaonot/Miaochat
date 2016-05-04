package com.miaonot.www.miaochat.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isServiceRunning = false;


        //make sure user is sign in, if not, turn to the LoginActivity
        final SharedPreferences sharedPreferences = getSharedPreferences("user", 0);

        if(sharedPreferences.getString("user_name", null) == null) {
            Log.d("MainAcitivty", "null");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        if(sharedPreferences.getString("user_name", null) == null) {
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
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

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
                    try{
                        SocketUtil.signIn(sharedPreferences.getString("user_name", null), sharedPreferences.getString("user_password", null));
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Connect error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }).start();

            startService(new Intent(this, SocketService.class));
        }
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
        if (id == R.id.action_settings) {

            SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_name", null);
            editor.apply();
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
            Intent intent = new Intent();
            startActivity(intent);
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
}
