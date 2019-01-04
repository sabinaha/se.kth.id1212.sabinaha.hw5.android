package se.kth.id1212.sabinaha.hw5.android.client.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerHandler {
    private final static String TAG = "ServerHandler";

    private Socket mSocket;
    private BufferedReader mReader;
    private BufferedWriter mWriter;

    private String mHost;
    private int mPort;
    private volatile boolean mConnecting;
    private boolean mConnected = false;

    private final static int CONNECTION_TIMEOUT = 2000;

    private ServerCallback mCallback;

    public ServerHandler(String host, int port, ServerCallback sCallback) {
        this.mHost = host;
        this.mPort = port;
        this.mCallback = sCallback;
    }

    public void connect() {
        synchronized (this) {
            if (mConnecting) {
                return;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        mConnecting = true;
                    }
                    mSocket = new Socket();
                    Log.d(TAG, "Trying to connect..");
                    mSocket.connect(new InetSocketAddress(mHost, mPort), CONNECTION_TIMEOUT);
                    Log.d(TAG, "Connected.");
                    mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    new Thread(new ServerListener()).start();
                    mConnected = true;
                    mCallback.notifyConnected();
                    Log.d(TAG, "Connected to " + mSocket);
                } catch (IOException e) {
                    disconnect();
                    Log.d(TAG, "Failed to connect");
                    e.printStackTrace();
                } finally {
                    synchronized (this) {
                        mConnecting = false;
                    }
                }
            }
        }).start();
    }

    public void startGame() {
        sendCommand("start");
    }

    public void quitGame() {
        sendCommand("exit");
        disconnect();
    }

    public void restartGame() {
        sendCommand("restart");
    }

    public void makeGuess(String msg) {
        if (msg.length() == 0)
            return;
        if (msg.length() == 1)
            sendCommand(msg);
        else
            sendCommand("guess " + msg);
    }

    private void sendCommand (String cmd) {
        class MessageSender {
            private String cmd;

            private MessageSender(String cmd) {
                this.cmd = cmd;
            }

            private void send() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mWriter.write(cmd + "\n");
                            Log.d(TAG, "Sending:" + cmd);
                            mWriter.flush();
                            mCallback.messageSent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        new MessageSender(cmd).send();
    }

    private void disconnect() {
        try {
            mSocket.close();
            mConnected = false;
            Log.d(TAG, "Disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mCallback.notifyDisconnect();
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "Server listener started.");
            while (mConnected) {
                try {
                    String received = mReader.readLine();
                    if (received == null) {
                        Log.d(TAG, "NULL RECEIVED, QUITTING");
                        disconnect();
                        return;
                    }
                    mCallback.messageReceived(received);
                    Log.d(TAG, "Server heard something");
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnect();
                }
            }
        }
    }
}

