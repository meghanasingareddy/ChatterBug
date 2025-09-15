package com.chatterbug.app.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatService {
    private static final String TAG = "BluetoothChatService";
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private final BluetoothChatListener listener;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;

    public interface BluetoothChatListener {
        void onConnectionStateChanged(int state);
        void onMessageReceived(byte[] buffer, int bytes);
        void onConnectionFailed();
        void onConnectionLost();
    }

    public BluetoothChatService(Context context, BluetoothChatListener listener) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.listener = listener;
        this.handler = new Handler();
        state = STATE_NONE;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.state + " -> " + state);
        this.state = state;
        
        if (listener != null) {
            listener.onConnectionStateChanged(state);
        }
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
        if (listener != null) {
            listener.onConnectionFailed();
        }
    }

    private void connectionLost() {
        setState(STATE_LISTEN);
        if (listener != null) {
            listener.onConnectionLost();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket accept thread started");
            setName("AcceptThread");

            BluetoothSocket socket = null;

            while (state != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "AcceptThread finished");
        }

        public void cancel() {
            Log.d(TAG, "Socket cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "ConnectThread started");
            setName("ConnectThread");

            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothChatService.this) {
                connectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "ConnectedThread started");
            byte[] buffer = new byte[1024];
            int bytes;

            while (state == STATE_CONNECTED) {
                try {
                    bytes = mmInStream.read(buffer);
                    if (listener != null) {
                        listener.onMessageReceived(buffer, bytes);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
