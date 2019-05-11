package it.pgp.squerez.enums;

import android.Manifest;

public enum Permissions {
    // actually, this is the only dangerous permission needed
    WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    ;

    String value;

    Permissions(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}

