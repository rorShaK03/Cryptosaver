package com.piperStd.alldatasafe;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.material.navigation.NavigationView;
import com.piperStd.alldatasafe.Core.AuthNode;
import com.piperStd.alldatasafe.Core.AuthServices;
import com.piperStd.alldatasafe.UI.MainNavigationListener;
import com.piperStd.alldatasafe.utils.Detectors.NFC.NfcHelper;

import java.io.File;
import java.io.FileOutputStream;

import static com.piperStd.alldatasafe.utils.Others.tools.showException;

public class internal_show_activity extends AppCompatActivity {

    String FILENAME = "encrypted";

    ViewFlipper flipper = null;
    MainNavigationListener navListener = null;
    NavigationView navigation = null;
    DrawerLayout drawerLayout;

    TextView internal_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        flipper = findViewById(R.id.viewFlipper);
        drawerLayout = findViewById(R.id.drawer_layout);
        navListener = new MainNavigationListener(this, drawerLayout);
        navigation = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigation.setNavigationItemSelectedListener(navListener);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        flipper.setDisplayedChild(8);
        navigation.getMenu().getItem(0).setChecked(true);
        internal_state = findViewById(R.id.internalState);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        String login = intent.getStringExtra("LOGIN");
        String password = intent.getStringExtra("PASSWORD");
        byte service = intent.getByteExtra("SERVICE", AuthServices.UNKNOWN);
        byte[] key = intent.getByteArrayExtra("KEY");
        AuthNode node = new AuthNode(service, login, password);
        new WriteTask().execute(node.getEncryptedString(key));
    }


    class WriteTask extends AsyncTask<String, Void, Void>
    {
        @Override
        public Void doInBackground(String[] params)
        {
            try
            {
                FileOutputStream outStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                outStream.write(params[0].getBytes());
                outStream.close();
                Log.e("writed", params[0]);
                internal_state.setText("Запись успешно произведена!");
                internal_state.setTextColor(Color.GREEN);
                Thread.sleep(1000);
                finish();
            }
            catch (Exception e)
            {
                showException(this, e.getMessage());
                Log.e("Couldn`t write", e.getMessage());
            }
            return null;
        }
    }
}


