package ru.oversee.MainFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import ru.oversee.R;
import ru.oversee.MainActivity;

public class SettingsFragment extends Fragment {
    public MainActivity parentActivity;
    private View rootView;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchChangeBiometricEntry;
    private ConstraintLayout changeBiometricEntryLayout;

    public SettingsFragment(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return rootView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (parentActivity == null) {
            parentActivity = (MainActivity) getActivity();
            assert parentActivity != null;
            parentActivity.restartApp();
        }

        Spinner camerasInRowSpinner = rootView.findViewById(R.id.cameras_in_row_spinner);
        camerasInRowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String rowValue = camerasInRowSpinner.getSelectedItem().toString();
                parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> parentActivity.appSettings.setGridCount(Integer.parseInt(rowValue)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        int appGridCount = parentActivity.appSettings.getGridCount();
        switch (appGridCount) {
            case 2:
                camerasInRowSpinner.setSelection(0);
                break;
            case 3:
                camerasInRowSpinner.setSelection(1);
                break;
            case 4:
                camerasInRowSpinner.setSelection(2);
                break;
        }
        parentActivity.changePinLayout = rootView.findViewById(R.id.change_pin_layout);
        parentActivity.changePinLayout.setVisibility(View.VISIBLE);

        parentActivity.changePinTitle = null;
        parentActivity.changePinTitle = rootView.findViewById(R.id.change_pin_title);
        if (parentActivity.appSettings.getPinCode() == 0) {
            parentActivity.changePinTitle.setText(R.string.pin_code_set);
            parentActivity.changePinLayout.setOnClickListener(view1 -> parentActivity.showPinCodeDialog("set"));
        } else {
            parentActivity.changePinTitle.setText(R.string.pin_code_change);
            parentActivity.changePinLayout.setOnClickListener(view1 -> parentActivity.showPinCodeDialog("reset"));
        }

        changeBiometricEntryLayout = rootView.findViewById(R.id.change_biometric_layout);
        changeBiometricEntryLayout.setVisibility(View.GONE);

        switchChangeBiometricEntry = rootView.findViewById(R.id.switch_change_biometric);
    }

    public void onBiometricSetting() {
        if (!parentActivity.appSettings.isFirstStart() && parentActivity.appSettings.getPinCode() != 0) {
            if (parentActivity.biometricEntryStatus.equals("success")) {
                changeBiometricEntryLayout.setVisibility(View.VISIBLE);
                switchChangeBiometricEntry.setChecked(parentActivity.appSettings.isUseBiometricEntry());

                switchChangeBiometricEntry.setOnCheckedChangeListener(null);
                switchChangeBiometricEntry.setOnCheckedChangeListener((compoundButton, b) -> {
                    parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> {
                        parentActivity.appSettings.setUseBiometricEntry(b);
                    });
                });
            } else if (parentActivity.biometricEntryStatus.equals("not_use")) {
                switchChangeBiometricEntry.setOnCheckedChangeListener(null);
                switchChangeBiometricEntry.setOnCheckedChangeListener((compoundButton, b) -> {
                    switchChangeBiometricEntry.setChecked(false);
                    Toast.makeText(parentActivity.getBaseContext(), parentActivity.getResources().getString(R.string.biometric_entry_settings_info), Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    @Override
    public void onStart() {
        onBiometricSetting();
        super.onStart();
    }

    public void onBackPressed(){
        parentActivity.openHomePage();
    }
}
