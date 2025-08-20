package rwu.it.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Message> messagesList;
    private String currentUserId;

    public MessageAdapter(ArrayList<Message> messagesList, String currentUserId) {
        this.messagesList = messagesList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getSenderId().equals(currentUserId)) {
            return 1; // Sent
        } else {
            return 2; // Received
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messagesList.get(position);

        // Message text set karo
        holder.txtMessage.setText(message.getMessage());

        // Time format karo
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = sdf.format(new Date(message.getTimestamp()));
        holder.txtTime.setText(time);

        // Agar message current user ne bheja hai to status show karo
        if (message.getSenderId().equals(currentUserId)) {
            if (holder.tvStatus != null) {
                holder.tvStatus.setVisibility(View.VISIBLE);

                if (message.isSeen()) {
                    holder.tvStatus.setText("Seen");
                } else if (message.isDelivered()) {
                    holder.tvStatus.setText("Delivered");
                } else {
                    holder.tvStatus.setText("Sending...");
                }
            } else {

                holder.tvStatus.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime, tvStatus ,userStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            userStatus=itemView.findViewById(R.id.userStatus);
        }
    }
}
