package com.iqbal.attendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by iqbal on 09/03/18.
 */

public class SharedPrefManager {

    public static final String SP_KINEST = "spKinest";

    public static final String SP_NAMA = "spNama";
    public static final String SP_EMAIL = "spEmail";
    public static final String SP_ID = "spId";
    public static final String SP_LOGIN = "spHasLogin";
    public static final String SP_PRESENT = "1";
    public static final String SP_TASK = "2";
    public static final String SP_SICK = "3";
    public static final String SP_PERMIT = "4";
    public static final String SP_LEAVE = "5";
    public static final String SP_REMOTE = "6";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public SharedPrefManager(Context context) {
        sp = context.getSharedPreferences(SP_KINEST, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value) {
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value) {
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanHadir(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanTugas(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanSakit(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanIzin(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanCuti(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void saveSPBooleanRemote(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public String getSPPresent() {
        return sp.getString(SP_PRESENT, "1");
    }

    public String getSPTask() {
        return sp.getString(SP_TASK, "2");
    }

    public String getSPSick() {
        return sp.getString(SP_SICK, "3");
    }

    public String getSPPermit() {
        return sp.getString(SP_PERMIT, "4");
    }

    public String getSPLeave() {
        return sp.getString(SP_LEAVE, "5");
    }

    public String getSPRemote() {
        return sp.getString(SP_REMOTE, "6");
    }

    public String getSpNama() {
        return sp.getString(SP_NAMA, "");
    }

    public String getSpId() {
        return sp.getString(SP_ID, "");
    }

    public String getSpEmail() {
        return sp.getString(SP_EMAIL, "");
    }

    public Boolean getSpHasPresent() {
        return sp.getBoolean(SP_PRESENT, false);
    }

    public Boolean getSpHasTask() {
        return sp.getBoolean(SP_TASK, false);
    }

    public Boolean getSpHasSick() {
        return sp.getBoolean(SP_SICK, false);
    }

    public Boolean getSpHasPermit() {
        return sp.getBoolean(SP_PERMIT, false);
    }

    public Boolean getSpHasLeave() {
        return sp.getBoolean(SP_LEAVE, false);
    }

    public Boolean getSpHasRemote() {
        return sp.getBoolean(SP_REMOTE, false);
    }

    public Boolean getSpHasLogin() {
        return sp.getBoolean(SP_LOGIN, false);
    }
}
