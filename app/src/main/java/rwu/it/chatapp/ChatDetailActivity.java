package rwu.it.chatapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText inputMessage;
    private Button btnSend;
    private TextView tvUserName, tvUserStatus;
    private ImageView profileImage;
    private View statusDot;

    private DatabaseReference db, userRef;
    private FirebaseAuth auth;

    private String senderId, receiverId;
    private ArrayList<Message> messagesList;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("chats");

        senderId = auth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");

        rvMessages = findViewById(R.id.rvMessages);
        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        tvUserName = findViewById(R.id.username);
        tvUserStatus = findViewById(R.id.userStatus);
        profileImage = findViewById(R.id.profileImage);
        statusDot = findViewById(R.id.statusDot); // ✅ dot

        messagesList = new ArrayList<>();
        adapter = new MessageAdapter(messagesList, senderId);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String msg = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(msg);
                inputMessage.setText("");
            }
        });

        listenForMessages();
        loadReceiverInfo();
    }

    private void sendMessage(String messageText) {
        String key = db.push().getKey();
        if (key == null) return;

        Message msgObj = new Message(senderId, receiverId, messageText, System.currentTimeMillis());
        msgObj.setDelivered(true);
        db.child(key).setValue(msgObj);
    }

    private void listenForMessages() {
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                if (msg != null) {
                    if ((msg.getSenderId().equals(senderId) && msg.getReceiverId().equals(receiverId)) ||
                            (msg.getSenderId().equals(receiverId) && msg.getReceiverId().equals(senderId))) {

                        messagesList.add(msg);
                        adapter.notifyItemInserted(messagesList.size() - 1);
                        rvMessages.scrollToPosition(messagesList.size() - 1);

                        if (msg.getReceiverId().equals(senderId) && !msg.isSeen()) {
                            snapshot.getRef().child("seen").setValue(true);
                        }
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ✅ User info + status listener
    private void loadReceiverInfo() {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    Long lastSeen = snapshot.child("lastSeen").getValue(Long.class);

                    tvUserName.setText(name != null ? name : "User");

                    if ("online".equals(status)) {
                        tvUserStatus.setText("Online");
                        statusDot.setBackgroundResource(R.drawable.bg_status_dot_green);
                    } else {
                        if (lastSeen != null) {
                            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    .format(new Date(lastSeen));
                            tvUserStatus.setText("Last seen: " + time);
                        } else {
                            tvUserStatus.setText("Offline");
                        }
                        statusDot.setBackgroundResource(R.drawable.bg_status_dot_gray);
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUserStatus("offline");
    }

    private void setUserStatus(String status) {
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("users").child(senderId);

        myRef.child("status").setValue(status);

        if ("offline".equals(status)) {
            myRef.child("lastSeen").setValue(System.currentTimeMillis());
        }
    }
}
