package ru.ssau.mobile.lab2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ru.ssau.mobile.lab2.models.Member;

public class SignupActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField1;
    private EditText passwordField2;
    private EditText nameField;
    private EditText positionField;
    private Button createAccountButton;
    private ProgressDialog progressDialog;

    private static final String TAG = "LoginActivity";

    // [START declare_auth]
    private FirebaseAuth auth;
    // [END declare_auth]
    private FirebaseDatabase db;

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTitle(R.string.activity_login_title);
        setContentView(R.layout.activity_signup);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        emailField = (EditText) findViewById(R.id.field_create_email);
        passwordField1 = (EditText) findViewById(R.id.field_create_pass1);
        passwordField2 = (EditText) findViewById(R.id.field_create_pass2);
        nameField = (EditText) findViewById(R.id.field_create_name);
        positionField = (EditText) findViewById(R.id.field_create_position);
        createAccountButton = (Button) findViewById(R.id.button_create_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String pass = intent.getStringExtra("password");
        if (email != null)
            emailField.setText(email);
        if (pass != null)
            passwordField1.setText(pass);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    setUpAccount();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(emailField.getText().toString(),
                        passwordField1.getText().toString());
            }
        });
    }

    private void setUpAccount() {
        showProgressDialog("Setting up your account...");
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Member m = new Member(nameField.getText().toString(),
                    positionField.getText().toString());
            DatabaseReference ref = db.getReference("members");
            ref.child(uid).setValue(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideProgressDialog();
                    Intent intent = new Intent(SignupActivity.this, MeetingsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressDialog();
                    Toast toast = new Toast(getApplicationContext());
                    toast.setText("Failed to connect =(");
                    finish();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog("Creating account...");

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, R.string.toast_acc_not_created,
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to create account: ", task.getException());
                        } else {
                            signIn(emailField.getText().toString(),
                                    passwordField1.getText().toString());
                        }
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        showProgressDialog("Signing in...");

        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(SignupActivity.this, R.string.toast_auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        EditText[] allFields = {emailField, passwordField1, passwordField2, nameField, positionField};

        for (EditText field : allFields) {
            String text = field.getText().toString();
            if (TextUtils.isEmpty(text)) {
                field.setError("Required.");
                valid = false;
            } else {
                field.setError(null);
            }
        }
        if (valid && passwordField1.getText().toString().length() < 6) {
            valid = false;
            passwordField1.setError("At least 6 chars");
        }

        if (valid && !passwordField1.getText().toString().
                equals(passwordField2.getText().toString())) {
            valid = false;
            passwordField2.setError("Doesn't match");
        }

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void showProgressDialog() {
        showProgressDialog(getString(R.string.loading));
    }

    public void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(msg);
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
