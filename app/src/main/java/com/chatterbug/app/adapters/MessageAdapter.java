package com.chatterbug.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatterbug.app.R;
import com.chatterbug.app.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private List<Message> messageList;
    private SimpleDateFormat timeFormat;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList == null || position < 0 || position >= messageList.size()) {
            return VIEW_TYPE_RECEIVED; // Default fallback
        }
        Message message = messageList.get(position);
        return (message != null && message.isSent()) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (messageList == null || position < 0 || position >= messageList.size()) {
            return; // Safety check
        }
        
        Message message = messageList.get(position);
        if (message == null) {
            return; // Safety check
        }
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
        }

        public void bind(Message message) {
            if (message == null) return;
            
            textMessage.setText(message.getContent() != null ? message.getContent() : "");
            textTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTime = itemView.findViewById(R.id.text_time);
        }

        public void bind(Message message) {
            if (message == null) return;
            
            textMessage.setText(message.getContent() != null ? message.getContent() : "");
            textTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }
}
