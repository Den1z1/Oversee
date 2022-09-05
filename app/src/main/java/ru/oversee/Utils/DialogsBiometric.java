package ru.oversee.Utils;

import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import ru.oversee.R;
import ru.oversee.MainActivity;

public class DialogsBiometric {
    public static void showBiometricEntryLayout(MainActivity parentActivity, String action, DialogAuthCallback callback) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(parentActivity);

        View elemView = View.inflate(parentActivity, R.layout.auth_biometric_layout, null);
        bottomSheetDialog.setContentView(elemView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);

        TextView biometricEntryInfo = elemView.findViewById(R.id.textInfo);
        Button proceedButton = elemView.findViewById(R.id.proceed_button);
        Button cancelButton = elemView.findViewById(R.id.cancel_button);

        if (action.equals("enter")) {
            biometricEntryInfo.setText(R.string.use_biometric_entry);
            proceedButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);

            new CountDownTimer(200, 200) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    BiometricPrompt biometricPrompt = new BiometricPrompt(parentActivity, parentActivity.executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            DialogsPinCode.showPinCodeLayout(parentActivity, action, callback);
                            bottomSheetDialog.dismiss();
                            super.onAuthenticationError(errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            callback.onEnter();
                            bottomSheetDialog.dismiss();
                            super.onAuthenticationSucceeded(result);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                        }
                    });

                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(parentActivity.getString(R.string.enter_biometric_entry))
                            .setDescription(parentActivity.getString(R.string.enter_biometric_entry_info))
                            .setNegativeButtonText(parentActivity.getString(R.string.cancel))
                            .build();

                    biometricPrompt.authenticate(promptInfo);
                }
            }.start();
        } else if (action.equals("set")) {
            biometricEntryInfo.setText(R.string.enter_biometric_entry);
            proceedButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            proceedButton.setOnClickListener(view -> {
                parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> parentActivity.appSettings.setUseBiometricEntry(true));
                callback.onSet();
                bottomSheetDialog.dismiss();
            });
            cancelButton.setOnClickListener(view -> {
                callback.onSet();
                bottomSheetDialog.dismiss();
            });
        }

        final long[] backPressed = {0};

        bottomSheetDialog.setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (backPressed[0] + 2000 > System.currentTimeMillis()) {
                    parentActivity.finishAffinity();
                } else {
                    Toast.makeText(parentActivity.getBaseContext(), parentActivity.getResources().getString(R.string.close_app_title), Toast.LENGTH_SHORT).show();
                }
                backPressed[0] = System.currentTimeMillis();
                return true;
            }
            return false;
        });

        bottomSheetDialog.show();
        Dialogs.setupFullHeight(bottomSheetDialog, null);
    }
}
