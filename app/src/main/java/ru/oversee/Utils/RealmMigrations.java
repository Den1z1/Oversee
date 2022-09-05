package ru.oversee.Utils;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class RealmMigrations implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        if (newVersion == 1) {

            try {
                RealmObjectSchema appSettings = schema.get("AppSettings");
                if(appSettings == null) {
                    schema.create("AppSettings");
                }
            } catch (Exception ignored) {}

            try {
                final RealmObjectSchema appSettings = schema.get("AppSettings");
                if (appSettings != null) {
                    appSettings.addField("isFirstStart", boolean.class);
                    appSettings.addField("pinCode", int.class);
                    appSettings.addField("gridCount", int.class);
                    appSettings.addField("useBiometricEntry", boolean.class);
                }
            } catch (Exception ignored) {}

            try {
                RealmObjectSchema cameraItem = schema.get("CameraItemRealm");
                if(cameraItem == null) {
                    schema.create("CameraItemRealm");
                }
            } catch (Exception ignored) {}

            try {
                final RealmObjectSchema cameraItem = schema.get("CameraItemRealm");
                if (cameraItem != null) {
                    cameraItem
                            .addField("id", String.class)
                            .addPrimaryKey("id")
                            .setRequired("id", true);
                    cameraItem.addField("address", String.class);
                    cameraItem.addField("name", String.class);
                    cameraItem.addField("sort", int.class);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public int hashCode() {
        return 39;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof RealmMigrations);
    }
}