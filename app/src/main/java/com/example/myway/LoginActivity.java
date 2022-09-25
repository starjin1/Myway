package com.example.myway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login_Activity";
    private Context mContext = LoginActivity.this;
    EditText mEmail, mPassword;
    private GoogleSignInClient mGoogleSignClient;
    private int RC_SIGN_IN = 123;
    public static String PREFS_NAME = "MyPrefsFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignClient = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct!=null){
            naviationToSecondActivity();
        }

        String NameData = getIntent().getStringExtra("userName");

//        Intent getName = getIntent();
//        String name = getName.getStringExtra("userName");
//        Log.i("Name",name);


        mEmail =  (EditText) findViewById(R.id.EmailLoginEd);
        mPassword = (EditText) findViewById(R.id.PasswordLogEd);
        Button btnLogin = (Button) findViewById(R.id.LoginBtn);
        Button btnGoogle = (Button) findViewById(R.id.googleBtn);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = mEmail.getText().toString();
                String userPassword = mPassword.getText().toString();


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success){
                                String userEmail = jsonResponse.getString("userEmail");
                                String userPassword = jsonResponse.getString("userPassword");
                                String userName = jsonResponse.get("userName").toString();
                                Toast.makeText(getApplicationContext(), "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();
                                if (userName.isEmpty()) {
                                    Log.i("로그인 Name 데이터 없음","Null");
                                } else {
                                    Log.i("로그인 Name 데이터 있음",userName);
                                }
                                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userEmail",userEmail);
                                editor.putString("userPassword",userPassword);
                                editor.putString("userName",userName);
                                editor.commit();
                                Toast.makeText(getApplicationContext(), userName+"님 환영합니다", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };

                LoginRequest loginRequest = new LoginRequest(userEmail, userPassword,responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);

            }
        });

        Button btnRegister = (Button) findViewById(R.id.registerBtn);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(intent);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle("");

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        findViewById(R.id.bars).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);

        NavigationViewHelper.enableNavigation(mContext,navigationView);

    }
    void signIn() {
        Intent signInIntent = mGoogleSignClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                naviagtionMainActivity();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_LONG).show();
            }

        }
    }
    void naviationToSecondActivity() {
        Intent intent = new Intent(LoginActivity.this,GoogleLoginActvity.class);
        startActivity(intent);
    }

    void naviagtionMainActivity() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
