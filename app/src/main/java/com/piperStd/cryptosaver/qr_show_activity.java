package com.piperStd.cryptosaver;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.piperStd.cryptosaver.R;
import com.piperStd.cryptosaver.utils.Crypto;
import com.piperStd.cryptosaver.utils.QRTools;
import com.piperStd.cryptosaver.utils.tools;

public class qr_show_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_qr_show_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigation = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        ImageView qrImage = findViewById(R.id.imageView);
        String text = "Hello world!";
        String password = "1234567";
        try
        {
            Crypto crypto = new Crypto(tools.toBytes(text), password);
            crypto.encrypt();
            qrImage.setImageBitmap(QRTools.genBarcode(crypto.genCredentialsArr()));
        }
        catch(Exception e)
        {
            Log.e("Error", e.getMessage());
        }

    }

}
