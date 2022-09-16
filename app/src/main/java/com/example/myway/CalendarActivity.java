package com.example.myway;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.MenuItem;

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
import android.app.DatePickerDialog;
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
import androidx.core.app.NotificationManagerCompat;

import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
    private TextView mStatusText;
    private TextView mResultText;
    private Button mGetEventButton;
    private Button mAddEventButton;
    private Button mAddCalendarButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);

        mAddCalendarButton = (Button) findViewById(R.id.button_main_add_calendar);
        mAddEventButton = (Button) findViewById(R.id.button_main_add_event);
        mGetEventButton = (Button) findViewById(R.id.button_main_get_event);

        mStatusText = (TextView) findViewById(R.id.textview_main_status);
        mResultText = (TextView) findViewById(R.id.textview_main_result);

        meditText_detail = (EditText) findViewById(R.id.textview_main_calendar_detail);
        meditText_title = (EditText) findViewById(R.id.textview_main_calendar_title);
        meditText_place = (EditText) findViewById(R.id.textview_main_calendar_place);
        meditText_time = (EditText) findViewById(R.id.textview_main_calendar_time);

        alarmBtn = findViewById(R.id.alarm_btn);

        alarm_title = meditText_title.getText().toString();

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

        // 버튼 클릭으로 동작 테스트
        mAddCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddCalendarButton.setEnabled(false);
                mStatusText.setText("");
                mID = 1; // 캘린더 생성
                getResultFromApi();
                mAddCalendarButton.setEnabled(true);
            }
        });

        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddEventButton.setEnabled(false);
                mStatusText.setText("");
                mID = 2; // 이벤트 생성
                getResultFromApi();
                mAddEventButton.setEnabled(true);
            }
        });

        mGetEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetEventButton.setEnabled(false);
                mStatusText.setText("");
                mID = 3; // 이벤트 가져오기
                getResultFromApi();
                mGetEventButton.setEnabled(true);
            }
        });

        //Google Calendar API 호출 결과를 표시하는 TextView를 준비
        mResultText.setVerticalScrollBarEnabled(true);
        mResultText.setMovementMethod(new ScrollingMovementMethod());

        mStatusText.setVerticalScrollBarEnabled(true);
        mStatusText.setMovementMethod(new ScrollingMovementMethod());
        mStatusText.setText("버튼을 눌러 테스트를 진행하세요.");

        //Google Calendar API 호출중에 표시되는 ProgressDialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Google Calendar API 호출 중입니다.");

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
        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용할 수 업슨 ㄴ경우
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정이선택x
            chooseAccount();
        } else if (!isDeviceOnline()) { // internet use X
            mStatusText.setText("No network connection available.");
        } else {
            // Google Calendar API 호출
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

    // 구글 플레이 서비스 업데이ㅡ 가능? -> 대화사앚 보여줌
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    // google user choice
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        // get_accounts 권한 있?
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultFromApi();
            } else {
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    (Activity) this,
                    "This app needs to access your Google account (Via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mStatusText.setText("앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
                            + "구글 플레이 서비스를 설치 후 다시 실행하세요.");
                } else {
                    getResultFromApi();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultFromApi();
                    }
                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {}

    public boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    private String getCalendarID(String calendarTitle) {
        String id = null;

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

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
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
            mProgress.show();
            mStatusText.setText("데이터 가져오는 중...");
            mResultText.setText("");
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mID == 1) {
                    return createCalendar();
                } else if (mID == 2) {
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
                    start = event.getStart().getDate();
                }
                eventStrings.add(String.format("%s \n (%s)", event.getSummary(), start));
            }
            return eventStrings.size() + "개의 데이터를 가져왔습니다.";
        }

        private String createCalendar() throws IOException {
            String ids = getCalendarID("MywayCalendar");
            if (ids != null) {
                return "이미 캘린더가 생성되어 있습니다.";
            }

//             com.google.api.services.calendar.model.Calendar calendar = new Calendar();
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary("MywayCalendar");
            calendar.setTimeZone("Asia/Seoul");
//             Calendar createdCalendar = mService.calendars().insert(calendar).execute();
            com.google.api.services.calendar.model.Calendar createdCalendar = mService.calendars().insert(calendar).execute();
            String calendarId = createdCalendar.getId();
            CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();
            calendarListEntry.setBackgroundColor("#9da2f0");
            CalendarListEntry updatedCalendarListEntry = mService.calendarList()
                    .update(calendarListEntry.getId(), calendarListEntry)
                    .setColorRgbFormat(true)
                    .execute();
            return "캘린더가 생성되었습니다.";
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            mStatusText.setText(output);
            if (mID == 3) mResultText.setText(TextUtils.join("\n\n", eventStrings));
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarActivity.REQUEST_AUTHORIZATION);
                } else {
                    mStatusText.setText("MakeRequestTask The following error occurred:\n" +
                            mLastError.getMessage());
                }
            } else {
                mStatusText.setText("요청 취소됨.");
            }
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

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.calendar_layout);
//
//        OneDayDecorator oneDayDecorator = new OneDayDecorator();
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar_calendar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
//        getSupportActionBar().setTitle("");
//        toolbar.setSubtitle("");
//
//        MaterialCalendarView materialCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
//        materialCalendarView.addDecorator(
//                oneDayDecorator
//        );
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
