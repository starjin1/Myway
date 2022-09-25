package com.example.myway;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myway.adapters.ChatAdapter;
import com.example.myway.helpers.SendMessageInBg;
import com.example.myway.interfaces.BotReply;
import com.example.myway.models.Message;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatbotActivity extends AppCompatActivity implements BotReply {

    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;

    public static Context context;

    public String[] startArr;
    public String[] arrivalArr;
    private String time3;
    public String[] TimeList;
    public String Timeset;

    //dialogflow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private String TAG = "mainactivity";

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("길찾기 또봇");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.bot_icon);

        AlertDialog.Builder oDialog = new AlertDialog.Builder(this,
                android.R.style.Theme_DeviceDefault_Light_Dialog);
        oDialog.setMessage("길찾기를 수행하기 위해 '길찾기'라고 입력해주세요.")
                .setTitle("알림")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "확인", Toast.LENGTH_LONG).show();
                    }
                })
                .setCancelable(false)
                .show();

        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    messageList.add(new Message(message, false));
                    editMessage.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(ChatbotActivity.this, "글자를 입력해주세요!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setUpBot();
    }


    private void setUpBot(){
        try{
            InputStream stream = this.getResources().openRawResource(R.raw.credential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            Log.d(TAG, "projectId : " + projectId);
        }catch(Exception e){
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }


    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse!=null) {
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            if(!botReply.isEmpty()){
                messageList.add(new Message(botReply, true));
                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                if(botReply.contains("출발역이")){
                    String S_botReply = botReply;
                    startArr = S_botReply.split(" ");
                    String time3 = mFormat.format(System.currentTimeMillis());//시간 가져와서 정해진 포멧으로
                    Date date = new Date();//Date 객체 생성
                    String Tlist = mFormat.format(date);//split하기 위해 Tlist에 저장
                    TimeList = Tlist.split(":");//split
                    //String Timeset=TimeList[0];//분,초 제외 현재 시각만 받아옴

                    try{
                        Log.d(TAG, "출발역 값 확인: "+startArr[2]);
                        Log.d(TAG,"시간확인"+TimeList[0]);
                    }catch(IndexOutOfBoundsException e){
                        System.out.println(e);
                    }
                }
                if(botReply.contains("도착역이")){
                    String A_botReply = botReply;
                    arrivalArr = A_botReply.split(" ");

                    try{
                        Log.d(TAG, "도착역 값 확인: "+arrivalArr[2]);
                    }catch(IndexOutOfBoundsException e){
                        System.out.println(e);
                    }
                }
                if(botReply.contains("완료했습니다.")){
                    Intent intent = new Intent(getApplicationContext(), DirectionActivity.class);
                    intent.putExtra("Sstation",startArr);
                    intent.putExtra("Astation",arrivalArr);
                    intent.putExtra("Tlist",TimeList);
                    startActivity(intent);
                }
            }else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_cancel) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}