package com.example.myway;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private com.google.api.services.calendar.Calendar mService = null;

    // Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    private int mID = 0;

    GoogleAccountCredential mCredential;
//    private TextView mStatusText;
    private TextView mResultText;
    private Button mGetEventButton;
    private Button mAddEventButton;
//    private Button mAddCalendarButton;
    private EditText meditText_detail;
    private EditText meditText_title;
    private EditText meditText_place;
    private EditText meditText_time;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    private Button alarmBtn;
    private NotificationHelper mNotificationhelper;
    private String alarm_title;
    private TextView diaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        mAddEventButton = (Button) findViewById(R.id.button_main_add_event);
        mGetEventButton = (Button) findViewById(R.id.button_main_get_event);
        mResultText = (TextView) findViewById(R.id.textview_main_result);
        meditText_detail = (EditText) findViewById(R.id.textview_main_calendar_detail);
        meditText_title = (EditText) findViewById(R.id.textview_main_calendar_title);
        meditText_place = (EditText) findViewById(R.id.textview_main_calendar_place);
        meditText_time = (EditText) findViewById(R.id.textview_main_calendar_time);
        alarmBtn = findViewById(R.id.alarm_btn);
        diaryTextView = findViewById(R.id.diaryTextView);

        alarm_title = meditText_title.getText().toString();

        Intent intent = getIntent();
        final String date = intent.getExtras().getString("selectedDate");
        diaryTextView.setText(date);

        final EditText meditText_time = (EditText) findViewById(R.id.textview_main_calendar_time);
        meditText_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)+9;
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CalendarActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selecteMinute) {
                        String state = "AM";

                        if (selectedHour > 12) {
                            selectedHour -= 12;
                            state = "PM";
                        }
                        meditText_time.setText(state+" " +selectedHour+"시"+selecteMinute+"분");
                        meditText_time.setText(selectedHour+"시"+selecteMinute+"분");
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddEventButton.setEnabled(false);
                mID = 2; // 이벤트 생성
                getResultFromApi();
                mAddEventButton.setEnabled(true);
            }
        });

        mGetEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetEventButton.setEnabled(false);
                mID = 3; // 이벤트 가져오기
                getResultFromApi();
                mGetEventButton.setEnabled(true);
            }
        });

        //Google Calendar API 호출 결과를 표시하는 TextView를 준비
        mResultText.setVerticalScrollBarEnabled(true);
        mResultText.setMovementMethod(new ScrollingMovementMethod());

//        //Google Calendar API 호출중에 표시되는 ProgressDialog
//        mProgress = new ProgressDialog(this);
//        mProgress.setMessage("Google Calendar API 호출 중입니다.");

        //Google Calendar API 사용하기 위해 필요한 인증 초기화(자격증명 credentials, 서비스객체)
        //OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff());

        mNotificationhelper = new NotificationHelper(this);

        alarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = meditText_title.getText().toString();
                String message = meditText_detail.getText().toString();
                sendOnChannel1(title, message);
            }
        });
    }

    public void sendOnChannel1(String title, String message) {
        NotificationCompat.Builder nb = mNotificationhelper.getChannel1Notification(title, message);
        mNotificationhelper.getManager().notify(0, nb.build());
    }

    // Google Calendar API를 사용하기 위한 조건
    private String getResultFromApi() {
        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용x
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정 선택x
            chooseAccount();
        } else if (!isDeviceOnline()) { // 인터넷 사용X

            Log.d("@@", "@@@@@@@@error_1@@@@@@@@");
        } else { // Google Calendar API 호출
            new MakeRequestTask(this, mCredential).execute();
        }
        return null;
    }

    // 디바이스에 최신 구글 플레이스토어 설치 확인
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    // 구글 플레이 서비스 업데이트 가능? -> 업데이트 유도 대화상자 보여줌
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    // 구글 플레이 스토어 설치x or 오래된 버전일 경우 대화상자 보여줌
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    // 구글 캘린더 api의 자격증명 에 사용할 구글 계정 설정
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        // get_accounts 권한이 있다면
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            // SharedPreferences에서 저장된 Google 계정 가져옴
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                // 선택된 Google 계정 이름으로 설정
                mCredential.setSelectedAccountName(accountName);
                getResultFromApi();
            } else { // 사용자가 구글 계정을 선택할 수 있는 다이얼로그 보여줌
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else { // get_accounts 권한x
            // 사용자에게 권한 요구하는 다이얼로그 보여줌(주소록 권한 요청
            EasyPermissions.requestPermissions(
                    (Activity) this,
                    "This app needs to access your Google account (Via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    // EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {}

    // EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {}

    // 안드로이드 디바이스가 인터넷 연결되어 있는지 확인. 연결 -> true, 연결x -> false 리턴
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    // 캐린더 이름에 대응하는 캘린더 ID 리턴
    private String getCalendarID(String calendarTitle) {
        String id = null;

        // 일정 목록에서 항목 반복
        String pageToken = null;
        do {
            CalendarList calendarList = null;
            try {
                calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry:items) {
                if (calendarListEntry.getSummary().toString().equals(calendarTitle)) {
                    id = calendarListEntry.getId().toString();
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return id;
    }

    // 비동기적으로 google calendar api 호출
    class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private Exception mLastError = null;
        private CalendarActivity mActivity;
        List<String> eventStrings = new ArrayList<String>();

        //
        String value_detail = meditText_detail.getText().toString();
        String value_title = meditText_title.getText().toString();
        String value_place = meditText_place.getText().toString();

        public MakeRequestTask(CalendarActivity activity, GoogleAccountCredential credential) {
            mActivity = activity;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar
                    .Builder(transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        @Override
        protected void onPreExecute() {
//            mProgress.show();
//            builder.setMessage("데이터 가져오는 중...");
            Log.d("@@", "@@@@@@@@error_2@@@@@@@@");
            mResultText.setText("");
        }

        // 백그라운드에서 구글 캘린더 api 호출 처리
        @Override
        protected String doInBackground(Void... params) {
            try {
                /*if (mID == 1) {
                    return createCalendar();
                } else */if (mID == 2) {
                    return addEvent();
                } else if (mID == 3) {
                    return getEvent();
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

        // MywayCalendar 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
        private String getEvent() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());

            String calendarID = getCalendarID("MywayCalendar");
            if (calendarID == null) {
                return "캘린더를 먼저 생성하세요.";
            }

            Events events = mService.events().list(calendarID)
                    .setMaxResults(10)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event:items) {
                DateTime start = event.getStart().getDateTime();

                if (start == null) {
                    // 모든 이벤트가 시작 시간을 갖고 있지 않음. 그런 경우 시작 시간만 사용
                    start = event.getStart().getDate();
                }
//                eventStrings.add(String.format("%s \n(%s)", event.getSummary(), start));
                eventStrings.add(String.format("%s \n", event.getSummary()));
            }
//            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
            return eventStrings.size() + " ";
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
//            mStatusText.setText(output);
            if (mID == 3) mResultText.setText(TextUtils.join("\n\n", eventStrings));
        }

        private String addEvent() {

            String calendarID = getCalendarID("MywayCalendar");
            if (calendarID == null) {
                return "캘린더를 먼저 생성하세요.";
            }

            Event event = new Event()
                    .setSummary(value_title)
                    .setLocation(value_place)
                    .setDescription(value_detail);

            java.util.Calendar calander;

            calander = java.util.Calendar.getInstance();
            SimpleDateFormat simpledateformat;

            simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA);
            String datetime = simpledateformat.format(calander.getTime());

            DateTime startDateTime = new DateTime(datetime);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);

            Log.d("@@@", datetime);

            DateTime endDateTime = new DateTime(datetime);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Seoul");
            event.setEnd(end);

            try {
                event = mService.events().insert(calendarID, event).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception", "Exception: " + e.toString());
            }
            System.out.printf("Event created: %s\n", event.getHtmlLink());
            Log.e("Event", "created: " + event.getHtmlLink());
            String eventStrings = "created: " + event.getHtmlLink();
            return eventStrings;
        }
    }
}
