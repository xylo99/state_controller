package com.example.pc_state_controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    Button reboot, shutdown;
    Toast transmitFailure;
    private BufferedReader inputBuffer;
    private DataOutputStream outputBuffer;
    private Socket sock;
    private String connectionSucceeded, connectionFailed, transmitFailed,
            r = "r", s = "s"; // r = reset, s = shutdown
    private boolean connected = false;

    private boolean connect() {
        String IP = "192.168.X.X"; // replace with ip.
        int port = 9999;

        try {
            sock = new Socket(IP, port);
            sock.setSoTimeout(3600);
            inputBuffer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            /* Great! We've successfully connected. */
            connectionSucceeded = "Connection Success! Remote is ready for commands";
            connected = true;
            return true;
        } catch (IOException ioe) {
            connectionFailed = "Connection failed. Reason: " + ioe.getMessage();
        }
        /* Oops. The connection failed. */
        connected = false;
        return false;
    }

    private class OpenConnection extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            connect();
            return null;
        }
    }

    private class CloseConnection extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class Reset extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            try {
                outputBuffer = new DataOutputStream(sock.getOutputStream());
                outputBuffer.writeBytes(r);
                outputBuffer.flush();
            } catch (IOException e) {
                transmitFailed = "Couldn't send reboot signal. Reason: " + e.getMessage();
                runOnUiThread(new Runnable() {
                    public void run() {
                        transmitFailure = Toast.makeText(getApplicationContext(), transmitFailed,
                                Toast.LENGTH_SHORT);
                        transmitFailure.show();
                    }
                });
            }
            return null;
        }
    }

    private class ShutDown extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            try {
                outputBuffer = new DataOutputStream(sock.getOutputStream());
                outputBuffer.writeBytes(s);
                outputBuffer.flush();
            } catch (IOException e) {
                transmitFailed = "Couldn't send shutdown signal. Reason: " + e.getMessage();
                runOnUiThread(new Runnable() {
                    public void run() {
                        transmitFailure = Toast.makeText(getApplicationContext(), transmitFailed,
                                Toast.LENGTH_SHORT);
                        transmitFailure.show();
                    }
                });
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reboot = findViewById(R.id.btn1);
        shutdown = findViewById(R.id.btn2);
        new OpenConnection().execute();
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    try {
                        new Reset().execute();
                    } catch (Exception e) {
                        transmitFailed = "Couldn't send reboot signal. Reason: " + e.getMessage();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                transmitFailure = Toast.makeText(getApplicationContext(),
                                        transmitFailed, Toast.LENGTH_SHORT);
                                transmitFailure.show();
                            }
                        });
                    }
                }
            }
        });
        shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    try {
                        new ShutDown().execute();
                    } catch (Exception e) {
                        transmitFailed = "Couldn't send shutdown signal. signal. Reason: "
                                + e.getMessage();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                transmitFailure = Toast.makeText(getApplicationContext(),
                                        transmitFailed, Toast.LENGTH_SHORT);
                                transmitFailure.show();
                            }
                        });
                    }
                }
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        new CloseConnection().execute();
    }
}
