package ru.oversee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationBarItemView;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import java.util.concurrent.Executor;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.oversee.MainFragments.AddFragment;
import ru.oversee.MainFragments.HomeFragment;
import ru.oversee.MainFragments.SettingsFragment;
import ru.oversee.Utils.DialogAuthCallback;
import ru.oversee.Utils.DialogsBiometric;
import ru.oversee.Utils.DialogsPinCode;
import ru.oversee.Utils.GeneralFunctions;
import ru.oversee.Utils.RealmMigrations;

public class MainActivity extends AppCompatActivity {

    private MainActivity parentActivity;

    public Realm uiThreadRealm;
    public Handler backUiThreadHandler;

    public String currentFragment = "";
    private BottomNavigationView bottomNavigationView;
    private RelativeLayout mainLayout;

    private HomeFragment homeFragment;
    private AddFragment addFragment;
    private SettingsFragment settingsFragment;

    public BottomSheetBehavior<ConstraintLayout> sheetPopupCamera;

    // Переменные для камер в детейле
    public ConstraintLayout streamLoaderLayout;
    public LibVLC libVLC;
    public MediaPlayer mediaPlayer;
    public boolean restartStream = true;
    public ConstraintLayout tabLayoutContainer;

    // Потоки для стрима
    public Runnable streamRun;

    public AppSettings appSettings;

    private static long backPressed;

    // Настройки
    public TextView changePinTitle;
    public ConstraintLayout changePinLayout;

    public Executor executor;
    public String biometricEntryStatus;
    public int loginAttempt = 0;

    public boolean cameraChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppActiveTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main_layout);

        parentActivity = this;

        executor = ContextCompat.getMainExecutor(this);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(new RealmMigrations()) // Migration to run
                .allowWritesOnUiThread(true)
                .build();

        HandlerThread backUiThread = new HandlerThread("backUiThread");
        backUiThread.start();
        Looper backUiThreadLooper = backUiThread.getLooper();
        backUiThreadHandler = new Handler(backUiThreadLooper);

        // Поток для базы для UI
        uiThreadRealm = Realm.getInstance(config);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigationView();
        setScaleMenuItem();

        appSettings = AppSettings.getAppSettings(uiThreadRealm);

        ConstraintLayout popupCameraDetail = findViewById(R.id.popup_camera_detail);
        sheetPopupCamera = BottomSheetBehavior.from(popupCameraDetail);

        homeFragment = new HomeFragment(this);
        addFragment = new AddFragment(this);
        settingsFragment = new SettingsFragment(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        if (appSettings.isFirstStart()) {
            mainLayout.setVisibility(View.GONE);
            showPinCodeDialog("set");
        } else {
            if (appSettings.getPinCode() != 0) {
                mainLayout.setVisibility(View.GONE);
                if (!appSettings.isUseBiometricEntry()) {
                    showPinCodeDialog("enter");
                } else {
                    showBiometricEntryDialog("enter");
                }
            } else {
                openHomePage();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void setScaleMenuItem() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        NavigationBarItemView devices = (NavigationBarItemView) menuView.getChildAt(1);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        devices.setIconSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21f, displayMetrics));
        devices.setItemPaddingTop(27);
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_home) {
                openHomePage();
            } else if (id == R.id.action_add) {
                openAddPage();
            } else if (id == R.id.action_settings) {
                openSettingPage();
            }
            return true;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void openHomePage() {
        recreateHomePage();
    }

    public void openAddPage() {
        int slideIn = currentFragment.equals("HOME") ? R.anim.slide_in_right : R.anim.slide_in_left;
        int slideOut = currentFragment.equals("HOME") ? R.anim.slide_out_right : R.anim.slide_out_left;

        currentFragment = "ADD";
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).setCustomAnimations(
                slideIn,
                R.anim.fade_out,
                R.anim.fade_in,
                slideOut
        ).replace(R.id.fragment_container, addFragment, "ADD").commitNow();
    }

    public void openSettingPage() {
        int slideIn = currentFragment.equals("HOME") || currentFragment.equals("ADD") ? R.anim.slide_in_right : R.anim.slide_in_left;
        int slideOut = currentFragment.equals("HOME") || currentFragment.equals("ADD") ? R.anim.slide_out_right : R.anim.slide_out_left;

        currentFragment = "SETTINGS";
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).setCustomAnimations(
                slideIn,
                R.anim.fade_out,
                R.anim.fade_in,
                slideOut
        ).replace(R.id.fragment_container, settingsFragment, "SETTINGS").commitNow();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != null) {
            switch (currentFragment) {
                case "HOME":
                    if (homeFragment.onBackPressed()) {
                        if (backPressed + 2000 > System.currentTimeMillis()) {
                            this.finishAffinity();
                        } else {
                            if (homeFragment != null) {
                                Toast.makeText(getBaseContext(), getResources().getString(R.string.close_app_title), Toast.LENGTH_SHORT).show();
                            }
                        }
                        backPressed = System.currentTimeMillis();
                    }
                    break;
                case "ADD":
                    if (addFragment.onBackPressed()) openHomePage();
                    break;
                case "SETTINGS":
                    settingsFragment.onBackPressed();
                    break;
            }
        }
    }

    public void showPinCodeDialog(String action) {
        GeneralFunctions.lockOrientation(this);
        DialogsPinCode.showPinCodeLayout(this, action, new DialogAuthCallback() {
            @Override
            public void onSet() {
                if (action.equals("set")) {
                    if (appSettings.isFirstStart()) {
                        uiThreadRealm.executeTransaction(executeTransaction -> appSettings.setFirstStart(false));
                        mainLayout.setVisibility(View.VISIBLE);
                        openHomePage();
                    } else {
                        if (settingsFragment != null && appSettings.getPinCode() != 0) {
                            changePinTitle.setText(R.string.pin_code_change);
                            changePinLayout.setOnClickListener(view1 -> showPinCodeDialog("reset"));
                            settingsFragment.onBiometricSetting();
                        }
                    }
                }

                GeneralFunctions.unlockOrientation(parentActivity);
            }

            @Override
            public void onEnter() {
                mainLayout.setVisibility(View.VISIBLE);

                openHomePage();
                GeneralFunctions.unlockOrientation(parentActivity);
            }
        });
    }

    public void showBiometricEntryDialog(String action) {
        GeneralFunctions.lockOrientation(this);
        DialogsBiometric.showBiometricEntryLayout(this, action, new DialogAuthCallback() {
            @Override
            public void onSet() {

            }

            @Override
            public void onEnter() {
                mainLayout.setVisibility(View.VISIBLE);

                openHomePage();
                GeneralFunctions.unlockOrientation(parentActivity);
            }
        });
    }

    public void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finishAffinity();
    }

    public void recreateHomePage() {
        homeFragment = new HomeFragment();
        homeFragment.parentActivity = this;

        currentFragment = "HOME";
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_left
        ).replace(R.id.fragment_container, homeFragment, "HOME").commitNow();
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int angle;
        switch (rotation) {
            case Surface.ROTATION_90:
                angle = -90;
                break;
            case Surface.ROTATION_180:
                angle = 180;
                break;
            case Surface.ROTATION_270:
                angle = 90;
                break;
            default:
                angle = 0;
                break;
        }

        if (tabLayoutContainer != null)
            tabLayoutContainer.setVisibility(angle == 90 || angle == -90 ? View.GONE : View.VISIBLE);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected void onStart() {
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                biometricEntryStatus = "success";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                biometricEntryStatus = "not_have";
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                biometricEntryStatus = "error";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                biometricEntryStatus = "not_use";
                break;
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (currentFragment.equals("HOME")) recreateHomePage();
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (sheetPopupCamera.getState() == BottomSheetBehavior.STATE_EXPANDED && !restartStream)
            sheetPopupCamera.setState(BottomSheetBehavior.STATE_COLLAPSED);

        super.onStop();
    }

}