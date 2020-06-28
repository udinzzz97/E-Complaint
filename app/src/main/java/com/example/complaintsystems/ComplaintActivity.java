package com.example.complaintsystems;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ComplaintActivity extends AppCompatActivity {

    private Spinner ctype, chours;

    private Button btnSubmit, UploadBtn, ChooseBtn;
    private ImageView IDProf;
    private EditText cdescription, cDate, clocation;
    private ProgressDialog progressDialog;
    private TextView mDisplayDate;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    DatabaseReference complaint_ref;

    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri image_uri;
    private double latitude = 0.0, longitude = 0.0;
    private static final String TAG = "ComplaintActivity";
    private Location current_location;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Context context;
    private String loc, price, authData, type, description, date, hours, location, Document_img1 = "";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);
        FirebaseApp.initializeApp(this);

        context = this;

        auth = FirebaseAuth.getInstance();

        authData = auth.getUid();

        Log.i("guid user", String.valueOf(auth));

        complaint_ref = FirebaseDatabase.getInstance().getReference("Complaint");

        cdescription = findViewById(R.id.description);
        clocation = findViewById(R.id.location);
        ctype = findViewById(R.id.spinner4);
        chours = findViewById(R.id.spinner5);
        cDate = findViewById(R.id.date);
        IDProf = findViewById(R.id.IdProf);
        UploadBtn = findViewById(R.id.UploadBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        if (checkLocationPermission()) {
            //already allowed
            detectLocation();
        }
        else {
            //not allowed, request
            requestLocationPermission();
        }

        IDProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick Image
                showImagePickDialog();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });



    }

    private void inputData() {
        type = ctype.getSelectedItem().toString();
        description = cdescription.getText().toString().trim();
        date = cDate.getText().toString().trim();
        hours = chours.getSelectedItem().toString();
        location = clocation.getText().toString().trim();


        if (TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        addComplaint();
    }

    private void addComplaint() {
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        final String timestamp = "" + System.currentTimeMillis();

        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("activityId", "" + timestamp);
            hashMap.put("posterImages", ""); //no image
            hashMap.put("complaintType", "" + type);
            hashMap.put("complaintDate", "" + date);
            hashMap.put("complaintTime", "" + hours);
            hashMap.put("latitude", "" + current_location.getLatitude());
            hashMap.put("longitude", "" + current_location.getLongitude());
            hashMap.put("activityDescription", "" + description);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + auth.getUid());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Complaint");
            reference.child(auth.getUid()).child("Complaint").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(ComplaintActivity.this, "Activity Added...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ComplaintActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            String filePathAndName = "activity_images/" + "" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("activityId", "" + timestamp);
                                hashMap.put("posterImages", ""); //no image
                                hashMap.put("complaintType", "" + type);
                                hashMap.put("complaintDate", "" + date);
                                hashMap.put("complainyTime", "" + hours);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("activityDescription", "" + description);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + auth.getUid());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(auth.getUid()).child("Complaint").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ComplaintActivity.this, "Activity Added...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ComplaintActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ComplaintActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        ctype.setAdapter(null);
        cdescription.setText("");
        cDate.setText("");
        chours.setAdapter(null);
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //camera is clicked
                    if (checkCameraPermission()) {
                        //camera permissions allowed
                        pickFromCamera();
                    } else {
                        //not allowed, request
                        requestCameraPermission();
                    }
                } else {
                    //gallery clicked
                    if (checkStoragePermission()) {
                        //storage permissions is allowed
                        pickFromGallery();
                    } else {
                        //not allowed, request
                        requestStoragePermission();
                    }
                }
            }
        })
                .show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait for location", Toast.LENGTH_LONG).show();

        new LocationGetter(this, new LocationGetter.OnFinishedListener() {
            @Override
            public void onFinished(Location location) {
                current_location = location;
            }
        });
    }

    private void findAddress() {
        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address;

            //set addresses
            clocation.setText(address);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //
    }
}
