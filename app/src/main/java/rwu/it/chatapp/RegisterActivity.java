package rwu.it.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword;
    private Button btnRegister;
    private TextView tvGoLogin;

    private FirebaseAuth auth;
    private DatabaseReference db, tokensDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");
        tokensDb = FirebaseDatabase.getInstance().getReference("Tokens");

        btnRegister.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!validateInput(name, email, password)) return;

            registerUser(name, email, password);
        });

        tvGoLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private boolean validateInput(String name, String email, String password) {
        boolean valid = true;

        if (name.isEmpty()) {
            inputName.setError("Name is required");
            valid = false;
        } else inputName.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("Enter a valid email");
            valid = false;
        } else inputEmail.setError(null);

        if (password.isEmpty() || password.length() < 6) {
            inputPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else inputPassword.setError(null);

        return valid;
    }

    private void registerUser(String name, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();

                        // ✅ Default profileImage empty string
                        // ✅ Default status online
                        User user = new User(uid, name, email, "", "online",System.currentTimeMillis());

                        db.child(uid).setValue(user)
                                .addOnSuccessListener(aVoid -> {

                                    // ✅ Save FCM token
                                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            if (token != null) {
                                                tokensDb.child(uid).setValue(token);
                                            }
                                        }
                                    });

                                    Toast.makeText(RegisterActivity.this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();

                                    // ✅ Move to ChatListActivity
                                    startActivity(new Intent(RegisterActivity.this, ChatListActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "DB Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        if (task.getException() != null && task.getException().getMessage().contains("already in use")) {
                            Toast.makeText(RegisterActivity.this, "Email already registered! Please login.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
