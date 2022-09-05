package ru.oversee;

import io.realm.Realm;
import io.realm.RealmObject;

public class AppSettings extends RealmObject {
    private boolean isFirstStart = true;
    private int pinCode = 0;
    private int gridCount = 3;
    private boolean useBiometricEntry = false;

    public static AppSettings getAppSettings(Realm realm) {
        AppSettings innerApp = realm.where(AppSettings.class).findFirst();
        if (innerApp == null) {
            realm.executeTransaction(transactionRealm -> {
                transactionRealm.insert(new AppSettings());
            });
        }
        return realm.where(AppSettings.class).findFirst();
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public int getGridCount() {
        return gridCount;
    }

    public void setGridCount(int gridCount) {
        this.gridCount = gridCount;
    }

    public boolean isUseBiometricEntry() {
        return useBiometricEntry;
    }

    public void setUseBiometricEntry(boolean useBiometricEntry) {
        this.useBiometricEntry = useBiometricEntry;
    }

    public boolean isFirstStart() {
        return isFirstStart;
    }

    public void setFirstStart(boolean firstStart) {
        isFirstStart = firstStart;
    }
}
