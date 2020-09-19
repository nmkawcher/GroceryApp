package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kawcher.mygrocery.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backBtn,gpsBtn;
    private ImageView profileIV;
    private EditText nameET,phoneET,countryET,stateET,cityET,
            addressET,emailET,passwordET,cPasswordET;

    private Button registerBtn;

    private TextView registerSellerTV;

    //permission constatnt

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //Image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission arrays

    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //image picked uri
    private Uri image_uri;

    private double latitude, longLatitude;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        initValue();

        onclickLeasener();

        //init permission array

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("please wait..");

        progressDialog.setCanceledOnTouchOutside(false);


    }

    private void onclickLeasener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location

                if (checkLocationPermisson()) {
                    //already allowed

                    detectLocation();

                } else {
                    //now allowed request

                    requestLocationPermission();


                }
            }
        });

        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pick image

                showImagePickDialog();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user

                inputData();
            }
        });

        registerSellerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this,RegisterSellerActivity.class));
            }
        });
    }

    private void initValue() {
        backBtn=findViewById(R.id.backBtn);
        gpsBtn=findViewById(R.id.gpsBtn);
        profileIV=findViewById(R.id.profileIv);
        nameET=findViewById(R.id.nameET);
        phoneET=findViewById(R.id.phoneET);
        countryET=findViewById(R.id.countryET);
        stateET=findViewById(R.id.stateET);
        addressET=findViewById(R.id.addressET);
        emailET=findViewById(R.id.emailET);
        passwordET=findViewById(R.id.passwordET);
        cPasswordET=findViewById(R.id.cPasswordET);
        cityET=findViewById(R.id.cityET);
        registerBtn=findViewById(R.id.registerBtn);
        registerSellerTV=findViewById(R.id.registerSellerTV);
    }

    private String fullName,phoneNumber,country,state,city,address,email,password,confirmPassword;

    private void inputData() {


        fullName=nameET.getText().toString().trim();
        phoneNumber=phoneET.getText().toString().trim();
        country=countryET.getText().toString().trim();
        state=stateET.getText().toString().trim();
        city=cityET.getText().toString().trim();
        address=addressET.getText().toString().trim();
        email=emailET.getText().toString().trim();
        password=passwordET.getText().toString().trim();
        confirmPassword=cPasswordET.getText().toString().trim();

        //valide data

        if(TextUtils.isEmpty(fullName)){

            Toast.makeText(this,"Enter Name....",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(phoneNumber)){

            Toast.makeText(this,"Enter Phone Number....",Toast.LENGTH_SHORT).show();
            return;
        }

        if(latitude==0.0 || longLatitude==0.0){

            Toast.makeText(this,"pls click gps button to detect location..",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this,"Invalid email Address",Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length()<6){

            Toast.makeText(this,"password must be atleast 6 character long..",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(confirmPassword)){

            Toast.makeText(this,"Password doesn't match",Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();

    }

    private void createAccount() {

        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        //create Account

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account create

                        saverFirebaseData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to create account

                progressDialog.dismiss();
                Toast.makeText(RegisterUserActivity.this,"error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saverFirebaseData() {

        progressDialog.setMessage("Saving Account Info");

        final String timestamp=""+System.currentTimeMillis();

        if(image_uri==null){

            //save info without  image

            //setup data to save

            HashMap<String,Object> hashMap=new HashMap<>();
            hashMap.put("uid",""+firebaseAuth.getUid());
            hashMap.put("email",""+email);
            hashMap.put("name",""+fullName);
            hashMap.put("phone",""+phoneNumber);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longLatitude);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("accountType","User");
            hashMap.put("online","true");
            hashMap.put("profileImage","");


            //save to database

            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //database Updated

                            progressDialog.dismiss();

                            startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //failed  updating database

                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                    finish();
                }
            });
        }
        else {

            //save info with Image

            //name  and path of  image

            String  filePathAndName = "profile_images/" + ""+firebaseAuth.getUid();

            //upload image

            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //get url of uploaded image

                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri=uriTask.getResult();

                            if(uriTask.isSuccessful()){


                                HashMap<String,Object>hashMap=new HashMap<>();
                                hashMap.put("uid",""+firebaseAuth.getUid());
                                hashMap.put("email",""+email);
                                hashMap.put("name",""+fullName);
                                hashMap.put("phone",""+phoneNumber);
                                hashMap.put("country",""+country);
                                hashMap.put("state",""+state);
                                hashMap.put("city",""+city);
                                hashMap.put("address",""+address);
                                hashMap.put("latitude",""+latitude);
                                hashMap.put("longitude",""+longLatitude);
                                hashMap.put("timestamp",""+timestamp);
                                hashMap.put("accountType","User");
                                hashMap.put("online","true");
                                hashMap.put("profileImage",""+downloadImageUri);// url of upload image


                                //save to database

                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //database Updated

                                                progressDialog.dismiss();

                                                startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //failed  updating database

                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterUserActivity.this,MainUserActivity.class));
                                        finish();
                                    }
                                });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            progressDialog.dismiss();

                            Toast.makeText(RegisterUserActivity.this, "error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {

        //options to display in dialog

        String[] options = {"camera", "gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //hadle click

                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {

                                //camera permission allowed
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {

                            //gallery clicked

                            if (checkStoragePermission()) {

                                //storage permission allowed

                                pickFromGallery();

                            } else {

                                //now  allowed request

                                requestStoragePermission();

                            }
                        }
                    }
                }).show();
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

        Toast.makeText(this, "Please wait....", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {

        //find address country state city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(latitude, longLatitude, 1);

            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set Address
            countryET.setText(country);
            stateET.setText(state);
            cityET.setText(city);
            addressET.setText(address);

        } catch (Exception e) {


            Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermisson() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions
                (this, storagePermissions,
                        STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.
                checkSelfPermission(this,
                        Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions
                (this, cameraPermissions,
                        CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(Location location) {

        //location detected

        latitude = location.getLatitude();
        longLatitude = location.getLongitude();

        findAddress();

    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

        //gps location disabled

        Toast.makeText(this, "Please turn on loction...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case LOCATION_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permisson allow

                        detectLocation();
                    } else {

                        //permission denied
                        Toast.makeText(this, "Location permission is necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;

            case CAMERA_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permisson allow

                        pickFromCamera();
                        ;

                    } else {

                        //permission denied
                        Toast.makeText(this, "Camera permission are necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;

            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permisson allow

                        pickFromGallery();

                    } else {

                        //permission denied
                        Toast.makeText(this, "storage permission are necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {

                //get picked image
                image_uri = data.getData();
                profileIV.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                //set to image view

                profileIV.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}