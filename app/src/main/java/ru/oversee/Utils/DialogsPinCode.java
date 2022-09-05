package ru.oversee.Utils;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import ru.oversee.R;
import ru.oversee.AppSettings;
import ru.oversee.MainActivity;

public class DialogsPinCode {
    @SuppressLint("ClickableViewAccessibility")
    public static void showPinCodeLayout(MainActivity parentActivity, String action, DialogAuthCallback callback) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(parentActivity);

        View elemView = View.inflate(parentActivity, R.layout.auth_pin_code_layout, null);
        bottomSheetDialog.setContentView(elemView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);

        TextSwitcher textSwitcherPassInfo = elemView.findViewById(R.id.textSwitcher);
        String[] textToShow = {
                parentActivity.getString(R.string.enter_password),
                parentActivity.getString(R.string.entry_again_password),
                parentActivity.getString(R.string.entry_old_password)
        };
        final int[] currentText = {action.equals("reset") ? 2 : 0};

        textSwitcherPassInfo.setText(textToShow[currentText[0]]);
        textSwitcherPassInfo.setInAnimation(parentActivity, android.R.anim.slide_in_left);
        textSwitcherPassInfo.setOutAnimation(parentActivity, android.R.anim.slide_out_right);

        ImageView firstNumbersCircle = elemView.findViewById(R.id.first_number_circle);
        ImageView secondNumbersCircle = elemView.findViewById(R.id.second_number_circle);
        ImageView thirdNumbersCircle = elemView.findViewById(R.id.third_number_circle);
        ImageView fourthNumbersCircle = elemView.findViewById(R.id.fourth_number_circle);

        ArrayList<ImageView> numbersCircles = new ArrayList<>();
        numbersCircles.add(firstNumbersCircle);
        numbersCircles.add(secondNumbersCircle);
        numbersCircles.add(thirdNumbersCircle);
        numbersCircles.add(fourthNumbersCircle);

        TextView numberButton1 = elemView.findViewById(R.id.number_button_1);
        TextView numberButton2 = elemView.findViewById(R.id.number_button_2);
        TextView numberButton3 = elemView.findViewById(R.id.number_button_3);
        TextView numberButton4 = elemView.findViewById(R.id.number_button_4);
        TextView numberButton5 = elemView.findViewById(R.id.number_button_5);
        TextView numberButton6 = elemView.findViewById(R.id.number_button_6);
        TextView numberButton7 = elemView.findViewById(R.id.number_button_7);
        TextView numberButton8 = elemView.findViewById(R.id.number_button_8);
        TextView numberButton9 = elemView.findViewById(R.id.number_button_9);
        TextView numberButton0 = elemView.findViewById(R.id.number_button_0);

        ArrayList<TextView> numberButtons = new ArrayList<>();
        numberButtons.add(numberButton1);
        numberButtons.add(numberButton2);
        numberButtons.add(numberButton3);
        numberButtons.add(numberButton4);
        numberButtons.add(numberButton5);
        numberButtons.add(numberButton6);
        numberButtons.add(numberButton7);
        numberButtons.add(numberButton8);
        numberButtons.add(numberButton9);
        numberButtons.add(numberButton0);

        final String[] pinCode = {"", "", ""};

        ImageView buttonDeleteNumber = elemView.findViewById(R.id.button_delete_text);
        buttonDeleteNumber.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    PinCodeAnimations.clickAnimationNumbers(buttonDeleteNumber, true);
                    GeneralFunctions.setVibration(parentActivity, 100);
                    break;
                case MotionEvent.ACTION_UP:
                    PinCodeAnimations.clickAnimationNumbers(buttonDeleteNumber, false);

                    if (pinCode[1].length() != 0) {
                        pinCode[1] = pinCode[1].substring(0, pinCode[1].length() - 1);
                        numbersCircles.get(pinCode[1].length()).setBackgroundTintList(ColorStateList.valueOf(parentActivity.getColor(R.color.whiteTransparent1)));
                    } else if (pinCode[0].length() != 0) {
                        pinCode[0] = pinCode[0].substring(0, pinCode[0].length() - 1);
                        numbersCircles.get(pinCode[0].length()).setBackgroundTintList(ColorStateList.valueOf(parentActivity.getColor(R.color.whiteTransparent1)));
                    } else if (pinCode[2].length() != 0) {
                        pinCode[2] = pinCode[2].substring(0, pinCode[2].length() - 1);
                        numbersCircles.get(pinCode[2].length()).setBackgroundTintList(ColorStateList.valueOf(parentActivity.getColor(R.color.whiteTransparent1)));
                    }
                    break;
            }
            return true;
        });

        buttonDeleteNumber.setOnClickListener(view -> {
            if (pinCode[1].length() != 0) {
                pinCode[1] = pinCode[1].substring(0, pinCode[1].length() - 1);
                numbersCircles.get(pinCode[1].length()).setBackgroundTintList(ColorStateList.valueOf(parentActivity.getColor(R.color.whiteTransparent1)));
            } else if (pinCode[0].length() != 0) {
                pinCode[0] = pinCode[0].substring(0, pinCode[0].length() - 1);
                numbersCircles.get(pinCode[0].length()).setBackgroundTintList(ColorStateList.valueOf(parentActivity.getColor(R.color.whiteTransparent1)));
            }
        });

        TextView textButton = elemView.findViewById(R.id.text_button);
        textButton.setText(action.equals("set") ? R.string.no_pin_code : R.string.out);
        textButton.setOnClickListener(view -> {
            if (action.equals("set") && !parentActivity.currentFragment.equals("APP_SETTINGS")) {
                pinCodeSetComeBack(
                        parentActivity,
                        bottomSheetDialog,
                        pinCode,
                        currentText,
                        textToShow,
                        numbersCircles,
                        numberButtons,
                        textSwitcherPassInfo,
                        textButton,
                        callback
                );
            } else if (action.equals("enter")) {
                pinCodeEnterComeBack(parentActivity, bottomSheetDialog, null);
            } else {
                bottomSheetDialog.dismiss();
            }
        });

        for (int i = 0; i < numberButtons.size(); i++) {
            TextView numberButton = numberButtons.get(i);
            numberButton.setOnTouchListener((view, motionEvent) -> {
                switch (action) {
                    case "enter":
                        if (pinCode[0].length() != 4)
                            pinCode[0] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[0], numbersCircles);

                        if (pinCode[0].length() == 4)
                            checkPinCode(
                                    parentActivity,
                                    action,
                                    pinCode[0],
                                    "",
                                    numbersCircles,
                                    numberButtons,
                                    new CheckPinCallback() {
                                        @Override
                                        public void onComplete(int value) {
                                            callback.onEnter();
                                            parentActivity.loginAttempt = 0;
                                            bottomSheetDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancel() {
                                            if (parentActivity.loginAttempt != 5) {
                                                parentActivity.loginAttempt++;
                                            } else {
                                                parentActivity.loginAttempt = 0;
                                                Dialogs.showInfoDialog(parentActivity,
                                                        parentActivity.getResources().getString(R.string.pin_error_header),
                                                        parentActivity.getResources().getString(R.string.pin_error_info),
                                                        () -> {
                                                            parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> {
                                                                parentActivity.appSettings.setPinCode(0);
                                                                parentActivity.appSettings.setUseBiometricEntry(false);
                                                                parentActivity.appSettings.setFirstStart(true);
                                                            });
                                                            parentActivity.showPinCodeDialog("set");
                                                            bottomSheetDialog.dismiss();
                                                        });
                                            }
                                            pinCode[0] = "";
                                        }
                                    }
                            );
                        break;

                    case "set":
                        if (pinCode[0].length() == 4) {
                            if (pinCode[1].length() != 4)
                                pinCode[1] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[1], numbersCircles);

                            if (pinCode[1].length() == 4)
                                checkPinCode(
                                        parentActivity,
                                        action,
                                        pinCode[0],
                                        pinCode[1],
                                        numbersCircles,
                                        numberButtons,
                                        new CheckPinCallback() {
                                            @Override
                                            public void onComplete(int value) {
                                                parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> parentActivity.appSettings.setPinCode(value));
                                                if (parentActivity.biometricEntryStatus.equals("success")) {
                                                    DialogsBiometric.showBiometricEntryLayout(parentActivity, action, callback);
                                                } else {
                                                    callback.onSet();
                                                }
                                                bottomSheetDialog.dismiss();
                                            }

                                            @Override
                                            public void onCancel() {
                                                pinCode[1] = "";
                                            }
                                        }
                                );
                        }

                        if (pinCode[0].length() != 4)
                            pinCode[0] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[0], numbersCircles);

                        if (pinCode[0].length() == 4) {
                            if (pinCode[1].length() == 0) {
                                if (currentText[0] != 1) {
                                    currentText[0] = 1;
                                    textSwitcherPassInfo.setText(textToShow[currentText[0]]);
                                    PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);
                                }

                                textButton.setText(R.string.back);
                                textButton.setOnClickListener(null);
                                textButton.setOnClickListener(view1 -> pinCodeSetComeBack(
                                        parentActivity,
                                        bottomSheetDialog,
                                        pinCode,
                                        currentText,
                                        textToShow,
                                        numbersCircles,
                                        numberButtons,
                                        textSwitcherPassInfo,
                                        textButton,
                                        callback
                                ));
                            }
                        }
                        break;

                    case "reset":
                        if (pinCode[2].length() != 4) {
                            pinCode[2] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[2], numbersCircles);
                            if (pinCode[2].length() == 4)
                                checkPinCode(
                                        parentActivity,
                                        action,
                                        pinCode[2],
                                        "",
                                        numbersCircles,
                                        numberButtons,
                                        new CheckPinCallback() {
                                            @Override
                                            public void onComplete(int value) {
                                                textButton.setText(R.string.back);
                                                PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons,true);

                                                currentText[0] = 0;
                                                textSwitcherPassInfo.setText(textToShow[currentText[0]]);

                                                textButton.setOnClickListener(null);
                                                textButton.setOnClickListener(view1 -> pinCodeResetComeBack(
                                                        bottomSheetDialog,
                                                        pinCode,
                                                        currentText,
                                                        textToShow,
                                                        numbersCircles,
                                                        numberButtons,
                                                        textSwitcherPassInfo,
                                                        textButton
                                                ));
                                            }

                                            @Override
                                            public void onCancel() {
                                                pinCode[2] = "";
                                            }
                                        }
                                );
                        } else {
                            if (pinCode[0].length() == 4) {
                                if (pinCode[1].length() != 4)
                                    pinCode[1] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[1], numbersCircles);

                                if (pinCode[1].length() == 4) {
                                    checkPinCode(
                                            parentActivity,
                                            "set",
                                            pinCode[0],
                                            pinCode[1],
                                            numbersCircles,
                                            numberButtons,
                                            new CheckPinCallback() {
                                                @Override
                                                public void onComplete(int value) {
                                                    parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> parentActivity.appSettings.setPinCode(value));
                                                    callback.onSet();
                                                    bottomSheetDialog.dismiss();
                                                }

                                                @Override
                                                public void onCancel() {
                                                    pinCode[1] = "";
                                                }
                                            }
                                    );
                                }
                            }

                            if (pinCode[0].length() != 4)
                                pinCode[0] = setClickNumberButton(parentActivity, view, motionEvent, numberButton, pinCode[0], numbersCircles);

                            if (pinCode[0].length() == 4) {
                                if (pinCode[1].length() == 0) {
                                    if (currentText[0] != 1) {
                                        currentText[0] = 1;
                                        textSwitcherPassInfo.setText(textToShow[currentText[0]]);
                                        PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);
                                    }

                                    textButton.setOnClickListener(null);
                                    textButton.setOnClickListener(view1 -> {
                                        pinCodeResetComeBack(
                                                bottomSheetDialog,
                                                pinCode,
                                                currentText,
                                                textToShow,
                                                numbersCircles,
                                                numberButtons,
                                                textSwitcherPassInfo,
                                                textButton
                                        );
                                    });
                                }
                            }
                        }
                        break;
                }
                return true;
            });
        }

        final long[] backPressed = {0};

        bottomSheetDialog.setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                if (parentActivity.appSettings.isFirstStart() || action.equals("enter")) {
                    if (action.equals("set")) {
                        pinCodeSetComeBack(
                                parentActivity,
                                bottomSheetDialog,
                                pinCode,
                                currentText,
                                textToShow,
                                numbersCircles,
                                numberButtons,
                                textSwitcherPassInfo,
                                textButton,
                                callback
                        );
                    } else {
                        pinCodeEnterComeBack(parentActivity, bottomSheetDialog, backPressed);
                    }
                } else {
                    pinCodeResetComeBack(
                            bottomSheetDialog,
                            pinCode,
                            currentText,
                            textToShow,
                            numbersCircles,
                            numberButtons,
                            textSwitcherPassInfo,
                            textButton
                    );
                }
                return true;
            }
            return false;
        });

        bottomSheetDialog.show();
        Dialogs.setupFullHeight(bottomSheetDialog, null);
    }

    private static void pinCodeEnterComeBack(MainActivity parentActivity, BottomSheetDialog bottomSheetDialog, long[] backPressed) {
        if (backPressed == null) {
            Dialogs.showActionDialog(
                    parentActivity,
                    parentActivity.getString(R.string.exit_auth_header),
                    parentActivity.getString(R.string.exit_auth_info),
                    parentActivity.getString(R.string.out),
                    () -> {
                        parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> {
                            parentActivity.appSettings.setPinCode(0);
                            parentActivity.appSettings.setUseBiometricEntry(false);
                            parentActivity.appSettings.setFirstStart(true);
                        });
                        parentActivity.showPinCodeDialog("set");
                        bottomSheetDialog.dismiss();
                    }
            );
        } else {
            if (backPressed[0] + 2000 > System.currentTimeMillis()) {
                parentActivity.finishAffinity();
            } else {
                Toast.makeText(parentActivity.getBaseContext(), parentActivity.getResources().getString(R.string.close_app_title), Toast.LENGTH_SHORT).show();
            }
            backPressed[0] = System.currentTimeMillis();
        }
    }

    private static void pinCodeSetComeBack(
            MainActivity parentActivity,
            BottomSheetDialog bottomSheetDialog,
            String[] pinCode,
            int[] currentText,
            String[] textToShow,
            ArrayList<ImageView> numbersCircles,
            ArrayList<TextView> numberButtons,
            TextSwitcher textSwitcherPassInfo,
            TextView textButton,
            DialogAuthCallback callback
    ) {
        if (pinCode[0].length() == 4) {
            currentText[0] = 0;
            textSwitcherPassInfo.setText(textToShow[currentText[0]]);
            PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);

            pinCode[0] = "";
            pinCode[1] = "";

            textButton.setText(R.string.no_pin_code);
            textButton.setOnClickListener(null);
            textButton.setOnClickListener(view2 -> pinCodeSetComeBack(
                    parentActivity,
                    bottomSheetDialog,
                    pinCode,
                    currentText,
                    textToShow,
                    numbersCircles,
                    numberButtons,
                    textSwitcherPassInfo,
                    textButton,
                    callback
            ));
        } else {
            Dialogs.showActionDialog(
                    parentActivity,
                    parentActivity.getString(R.string.no_pin_title),
                    parentActivity.getString(R.string.no_pin_info),
                    parentActivity.getString(R.string.no_pin_code),
                    () -> {
                        parentActivity.uiThreadRealm.executeTransaction(executeTransaction -> parentActivity.appSettings.setPinCode(0));
                        callback.onSet();
                        bottomSheetDialog.dismiss();
                    }
            );
        }
    }

    private static void pinCodeResetComeBack(
            BottomSheetDialog bottomSheetDialog,
            String[] pinCode,
            int[] currentText,
            String[] textToShow,
            ArrayList<ImageView> numbersCircles,
            ArrayList<TextView> numberButtons,
            TextSwitcher textSwitcherPassInfo,
            TextView textButton
    ) {
        if (pinCode[2].length() == 4 && pinCode[0].length() != 4) {
            PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);

            currentText[0] = 2;
            textSwitcherPassInfo.setText(textToShow[currentText[0]]);

            textButton.setText(R.string.out);
            textButton.setOnClickListener(null);
            textButton.setOnClickListener(view2 -> {
                textButton.setText(R.string.out);
                bottomSheetDialog.dismiss();
            });

            pinCode[0] = "";
            pinCode[2] = "";
        } else if (pinCode[0].length() == 4){
            currentText[0] = 0;
            textSwitcherPassInfo.setText(textToShow[currentText[0]]);
            PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);

            textButton.setText(R.string.back);
            textButton.setOnClickListener(null);
            textButton.setOnClickListener(view2 -> {
                textButton.setText(R.string.out);
                currentText[0] = 2;
                textSwitcherPassInfo.setText(textToShow[currentText[0]]);
                PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, false);

                pinCode[0] = "";
                pinCode[2] = "";

                textButton.setOnClickListener(view1 -> bottomSheetDialog.dismiss());
            });

            pinCode[1] = "";
            pinCode[0] = "";
        } else {
            bottomSheetDialog.dismiss();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private static String setClickNumberButton(
            MainActivity parentActivity,
            View view,
            MotionEvent motionEvent,
            TextView button,
            String pinCode,
            ArrayList<ImageView> numbersCircles
    ) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                PinCodeAnimations.clickAnimationNumbers(button, true);
                GeneralFunctions.setVibration(parentActivity, 100);
                break;
            case MotionEvent.ACTION_UP:
                PinCodeAnimations.clickAnimationNumbers(button, false);

                switch (pinCode.length()) {
                    case 0:
                        PinCodeAnimations.clickAnimationNumbersCircles(numbersCircles.get(0), parentActivity.getColor(R.color.white));
                        pinCode = pinCode + button.getText().toString();
                        break;
                    case 1:
                        PinCodeAnimations.clickAnimationNumbersCircles(numbersCircles.get(1), parentActivity.getColor(R.color.white));
                        pinCode = pinCode + button.getText().toString();
                        break;
                    case 2:
                        PinCodeAnimations.clickAnimationNumbersCircles(numbersCircles.get(2), parentActivity.getColor(R.color.white));
                        pinCode = pinCode + button.getText().toString();
                        break;
                    case 3:
                        PinCodeAnimations.clickAnimationNumbersCircles(numbersCircles.get(3), parentActivity.getColor(R.color.white));
                        pinCode = pinCode + button.getText().toString();
                        break;
                }
                break;
        }
        view.onTouchEvent(motionEvent);
        return pinCode;
    }

    private static void checkPinCode(
            MainActivity parentActivity,
            String action,
            String pinCode,
            String secondPin,
            ArrayList<ImageView> numbersCircles,
            ArrayList<TextView> numberButtons,
            CheckPinCallback callback
    ) {
        int value = !action.equals("set") ? AppSettings.getAppSettings(parentActivity.uiThreadRealm).getPinCode() : Integer.parseInt(secondPin);

        for (int i = 0; i < numberButtons.size(); i++) numberButtons.get(i).setEnabled(false);

        if (value == Integer.parseInt(pinCode)) {
            for (int i = 0; i < numbersCircles.size(); i++)
                PinCodeAnimations.accessPinAnimation(numbersCircles.get(i), parentActivity.getColor(R.color.green1));

            new CountDownTimer(650, 650) {
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    callback.onComplete(value);
                    PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, true);
                }
            }.start();
        } else {
            new CountDownTimer(500, 500) {
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    GeneralFunctions.setVibration(parentActivity, 500);

                    for (int i = 0; i < numbersCircles.size(); i++)
                        PinCodeAnimations.clickAnimationNumbersCircles(numbersCircles.get(i), parentActivity.getColor(R.color.red3));

                    new CountDownTimer(500, 500) {
                        @Override
                        public void onTick(long l) {}

                        @Override
                        public void onFinish() {
                            callback.onCancel();
                            PinCodeAnimations.setDefaultNumbersCircles(numbersCircles, numberButtons, true);
                        }
                    }.start();
                }
            }.start();
        }
    }
}
