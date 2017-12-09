package com.example.Hackturmatiasapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonReg, buttonI;
    EditText editEmail,editPass;


    FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mCallbackManager;
    private static final String TAG = "FACELOG";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Relacion de los controles de la interfaz con la parte logica
        buttonI = (Button) findViewById(R.id.btnIniciar);
        buttonReg = (Button) findViewById(R.id.btnRegistrarse);
        editEmail = (EditText) findViewById(R.id.editMail);
        editPass = (EditText) findViewById(R.id.EditPass);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Asignacion del metodo onClick a los botones
        buttonReg.setOnClickListener(this);
        buttonI.setOnClickListener(this);

        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user !=null){
                    //starta
                    Intent intentintereses = new Intent(MainActivity.this,InteresesActivity.class);
                    startActivity(intentintereses);
                    Log.i("SESION","sesion iniciada con email:"+ user.getEmail());
                }else {
                    Log.i("SESION","sesion cerrada");
                }
            }
        };

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.loginfb);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


    }
    // ...

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuth.getInstance().signOut();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null){
            updateUI();
        }
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);


    }

    private void updateUI(){
        Toast.makeText(MainActivity.this,"Se ha iniciado sesion correctamente",Toast.LENGTH_SHORT).show();
        Intent intentIntereses = new Intent(MainActivity.this, InteresesActivity.class);
        startActivity(intentIntereses);
        finish();
    }




    //Metodo para registrar al usuario en el que se obtiene la instancia de FirebaseAuth para crear un usuario con usuario y contrase;a
    private void registrar(String email,String pass){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.i("SESION","usuario creado correctamente");
                }else{
                    Log.e("SESION",task.getException().getMessage()+"");
                }
            }
        });
    }

    //Metodo para iniciar la sesion del usuario
    private void iniciar(String email,String pass){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass);
    }

    //onClick utilizado con un switch para ambos botones dependiendo si es registro o inicio
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIniciar:
                String emailI = editEmail.getText().toString();
                String passI = editPass.getText().toString();
                iniciar(emailI,passI);
                break;
            case R.id.btnRegistrarse:
                String emailR = editEmail.getText().toString();
                String passR = editPass.getText().toString();
                registrar(emailR,passR);
                break;
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener !=null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });


    }

}
