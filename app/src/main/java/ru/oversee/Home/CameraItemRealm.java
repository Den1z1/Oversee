package ru.oversee.Home;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class CameraItemRealm extends RealmObject {
    @PrimaryKey
    @Required
    private String id;
    private String address;
    private String name;
    private int sort;

    public CameraItemRealm() {}

    public static RealmResults<CameraItemRealm> getAll(Realm realm) {
        return realm.where(CameraItemRealm.class).findAll();
    }

    public static CameraItemRealm getCameraById(Realm realm, String id) {
        return realm.where(CameraItemRealm.class).equalTo("id", id).findFirst();
    }

    public static void deleteCamera(Realm realm, String id) {
        realm.executeTransaction(transactionRealm -> {
            CameraItemRealm cameraItemRealm = transactionRealm.where(CameraItemRealm.class).equalTo("id", id).findFirst();
            if (cameraItemRealm != null) cameraItemRealm.deleteFromRealm();
        });
    }

    public static long getCountAll(Realm realm) {
        return realm.where(CameraItemRealm.class).count();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

}
