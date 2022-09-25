package com.example.myway;

import static com.example.myway.CalendarActivity.REQUEST_ACCOUNT_PICKER;
import static com.example.myway.CalendarActivity.REQUEST_AUTHORIZATION;
import static com.example.myway.CalendarActivity.REQUEST_GOOGLE_PLAY_SERVICES;
import static com.example.myway.CalendarActivity.REQUEST_PERMISSION_GET_ACCOUNTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarMainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    final CharSequence[] date = {null};

    // Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    private int mID = 0;

    // Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    private com.google.api.services.calendar.Calendar mService = null;

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_main);

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        TextView diaryTextView = (TextView) findViewById(R.id.diaryTextView);

        // 표시 날짜 클릭시 화면 전환
        diaryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);

                mID = 1; // 캘린더 생성
                getResultFromApi();

                intent.putExtra("selectedDate", date[0]);

                startActivity(intent);
            }
        });

        // 클릭한 날짜 표시
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                diaryTextView.setVisibility(View.VISIBLE);
                diaryTextView.setText(String.format("%d . %02d . %02d", year, month + 1, dayOfMonth));

                date[0] = String.format("%d . %02d . %02d", year, month + 1, dayOfMonth);
            }
        });

        //Google Calendar API 사용하기 위해 필요한 인증 초기화(자격증명 credentials, 서비스객체)
        //OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES)
        ).setBackOff(new ExponentialBackOff());
    }

    // Google Calendar API를 사용하기 위한 조건
    private String getResultFromApi() {
        if (!isGooglePlayServicesAvailable()) { // Google Play Services를 사용x
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) { // 유효한 Google 계정 선택x
            chooseAccount();
        } else if (!isDeviceOnline()) { // 인터넷 사용X
//            alert.setMessage("No network connection available.");
            Log.d("@@", "@@@@@@@@error_1@@@@@@@@");
        } else { // Google Calendar API 호출
            new CalendarMainActivity.MakeRequestTask(this, mCredential).execute();
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
                CalendarMainActivity.this,
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

    // 안드로이드 디바이스가 인터넷 연결되어 있는지 확인. 연결 -> true, 연결x -> false 리턴
    public boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    // 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그,인증 다이얼로그에서 되돌아올때 호출
    @Override
    protected void onActivityResult(
            int requestCode, // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
            int resultCode, // 요청에 대한 결과 코드
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    alert.setMessage("앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
//                            + "구글 플레이 서비스를 설치 후 다시 실행하세요.");
                    Log.d("@@", "@@@@@@@@@@@@@@@@");
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

    // android 6.0(api 23) 이상에서 런타임 권한 요청시 리턴받음
    @Override
    public void onRequestPermissionsResult(
            int requestCode, // requestPermissions(android.app.Activity, String, int, Stirng[])에서 전달된 요청 코드
            @NonNull String[] permissions, // 요청한 퍼미션
            @NonNull int[] grantResults // 퍼미션 처리 결과. PERMISSION_GRAND 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출
    @Override
    public void onPermissionsGranted(int requestCode, List<String> requestPermissionList) {}

    // EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출
    @Override
    public void onPermissionsDenied(int requestCode, List<String> requestPermissionList) {}

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
        private CalendarMainActivity mActivity;
        List<String> eventStrings = new ArrayList<String>();

        public MakeRequestTask(CalendarMainActivity activity, GoogleAccountCredential credential) {
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
//            alert.setMessage("데이터 가져오는 중...");
            Log.d("@@", "@@@@@@@@error_2@@@@@@@@");
        }

        // 백그라운드에서 구글 캘린더 api 호출 처리
        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mID == 1) {
                    return createCalendar();
                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

        // 선택되어 있는 구글 계정에 "MywayCalendar" 캘린더 추가
        private String createCalendar() throws IOException {
            String ids = getCalendarID("MywayCalendar");
            if (ids != null) {
                return "이미 캘린더가 생성되어 있습니다.";
            }

            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar(); // 새로운 캘린더 생성
            calendar.setSummary("MywayCalendar"); // 캘린더 제목
            calendar.setTimeZone("Asia/Seoul"); // 캘린더 시간대
            com.google.api.services.calendar.model.Calendar createdCalendar = mService.calendars().insert(calendar).execute(); // 새로만든 캘린더 -> 구글 캘린더에 추가
            String calendarId = createdCalendar.getId(); // 추가한 캘린더의 아이디 가져옴
            CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute(); // 구글 캘린더 목록에서 새로 만든 캘린더 검색
            calendarListEntry.setBackgroundColor("#9da2f0"); // 캘린더 배경 색
            // 변경한 내용 -> 구글 캘린더에 반영
            CalendarListEntry updatedCalendarListEntry = mService.calendarList()
                    .update(calendarListEntry.getId(), calendarListEntry)
                    .setColorRgbFormat(true)
                    .execute();

            return "캘린더가 생성되었습니다."; // 새로 추가한 캘린더 아이디 리턴
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarMainActivity.REQUEST_AUTHORIZATION);
                } else {
//                    alert.setMessage("MakeRequestTask The following error occurred:\n" +
//                            mLastError.getMessage());
                    Log.d("@@", "@@@@@@@@error_3@@@@@@@@");
                }
            } else {
//                alert.setMessage("요청 취소됨.");
                Log.d("@@", "@@@@@@@@error_4@@@@@@@@");
            }
        }
    }
}