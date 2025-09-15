package com.chatterbug.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chatterbug.app.adapters.MessageAdapter;
import com.chatterbug.app.databinding.ActivityChatBinding;
import com.chatterbug.app.models.Message;
import com.chatterbug.app.services.BluetoothChatService;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements BluetoothChatService.BluetoothChatListener {
    private ActivityChatBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChatService chatService;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String deviceName;
    private String deviceAddress;
    private boolean isPaired;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainHandler = new Handler(Looper.getMainLooper());
        
        getIntentExtras();
        setupToolbar();
        initializeBluetooth();
        setupRecyclerView();
        setupClickListeners();
        initializeChatService();
    }

    private void getIntentExtras() {
        Intent intent = getIntent();
        deviceName = intent.getStringExtra("device_name");
        deviceAddress = intent.getStringExtra("device_address");
        isPaired = intent.getBooleanExtra("is_paired", false);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(deviceName != null ? deviceName : "Unknown Device");
            getSupportActionBar().setSubtitle("Connecting...");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        binding.buttonSend.setOnClickListener(v -> sendMessage());
        
        binding.editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void initializeChatService() {
        chatService = new BluetoothChatService(this, this);
        
        // Connect to the device
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatService.connect(device);
    }

    private void sendMessage() {
        String messageText = binding.editTextMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            if (chatService != null && chatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                chatService.write(messageText.getBytes());
                binding.editTextMessage.setText("");
                
                // Add message to list
                Message message = new Message(messageText, true, System.currentTimeMillis());
                messageList.add(message);
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                binding.recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            } else {
                Toast.makeText(this, "Not connected to device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectionStateChanged(int state) {
        mainHandler.post(() -> {
            switch (state) {
                case BluetoothChatService.STATE_CONNECTING:
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle("Connecting...");
                    }
                    binding.layoutConnectionStatus.setVisibility(View.VISIBLE);
                    binding.textConnectionStatus.setText("Connecting to " + deviceName + "...");
                    break;
                    
                case BluetoothChatService.STATE_CONNECTED:
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle("Connected");
                    }
                    binding.layoutConnectionStatus.setVisibility(View.GONE);
                    binding.layoutMessageInput.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
                    break;
                    
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle("Not connected");
                    }
                    binding.layoutConnectionStatus.setVisibility(View.VISIBLE);
                    binding.textConnectionStatus.setText("Connection lost. Tap to retry.");
                    binding.layoutMessageInput.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onMessageReceived(byte[] buffer, int bytes) {
        String receivedMessage = new String(buffer, 0, bytes);
        
        mainHandler.post(() -> {
            Message message = new Message(receivedMessage, false, System.currentTimeMillis());
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            binding.recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        });
    }

    @Override
    public void onConnectionFailed() {
        mainHandler.post(() -> {
            Toast.makeText(this, "Failed to connect to " + deviceName, Toast.LENGTH_LONG).show();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Connection failed");
            }
            binding.layoutConnectionStatus.setVisibility(View.VISIBLE);
            binding.textConnectionStatus.setText("Connection failed. Tap to retry.");
            binding.layoutMessageInput.setVisibility(View.GONE);
        });
    }

    @Override
    public void onConnectionLost() {
        mainHandler.post(() -> {
            Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle("Connection lost");
            }
            binding.layoutConnectionStatus.setVisibility(View.VISIBLE);
            binding.textConnectionStatus.setText("Connection lost. Tap to retry.");
            binding.layoutMessageInput.setVisibility(View.GONE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatService != null) {
            chatService.stop();
        }
    }
}
