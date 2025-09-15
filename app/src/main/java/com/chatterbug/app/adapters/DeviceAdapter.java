package com.chatterbug.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatterbug.app.R;
import com.chatterbug.app.models.BluetoothDeviceModel;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<BluetoothDeviceModel> deviceList;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDeviceModel device);
    }

    public DeviceAdapter(List<BluetoothDeviceModel> deviceList, OnDeviceClickListener listener) {
        this.deviceList = deviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDeviceModel device = deviceList.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView textDeviceName;
        private TextView textDeviceAddress;
        private ImageView imageDeviceStatus;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            textDeviceName = itemView.findViewById(R.id.text_device_name);
            textDeviceAddress = itemView.findViewById(R.id.text_device_address);
            imageDeviceStatus = itemView.findViewById(R.id.image_device_status);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeviceClick(deviceList.get(position));
                }
            });
        }

        public void bind(BluetoothDeviceModel device) {
            textDeviceName.setText(device.getName());
            textDeviceAddress.setText(device.getAddress());
            
            if (device.isPaired()) {
                imageDeviceStatus.setImageResource(R.drawable.ic_bluetooth_connected);
                imageDeviceStatus.setColorFilter(itemView.getContext().getColor(R.color.success));
            } else {
                imageDeviceStatus.setImageResource(R.drawable.ic_bluetooth);
                imageDeviceStatus.setColorFilter(itemView.getContext().getColor(R.color.primary));
            }
        }
    }
}
