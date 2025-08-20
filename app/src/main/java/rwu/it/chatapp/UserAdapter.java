package rwu.it.chatapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> usersList;
    private Context context;
    private String currentUserId;

    public UserAdapter(Context context, ArrayList<User> usersList, String currentUserId) {
        this.context = context;
        this.usersList = usersList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = usersList.get(position);

        holder.txtName.setText(user.getName());

        // ✅ Profile image
        Glide.with(context)
                .load(user.getProfileImage())
                .apply(new RequestOptions().placeholder(R.drawable.ic_person).circleCrop())
                .into(holder.imgProfile);

        // ✅ Load last message instead of status
        loadLastMessage(user.getUserId(), holder.txtLastMessage);

        // Click to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("receiverId", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtLastMessage;
        ImageView imgProfile;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.username);
            txtLastMessage = itemView.findViewById(R.id.tvLastMessage); // reuse TextView for last message
            imgProfile = itemView.findViewById(R.id.profileImage);

        }
    }

    private void loadLastMessage(String otherUserId, TextView txtLastMessage) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chats");
        ref.addValueEventListener(new ValueEventListener() {
            String lastMessage = "No messages yet";

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Chat chat = data.getValue(Chat.class);
                    if (chat != null) {
                        if ((chat.getSenderId().equals(currentUserId) && chat.getReceiverId().equals(otherUserId)) ||
                                (chat.getSenderId().equals(otherUserId) && chat.getReceiverId().equals(currentUserId))) {
                            lastMessage = chat.getMessage();
                        }
                    }
                }
                txtLastMessage.setText(lastMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
