package rwu.it.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {

    private Button btnLogout, btnExit;
    private RecyclerView rvUsers;
    private ArrayList<User> usersList;
    private UserAdapter adapter;

    private DatabaseReference db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Initialize views
        btnLogout = findViewById(R.id.btnLogout);
        btnExit = findViewById(R.id.btnExit);
        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data
        usersList = new ArrayList<>();
        adapter = new UserAdapter(this, usersList, FirebaseAuth.getInstance().getUid()); // pass currentUserId
        rvUsers.setAdapter(adapter);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = auth.getCurrentUser().getUid();

        loadUsers();

        // Logout button with dialog
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(ChatListActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.child(currentUserId).child("status").setValue("offline")
                                .addOnCompleteListener(task -> {
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(ChatListActivity.this, LoginActivity.class));
                                    finish();
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Exit button
        btnExit.setOnClickListener(v -> {
            db.child(currentUserId).child("status").setValue("offline")
                    .addOnCompleteListener(task -> finishAffinity());
        });
    }

    private void loadUsers() {
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        usersList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        setUserStatus("offline");
    }

    private void setUserStatus(String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("status");
        ref.setValue(status);
    }
}
