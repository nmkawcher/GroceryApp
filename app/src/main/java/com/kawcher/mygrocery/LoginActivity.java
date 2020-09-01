package com.kawcher.mygrocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET,passwordET;
    private TextView forgetTV,noAccountTV;
    private Button loginBtn;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailET=findViewById(R.id.emailET);
        passwordET=findViewById(R.id.passwordET);
        forgetTV=findViewById(R.id.forgetTV);
        loginBtn=findViewById(R.id.loginBtn);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("login...");
        progressDialog.setCanceledOnTouchOutside(false);
        noAccountTV=findViewById(R.id.noAccountTV);

        noAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));

            }
        });


        forgetTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LoginActivity.this,ForgetPasswordActivity.class));
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginUser();
            }
        });



    }

    private String email,password;

    private void loginUser() {

        email=emailET.getText().toString().trim();
        password=passwordET.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(LoginActivity.this,"invalid email..",Toast.LENGTH_SHORT).show();
            return;

        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(LoginActivity.this,"Enter password",Toast.LENGTH_SHORT);
            return;
        }

        progressDialog.setMessage("Logging in");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        //logged in successgully
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void makeMeOnline() {

        //after loggin make  user  in online


        progressDialog.setMessage("Checking user..");

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("online","true");

        //update value to database

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //update successfully

                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();

                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void checkUserType() {

        //if user is seller , start seller main screen
//if user is buyer  , start user  main screen

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");

        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                            String accountType =""+dataSnapshot.child("accountType").getValue();

                            if(accountType.equals("Seller")){

                                progressDialog.dismiss();
                                //user is  seller
                                startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                                finish();
                            } else {

                                progressDialog.dismiss();
                                //user is  buyer
                                startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}