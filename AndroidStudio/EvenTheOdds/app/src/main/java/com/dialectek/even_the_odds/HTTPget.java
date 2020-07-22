// HTTP GET.

package com.dialectek.even_the_odds;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class HTTPget {

    private String mURLname;
    public HttpURLConnection httpConn;
    public Exception exception;

    public HTTPget(String URLname) {
        mURLname = URLname;
        httpConn = null;
        exception = null;
    }

    public int get() {
        int status = -1;

        try {
            URL url = new URL(mURLname);
            httpConn = (HttpURLConnection) url.openConnection();

            status = httpConn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Invalid response from server: " + status);
            }
        } catch (Exception e) {
            exception = e;
        }
        return status;
    }

    public void close() {
        if (httpConn != null) {
            httpConn.disconnect();
        }
    }
}
