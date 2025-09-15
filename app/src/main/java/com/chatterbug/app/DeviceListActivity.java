package com.chatterbug.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chatterbug.app.adapters.DeviceAdapter;
import com.chatterbug.app.databinding.ActivityDeviceListBinding;
import com.chatterbug.app.models.BluetoothDeviceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {
    private ActivityDeviceListBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDeviceModel> deviceList;
    private boolean isDiscovering = false;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(
                        device.getName(),
                        device.getAddress(),
                        device.getBondState() == BluetoothDevice.BOND_BONDED
                    );
                    
                    // Avoid duplicates
                    if (!deviceList.contains(deviceModel)) {
                        deviceList.add(deviceModel);
                        deviceAdapter.notifyItemInserted(deviceList.size() - 1);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                isDiscovering = true;
                binding.swipeRefresh.setRefreshing(true);
                binding.textStatus.setText("Scanning for devices...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                isDiscovering = false;
                binding.swipeRefresh.setRefreshing(false);
                updateStatusText();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        initializeBluetooth();
        setupRecyclerView();
        setupSwipeRefresh();
        loadPairedDevices();
        startDiscovery();
        registerBluetoothReceiver();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Device");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void setupRecyclerView() {
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(deviceList, this);
        binding.recyclerViewDevices.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewDevices.setAdapter(deviceAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDeviceList();
            }
        });
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadPairedDevices() {
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(
                    device.getName() != null ? device.getName() : "Unknown Device",
                    device.getAddress(),
                    true
                );
                deviceList.add(deviceModel);
            }
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void startDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        }
    }

    private void refreshDeviceList() {
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        loadPairedDevices();
        startDiscovery();
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
    }

    private void updateStatusText() {
        if (deviceList.isEmpty()) {
            binding.textStatus.setText("No devices found. Make sure other devices are discoverable.");
            binding.textStatus.setVisibility(View.VISIBLE);
        } else {
            binding.textStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeviceClick(BluetoothDeviceModel device) {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("device_name", device.getName());
        intent.putExtra("device_address", device.getAddress());
        intent.putExtra("is_paired", device.isPaired());
        startActivity(intent);
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
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
    }
}
