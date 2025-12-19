package com.mirea.tyurkinaia.cryptoloader;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyLoader extends AsyncTaskLoader<String> {

    public static final String ARG_WORD = "word";
    public static final String ARG_KEY = "key";

    private byte[] cryptText;
    private byte[] keyBytes;

    public MyLoader(@NonNull Context context, Bundle args) {
        super(context);
        if (args != null) {
            cryptText = args.getByteArray(ARG_WORD);
            keyBytes = args.getByteArray(ARG_KEY);
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        SystemClock.sleep(5000);

        if (cryptText != null && keyBytes != null) {
            SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
            return AESCryptHelper.decryptMsg(cryptText, originalKey);
        }

        return null;
    }
}