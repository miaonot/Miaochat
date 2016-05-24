package com.miaonot.www.miaochat.activity;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.miaonot.www.miaochat.activity.adapter.MyAdapter;
import com.miaonot.www.miaochat.R;
import com.miaonot.www.miaochat.database.DatabaseHelper;
import com.miaonot.www.miaochat.module.Friend;
import com.miaonot.www.miaochat.service.SocketService;
import com.miaonot.www.miaochat.utils.SocketUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Const for handler
    private final int CONNECTION = 0;
    private final int ACCOUNT = 1;
    private final int FRIEND = 2;

    //Handler and Message
    public static Handler handler;
    private Message message = new Message();

    //RecyclerView
    private RecyclerView recyclerView;
    private RecyclerView.Adapter Adapter;
    private RecyclerView.LayoutManager layoutManager;

    //Data
    private List<Friend> friends;
    private List<Friend> newList;
    String userId = null;

    //Other UI
    private TextView textView;
    private FloatingActionButton fab;

    //DB
    private DatabaseHelper databaseHelper;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isServiceRunning = false;

        //user's information, use for auto sign in
        final String password;
        Boolean isAutoSignIn;

        //read user_data from the database
        databaseHelper = new DatabaseHelper(this, "miaochat.db", null, 1);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = db.query("user", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            isAutoSignIn = (cursor.getInt(cursor.getColumnIndex("is_auto_sign_in")) != 0);
            userId = cursor.getString(cursor.getColumnIndex("user_id"));
            password = cursor.getString(cursor.getColumnIndex("password"));
        } else {
            isAutoSignIn = false;
            userId = null;
            password = null;
        }

        //make sure user is sign in, if not, turn to the LoginActivity
//        final SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
//        final SharedPreferences.Editor editor = sharedPreferences.edit();

//        if(!sharedPreferences.getBoolean("is_auto_sign_in", false)) {
        if (!isAutoSignIn) {
            Log.d("MainActivity", "false");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, 0);
        }
//        if(!sharedPreferences.getBoolean("is_auto_sign_in", false)) {
        if (!isAutoSignIn) {
            finish();
        }

//        if (sharedPreferences.getBoolean("is_auto_sign_in", false)) {
        if (isAutoSignIn) {
            //check if service is running to prevent service been killed when the activity is create, if not, sign in again
            ActivityManager myManager = (ActivityManager) this.getApplicationContext().
                    getSystemService(Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningService =
                    (ArrayList<ActivityManager.RunningServiceInfo>) myManager
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
//                            if (!SocketUtil.signIn(sharedPreferences.getString("user_name", null),
//                                    sharedPreferences.getString("user_password", null))) {
                                if (!SocketUtil.signIn(userId, password)) {
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

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getBaseContext(), ChatActivity.class));
                onNavFriend();
            }
        });

        textView = (TextView) findViewById(R.id.app_name);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setVisibility(View.INVISIBLE);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //read friend information from the database first
        if (userId != null) {
            cursor = db.query("friend", new String[] {"id", "nickname"}, "user_id = ?",
                    new String[] {userId}, null, null, null);
            friends = new ArrayList<>();
            int counter = 0;
            Friend friend;
            if (cursor.moveToFirst()) {
                do {
                    friend = new Friend(cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("nickname")));
                    friends.add(friend);
                    counter++;
                } while (cursor.moveToNext());
            }
        }

        // specify an adapter (see also next example)
        Adapter = new MyAdapter(friends);
        recyclerView.setAdapter(Adapter);

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
                } else if (message.what == FRIEND) {
                    if (message.obj.equals(true)) {
                        Adapter = new MyAdapter(newList);
                        recyclerView.setAdapter(Adapter);
//                        for (int i = 0; i < newList.size(); i++) {
//                            if (friends.size() > i) {
//                                if (!friends.get(i).equals(newList.get(i))) {
//                                    friends.set(i, newList.get(i));
//                                    Adapter.notifyItemChanged(i);
//
//                                }
//                            } else {
//                                friends.add(newList.get(i));
//                                Adapter.notifyItemInserted(i);
//                            }
//                        }
                    Log.d("UI", "friends list changed");
                    Log.d("UI", friends.size() + "");
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

//            SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("is_auto_sign_in",false);
//            editor.apply();
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.delete("user", "is_auto_sign_in = ?", new String[] {"1"});
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
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_friend) {
            onNavFriend();
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

    private void onNavFriend() {
        recyclerView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "getFriendThread running");
                boolean isfresh = false;
                while (!isfresh) {
                    if (userId != null) {
                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
//                Friend[] friends = SocketUtil.requestFriends(sharedPreferences.
//                        getString("user_name", null));
                        Friend[] friendslist = SocketUtil.requestFriends(userId);
                        newList = new ArrayList<Friend>();
                        if (friends != null) {
//                editor.putInt("friend_num", friends.length);
                            Log.d("Friend", friendslist.length + "");
                            friends = new ArrayList<Friend>();
                            for (int i = 0; i < friendslist.length; i++) {
                                newList.add(friendslist[i]);
                                if (!db.query("friend",
                                        new String[] {"id"}, "id = ? AND user_id = ?",
                                        new String[] {newList.get(i).getId(), userId}, null, null,
                                        null).moveToFirst()) {
                                    ContentValues values = new ContentValues();
                                    values.put("user_id", userId);
                                    values.put("id", newList.get(i).getId());
                                    values.put("nickname", newList.get(i).getNickname());
                                    db.insert("friend", null, values);
                                    values.clear();
                                }
//                    editor.putString("friend" + i, friends[i].getNickname());
                                Log.d("Friend", newList.get(i).getNickname());
                            }
                            isfresh = true;
                            if (!friends.equals(newList)) {
                                message = handler.obtainMessage();
                                message.what = FRIEND;
                                message.obj = true;
                                MainActivity.handler.sendMessage(message);
                            }
//                editor.apply();
                        }
                    }
                }

            }
        }).start();
    }

}
