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

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private FirebaseAuth auth;
    private DatabaseReference tokensDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        auth = FirebaseAuth.getInstance();
        tokensDb = FirebaseDatabase.getInstance().getReference("Tokens");

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!validateInput(email, password)) return;

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();

                            // ✅ Update FCM token after login
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String token = tokenTask.getResult();
                                            if (token != null) {
                                                tokensDb.child(uid).setValue(token);
                                            }
                                        }
                                    });

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvGoRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private boolean validateInput(String email, String password) {
        boolean valid = true;

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
}
