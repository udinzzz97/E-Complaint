package com.example.complaintsystems;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    TextView username,lastname,firstname,phone,email;

    Button update;

    DatabaseReference ref;
    private FirebaseAuth auth;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ref = FirebaseDatabase.getInstance().getReference("User").child("");
        auth = FirebaseAuth.getInstance();

        userId = auth.getUid();

        username = (TextView)findViewById(R.id.username);
        lastname = (TextView)findViewById(R.id.lastname);
        firstname = (TextView)findViewById(R.id.firstname);
        phone = (TextView)findViewById(R.id.phone);
        email = (TextView)findViewById(R.id.email);


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User data = dataSnapshot.child(userId).getValue(User.class);




                username.setText(data.userName);
                lastname.setText(data.lastName);
                firstname.setText(data.firstName);
                phone.setText(data.phoneNo);
                email.setText(data.email);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        update =(Button)findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.i("testt", lastname.getText().toString());
                        dataSnapshot.getRef().child("lastName").setValue(lastname.getText().toString());
                        dataSnapshot.getRef().child("firstName").setValue(firstname.getText().toString());
                        dataSnapshot.getRef().child("phoneNo").setValue(phone.getText().toString());
                        dataSnapshot.getRef().child("userName").setValue(username.getText().toString());

                        Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                        startActivity(intent);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("User", databaseError.getMessage());
                    }
                });
            }
        });


    }
}
