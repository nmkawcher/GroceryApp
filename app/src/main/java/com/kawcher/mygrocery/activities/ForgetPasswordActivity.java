package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.kawcher.mygrocery.R;

public class ForgetPasswordActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText emailET;

    private Button recoverBtn;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        emailET=findViewById(R.id.emailET);
        backBtn=findViewById(R.id.backBtn);
        recoverBtn=findViewById(R.id.recoverBtn);


        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("pls wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recoverPassword();
            }
        });
    }

    private String email;

    private void recoverPassword() {

        email=emailET.getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this,"Invalid Email...",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Sending instruction to reset password...");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //instruction sent

                        progressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this,"password reset mail send to your email",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed sending instructions

                        progressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this,"error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}