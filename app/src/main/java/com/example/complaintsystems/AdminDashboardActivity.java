package com.example.complaintsystems;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.complaintsystems.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    Button CheckBooking, listuser;
    Button btnSignOut;
    FirebaseUser user;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        CheckBooking = (Button) findViewById(R.id.checkbooking);
        CheckBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HistoryAdminActivity.class);
                startActivity(intent);

            }
        });

        btnSignOut = (Button) findViewById(R.id.sigout);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
                finish();
                FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                };
            }
        });

        listuser = (Button) findViewById(R.id.listuser);
        listuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminDashboardActivity.this, ListUserActivity.class));
                finish();
            }
        });

    }
}
