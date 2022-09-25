package com.example.myway;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private Context mContext = RegisterActivity.this;
    private EditText mCheckPassword,mName;
    private static String userEmail,userPassword,userName, userChekPassword;
    private  AlertDialog dialog;;
    private EditText mEmail;
    private EditText mPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        mEmail = (EditText) findViewById(R.id.EmailLoginEd);
        mPassword = (EditText) findViewById(R.id.PasswordLogEd);
        mCheckPassword = (EditText) findViewById(R.id.PasswordCheck);
        mName = (EditText) findViewById(R.id.RegisterName);
        Pattern pattern = Patterns.EMAIL_ADDRESS;



        Button regBtn = (Button) findViewById(R.id.registerBtn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 현재 입력된 정보를 string으로 가져오기
                Log.i("회원가입 버튼 누름", "회원가입 버튼 click 성공");
                userName = mName.getText().toString();
                userEmail = mEmail.getText().toString();
                userPassword = mPassword.getText().toString();
                userChekPassword = mCheckPassword.getText().toString();

                if (userPassword.equals("") || userEmail.equals("") || userName.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("모두 입력해주세요").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                if(!pattern.matcher(userEmail).matches()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("이메일 형식에 맞게 입력해주세요").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                // 영문자 필수 + 숫자 필수 + 특수문자 필수 + 8 ~ 20자 이하
                if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$",userPassword)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호 형식에 맞게 입력해주세요").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }

                if (!userPassword.equals(userChekPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호가 일치하지 않습니다.").setNegativeButton("확인", null).create();
                    dialog.show();
                    return;
                }



                    //회원가입 절차 시작
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("OnResponce 성공", "OnResponce 성공");
                            try {
                                // String으로 그냥 못 보냄으로 JSON Object 형태로 변형하여 전송
                                // 서버 통신하여 회원가입 성공 여부를 jsonResponse로 받음
                                Log.i("값 받아옴", response + "회원가입 값 받아옴");
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                    if (success) { // 회원가입이 가능하다면
                                        Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();//액티비티를 종료시킴(회원등록 창을 닫음)

                                    } else {// 회원가입이 안된다면
                                        Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다. 다시 한 번 확인해 주세요.", Toast.LENGTH_SHORT).show();


                                    }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };


                    // Volley 라이브러리를 이용해서 실제 서버와 통신을 구현하는 부분
                    RegisterRequest registerRequest = new RegisterRequest(userEmail, userPassword, userName, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                    queue.add(registerRequest);

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }





}

