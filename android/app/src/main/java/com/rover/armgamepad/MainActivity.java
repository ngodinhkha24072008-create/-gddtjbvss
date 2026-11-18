package com.rover.armgamepad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows the landscape gamepad (assets/index.html) full screen and sends its
 * single-character commands to an HC-05 / HC-06 over classic Bluetooth serial.
 */
public class MainActivity extends Activity {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BT_PERMISSION = 1;

    private WebView web;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private volatile BluetoothSocket socket;
    private volatile OutputStream out;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(0xFF0D1220);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Bridge(), "Robot");
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
        hideSystemBars();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Safety: stop the car and arm playback when the app goes to the background
        write("D");
    }

    @Override
    protected void onDestroy() {
        io.execute(this::closeSocket);
        io.shutdown();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    // ---------- Bluetooth helpers ----------

    private BluetoothAdapter adapter() {
        BluetoothManager m = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        return m == null ? null : m.getAdapter();
    }

    private boolean hasBtPermission() {
        return Build.VERSION.SDK_INT < 31
                || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_BT_PERMISSION) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            js("window.onRobotPermission && onRobotPermission(" + granted + ")");
        }
    }

    @SuppressLint("MissingPermission")
    private void doConnect(String address, String name) {
        closeSocket();
        status("connecting", name);
        BluetoothAdapter a = adapter();
        if (a == null || !hasBtPermission()) {
            status("error", "Bluetooth isn't available or the permission was refused.");
            return;
        }
        BluetoothDevice device;
        try {
            device = a.getRemoteDevice(address);
        } catch (IllegalArgumentException e) {
            status("error", "That device address isn't valid.");
            return;
        }

        BluetoothSocket s = null;
        try {
            s = device.createRfcommSocketToServiceRecord(SPP_UUID);
            s.connect();
        } catch (IOException | SecurityException first) {
            closeQuietly(s);
            s = null;
            try {
                // Fallback many HC-05 / HC-06 modules need: RFCOMM channel 1
                Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                s = (BluetoothSocket) m.invoke(device, 1);
                s.connect();
            } catch (Exception second) {
                closeQuietly(s);
                status("error", "Couldn't connect to " + name + ". Check the robot is switched on and nearby.");
                return;
            }
        }

        try {
            out = s.getOutputStream();
            socket = s;
        } catch (IOException e) {
            closeQuietly(s);
            status("error", "Connected, but couldn't open the data stream.");
            return;
        }
        status("connected", name);
        startReader(s);
    }

    /** Reads lines the Arduino prints (e.g. "Pos 1 Saved") and shows them in the app. */
    private void startReader(final BluetoothSocket s) {
        Thread t = new Thread(() -> {
            StringBuilder line = new StringBuilder();
            byte[] buf = new byte[256];
            try {
                InputStream in = s.getInputStream();
                while (true) {
                    int n = in.read(buf);
                    if (n < 0) break;
                    for (int i = 0; i < n; i++) {
                        char c = (char) (buf[i] & 0xFF);
                        if (c == '\n') {
                            final String text = line.toString().trim();
                            line.setLength(0);
                            if (!text.isEmpty()) js("window.onRobotMessage && onRobotMessage(" + JSONObject.quote(text) + ")");
                        } else if (c != '\r' && line.length() < 200) {
                            line.append(c);
                        }
                    }
                }
            } catch (IOException ignored) {
                // socket closed
            }
            if (socket == s) {
                io.execute(() -> {
                    if (socket == s) {
                        closeSocket();
                        status("disconnected", "The robot disconnected.");
                    }
                });
            }
        }, "bt-reader");
        t.setDaemon(true);
        t.start();
    }

    private void write(final String text) {
        if (io.isShutdown()) return;
        io.execute(() -> {
            OutputStream o = out;
            if (o == null) return;
            try {
                o.write(text.getBytes(StandardCharsets.US_ASCII));
                o.flush();
            } catch (IOException e) {
                closeSocket();
                status("disconnected", "Lost the connection to the robot.");
            }
        });
    }

    private void closeSocket() {
        BluetoothSocket s = socket;
        socket = null;
        out = null;
        closeQuietly(s);
    }

    private static void closeQuietly(BluetoothSocket s) {
        if (s == null) return;
        try {
            s.close();
        } catch (IOException ignored) {
        }
    }

    private void status(String state, String message) {
        js("window.onRobotStatus && onRobotStatus(" + JSONObject.quote(state) + "," + JSONObject.quote(message) + ")");
    }

    private void js(final String code) {
        main.post(() -> {
            if (web != null) web.evaluateJavascript(code, null);
        });
    }

    // ---------- Methods the gamepad page calls as window.Robot.* ----------

    private class Bridge {

        @JavascriptInterface
        public boolean hasPermission() {
            return hasBtPermission();
        }

        @JavascriptInterface
        public void requestPermission() {
            main.post(() -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BT_PERMISSION);
                } else {
                    js("window.onRobotPermission && onRobotPermission(true)");
                }
            });
        }

        @JavascriptInterface
        public boolean bluetoothOn() {
            BluetoothAdapter a = adapter();
            return a != null && a.isEnabled();
        }

        @SuppressLint("MissingPermission")
        @JavascriptInterface
        public void enableBluetooth() {
            main.post(() -> {
                try {
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                } catch (Exception e) {
                    openSettings();
                }
            });
        }

        @JavascriptInterface
        public void openBluetoothSettings() {
            main.post(MainActivity.this::openSettings);
        }

        @SuppressLint("MissingPermission")
        @JavascriptInterface
        public String pairedDevices() {
            JSONArray list = new JSONArray();
            BluetoothAdapter a = adapter();
            if (a == null || !hasBtPermission()) return list.toString();
            try {
                Set<BluetoothDevice> bonded = a.getBondedDevices();
                if (bonded != null) {
                    for (BluetoothDevice d : bonded) {
                        JSONObject o = new JSONObject();
                        o.put("name", d.getName() == null ? "" : d.getName());
                        o.put("address", d.getAddress());
                        list.put(o);
                    }
                }
            } catch (Exception ignored) {
            }
            return list.toString();
        }

        @JavascriptInterface
        public void connect(final String address, final String name) {
            io.execute(() -> doConnect(address, name));
        }

        @JavascriptInterface
        public void send(String text) {
            if (text != null && !text.isEmpty()) write(text);
        }

        @JavascriptInterface
        public void disconnect() {
            io.execute(() -> {
                closeSocket();
                status("disconnected", "");
            });
        }
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        } catch (Exception ignored) {
        }
    }
}
