package com.kawcher.mygrocery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private String productId;
    private ImageButton backBtn;
    private ImageView productIconIV;
    private EditText titleET, descriptionET;
    private TextView categoryTV, quantityET, priceET, discountedPriceET, discountedNoteET;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays

    private String[] cameraPermissions;
    private String[] storagePermissions;

    //imgage picked uri
    private Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //get id of product

        productId = getIntent().getStringExtra("productId");

        backBtn = findViewById(R.id.backBtn);
        productIconIV = findViewById(R.id.productIconIV);
        titleET = findViewById(R.id.titleET);
        descriptionET = findViewById(R.id.descriptionET);
        categoryTV = findViewById(R.id.categoryTV);
        quantityET = findViewById(R.id.quantityET);
        priceET = findViewById(R.id.priceET);
        discountSwitch = findViewById(R.id.discountSwitch);
        discountedPriceET = findViewById(R.id.discountedPriceET);
        discountedNoteET = findViewById(R.id.discountedNoteET);
        updateProductBtn = findViewById(R.id.updateProductBtn);

        //on start is unchecked so hide discountPriceET, discountNoteET
        discountedPriceET.setVisibility(View.GONE);
        discountedNoteET.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Pleas wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadProductDetails();

        //init permission arrays

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //if  discountSwitch is checked show discountPriceET,discountNoteET
        // if disoucntSwitch is not checked hide disocunt PriceET,discountET

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    //checked show discount Price ET,discount note et

                    discountedPriceET.setVisibility(View.VISIBLE);
                    discountedNoteET.setVisibility(View.VISIBLE);

                } else {

                    //unchecked hide discount price et, discount et
                    discountedPriceET.setVisibility(View.GONE);
                    discountedNoteET.setVisibility(View.GONE);
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show  image  pick dialogue

                showImagePickDialog();
            }
        });

        categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category

                categoryDialog();
            }
        });

        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //flow
                //1.input data
                // 2.validate data
                //3.update data to database
                inputData();
            }
        });

    }

    private void loadProductDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get data

                        String productId = "" + snapshot.child("productId").getValue();
                        String productTitle = "" + snapshot.child("productTitle").getValue();
                        String productDescription = "" + snapshot.child("productDescription").getValue();
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        String productQuantity = "" + snapshot.child("productQuantity").getValue();
                        String productIcon = "" + snapshot.child("productIcon").getValue();
                        String originalPrice = "" + snapshot.child("originalPrice").getValue();
                        String discountPrice = "" + snapshot.child("discountPrice").getValue();
                        String discountNote = "" + snapshot.child("discountNote").getValue();
                        String discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();


                        //set data  to views


                        if (discountAvailable.equals("true")) {

                            discountSwitch.setChecked(true);

                            discountedPriceET.setVisibility(View.VISIBLE);
                            discountedNoteET.setVisibility(View.VISIBLE);

                        } else {

                            discountSwitch.setChecked(false);

                            discountedPriceET.setVisibility(View.GONE);
                            discountedNoteET.setVisibility(View.GONE);
                        }

                        titleET.setText(productTitle);
                        descriptionET.setText(productDescription);
                        categoryTV.setText(productCategory);
                        discountedNoteET.setText(discountNote);
                        quantityET.setText(productQuantity);
                        priceET.setText(originalPrice);
                        discountedPriceET.setText(discountPrice);

                        try {

                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_white).into(productIconIV);

                        } catch (Exception e) {

                            productIconIV.setImageResource(R.drawable.ic_add_shopping_white);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle, productDescriptions, productCategory, productQuantity, orginalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //input data

        productTitle = titleET.getText().toString().trim();
        productDescriptions = descriptionET.getText().toString().trim();
        productCategory = categoryTV.getText().toString().trim();
        productQuantity = quantityET.getText().toString().trim();
        orginalPrice = priceET.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();//ture  false

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show();

            return; //don't force further
        }
        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Categoreis required...", Toast.LENGTH_SHORT).show();

            return; //don't force further
        }
        if (TextUtils.isEmpty(orginalPrice)) {
            Toast.makeText(this, "Price is is required...", Toast.LENGTH_SHORT).show();

            return; //don't force further
        }
        if (discountAvailable) {
            //product is with discount

            discountPrice = discountedPriceET.getText().toString().trim();
            discountNote = discountedNoteET.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {

                Toast.makeText(this, "Discount Price is  required../", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            //product without discount
            discountPrice = "0";
            discountNote = "demo";
        }

        updateProduct();


    }

    private void updateProduct() {

        //update data to database


        progressDialog.setMessage("Updating product...");
        progressDialog.show();

        final String timeStamp = "" + System.currentTimeMillis();

        if (image_uri == null) {
            //update without image

            HashMap<String, Object> hashMap = new HashMap<>();

            //setup data  in hashmap to update

            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescriptions);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("originalPrice", "" + orginalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountAvailable", "" + discountAvailable);


            //update to database

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products")
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //update success

                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {

            //update with image

            //first upload image

            //image  name  and ath an firebase storage


            String filePathAndName = "product_images/" + "" + timeStamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //image uploaded
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //uri of image received upload to database

                                HashMap<String, Object> hashMap = new HashMap<>();

                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescriptions);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("originalPrice", "" + orginalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountAvailable", "" + discountAvailable);


                                //upload  to database

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                //added to database

                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();

                                               /* clearData();*/
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });


                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

  /*  private void clearData() {

        //clear data  after uploading product
        titleET.setText("");
        descriptionET.setText("");
        categoryTV.setText("");
        quantityET.setText("");
        priceET.setText("");
        discountedPriceET.setText("");
        discountedNoteET.setText("");
        productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri = null;

    }*/

    private void categoryDialog() {
        //dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constatns.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String category = Constatns.productCategories[which];
                        categoryTV.setText(category);
                    }
                })
                .show();
    }

    private void showImagePickDialog() {

        //options to display in dialog

        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which == 0) {
                            //camera clicked

                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {

                                //permission granted
                                requestCameraPermission();
                            }

                        } else {

                            //gallery clicked
                            if (checkStoragePermission()) {

                                pickFromGallery();

                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
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

    private void pickFromGallery() {

        //intent to pick imamge  from camera

        //using media store to pick high/orginal quality image

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
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


    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

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

    //handle image pick results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image picked from gallery

                //save picked image  uri
                image_uri = data.getData();

                //setImage

                productIconIV.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                //image picked from camera

                productIconIV.setImageURI(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}