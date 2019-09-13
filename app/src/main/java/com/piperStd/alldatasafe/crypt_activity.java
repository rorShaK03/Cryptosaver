package com.piperStd.alldatasafe;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.widget.PopupMenu;
import android.app.Fragment;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.ViewFlipper;

import static com.piperStd.alldatasafe.utils.Others.tools.*;

import com.google.android.gms.auth.api.Auth;
import com.google.android.material.navigation.NavigationView;
import com.piperStd.alldatasafe.Core.AuthNode;
import com.piperStd.alldatasafe.Core.AuthServices;
import com.piperStd.alldatasafe.UI.ActivityLauncher;
import com.piperStd.alldatasafe.UI.Fragments.CryptCard;
import com.piperStd.alldatasafe.UI.MainNavigationListener;
import com.piperStd.alldatasafe.utils.Cryptographics.Crypto;
import com.piperStd.alldatasafe.utils.Detectors.NFC.NfcHelper;


public class crypt_activity extends AppCompatActivity implements View.OnClickListener{



    ViewFlipper flipper = null;
    public static Context context = null;
    NavigationView navigation = null;
    DrawerLayout drawerLayout;
    ActivityLauncher launcher = null;
    MainNavigationListener navListener = null;

    EditText editPass;
    CheckBox useNfc;
    Button nextBtn;
    Button add_btn;
    ScrollView scroll;

    LinearLayout frags;

    CryptCard[] cards = new CryptCard[10];
    byte card_i = 0;

    boolean nfc_used = false;

    NfcAdapter adapter = null;
    PendingIntent pending = null;
    IntentFilter[] filters = null;
    String[][] techList;

    byte[] key = new byte[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        launcher = new ActivityLauncher(this);
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
        adapter = NfcAdapter.getDefaultAdapter(this);
        pending = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        filters = new IntentFilter[]{NfcHelper.createNdefFilter(NfcHelper.TYPE_KEY), NfcHelper.createTechFilter()};
        techList = new String[][]{NfcHelper.knownTech};
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        flipper.setDisplayedChild(0);
        navigation.getMenu().getItem(0).setChecked(true);
        editPass = findViewById(R.id.editPass);
        nextBtn = findViewById(R.id.next_btn);
        add_btn = findViewById(R.id.add_btn);
        useNfc = findViewById(R.id.use_nfc);
        frags = findViewById(R.id.crypt_frags);
        scroll = findViewById(R.id.crypt_scroll);
        useNfc.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        add_btn.setOnClickListener(this);
        if(card_i == 0)
            addCard();
        /*

            */
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF)) {
            adapter.enableForegroundDispatch(this, pending, filters, techList);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF)) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if(intent.hasExtra(NfcAdapter.EXTRA_TAG))
        {
            NfcHelper helper = new NfcHelper(intent.getExtras());
            this.key = Base64.decode(helper.readTag(), Base64.DEFAULT);
            editPass.setText("");
            editPass.setEnabled(false);
            useNfc.setChecked(true);
            nfc_used = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }





    @Override
    public void onClick(View view)
    {
        if(view.getId() == nextBtn.getId()) {

            RadioGroup place = findViewById(R.id.placeGroup);
            int placeId = place.getCheckedRadioButtonId();
            AuthNode[] nodes = new AuthNode[card_i];
            for (int i = 0; i < card_i; i++)
                nodes[i] = cards[i].getNode();
            byte[] key_bytes = null;
            if (!nfc_used && !editPass.getText().toString().equals(""))
                key_bytes = Crypto.getKDF(editPass.getText().toString());
            else if (nfc_used)
                key_bytes = this.key;
                if (nodes[0].password.length() != 0 && nodes[0].login.length() != 0 && key_bytes != null) {
                    String encrypted = AuthNode.getEncryptedStringFromArray(nodes, key_bytes);
                    AuthNode.DecryptAndParseArray(encrypted, key_bytes);
                    /*
                    switch (placeId) {
                        case R.id.radioQR:
                            launcher.launchQRCodeActivity(service, login, password, key_bytes);
                            break;
                        case R.id.radioInternal:
                            launcher.launchInternalShowActivity(service, login, password, key_bytes);
                            break;
                        default:
                            showException(this, "Choose place for saving");
                    }
                } else if (login.length() == 0) {
                    showException(this, "Login is empty");
                } else if (password.length() == 0) {
                    showException(this, "Password is empty");
                } else if (key_bytes == null) {
                    showException(this, "Encryption key is empty");
                }
                            */
            }
        }
        else if(view.getId() == useNfc.getId())
        {
            if(useNfc.isChecked() == true)
            {
                editPass.setText("");
                editPass.setEnabled(false);
            }
            else
            {
                nfc_used = false;
                editPass.setEnabled(true);
            }
        }
        else if(view.getId() == add_btn.getId())
        {
            addCard();
        }

    }

    private void addCard()
    {
        final FrameLayout current = new FrameLayout(this);
        current.setId(card_i + 1);
        frags.addView(current);
        cards[card_i] = new CryptCard();
        FragmentTransaction trans = getFragmentManager().beginTransaction();
        trans.add(current.getId(), cards[card_i]);
        trans.commit();
        card_i++;
        current.setFocusable(true);
        current.setFocusableInTouchMode(true);
        scroll.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    }
        , 100);
    }



}

