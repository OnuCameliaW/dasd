package com.example.camius.telefoniecustil;

import java.lang.Object;
import java.util.Calendar;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import java.util.*;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.database.Cursor;
import android.provider.ContactsContract;

import static java.security.AccessController.getContext;

public class MainActivity extends Activity {
    Button buttonAdd;
    LinearLayout container;
    final Context context = this;
    private static final int RESULT_PICK_CONTACT = 85500;
    private TextView nameOfPickedContact;
    private TextView timeOfAlarm;
    private String phoneNo;
    TimePicker myTimePicker;
    Button buttonSetDialog;
    Button buttonPickContact;
    Button buttonAddAlarm;
    TextView textAlarmPrompt;
    private int alarmNumber = 0;
    private String PickedName = "";
    private Calendar PickedTime ;
    private String pickedPhoneNumber = "";
    TimePickerDialog timePickerDialog;

    final static int RQS_1 = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View startAlarm = getLayoutInflater().inflate(R.layout.setalarm, null);

        //PhoneStateListener
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

        View buttonAdd = findViewById(R.id.add);
        container = (LinearLayout)findViewById(R.id.container);
        buttonAdd.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //Adaug fiecare alarma in mod dinamic (fiecare alarma are asociat cate un View)
                if(PickedTime != null && pickedPhoneNumber != null) {
                    //Id-ul Alarmei
                    alarmNumber = alarmNumber + 1;
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View addView = layoutInflater.inflate(R.layout.row, null);
                    addView.setId(alarmNumber);
                    //Se seteaza numele contactului ales
                    TextView pickedName = (TextView)addView.findViewById(R.id.name);
                    pickedName.setText(PickedName);
                    //Se seteaza timpul ales
                    TextView pickedTime = (TextView)addView.findViewById(R.id.time);
                    pickedTime.setText(""+PickedTime.getTime());

                    Log.i("-------------Set", ""+alarmNumber);
                    //Pentru fiecare alarma se asociaza cate un buton de remove
                    Button buttonRemove = (Button)addView.findViewById(R.id.remove);
                    setAlarm(alarmNumber, PickedTime, pickedPhoneNumber);
                    buttonRemove.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            //Actiunea pentru butonul de remove
                            ((LinearLayout)addView.getParent()).removeView(addView);
                            Log.i("-------------Remove", ""+alarmNumber);
                            cancelAlarm(addView.getId());
                        }});
                    container.addView(addView);
                }
            }});
        nameOfPickedContact = (TextView) findViewById(R.id.name);
        timeOfAlarm = (TextView) findViewById(R.id.time);
        buttonPickContact = (Button)findViewById(R.id.pickContact);
        buttonSetDialog = (Button)findViewById(R.id.startSetDialog);

        //Butonul pentru a deschide dialogul pentru timepicker
        buttonSetDialog.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                openTimePickerDialog(false);
            }});
        //Butonul pentru pick contact
        buttonPickContact.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }});
    }
    //Pentru a anula alarma
    private void cancelAlarm(int alarmId){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), alarmId, myIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
    //Deschide dialogul pentru time picker
    private void openTimePickerDialog(boolean is24r){
        Calendar calendar = Calendar.getInstance();
        timePickerDialog = new TimePickerDialog(
                MainActivity.this,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24r);
        timePickerDialog.setTitle("Set Alarm Time");
        timePickerDialog.show();
    }

    OnTimeSetListener onTimeSetListener = new OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();
            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);
            if(calSet.compareTo(calNow) <= 0){
                calSet.add(Calendar.DATE, 1);
            }
            //pentru alarma, isi seteaza variabila globala "Picked time"  pe care o pune dinamic in view-urile pentru fiecare alarma
            timeOfAlarm.setText("" + calSet.getTime());
            PickedTime = calSet;
        }};
    //Seteaza alarma; trimite la AlarmReceive numarul de telefon
    private void setAlarm(int alarmNr, Calendar targetCal, String pickedPhoneNr){
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("phoneNo", pickedPhoneNr);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmNr,  intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }
    //Listener pentru apelul telefonic
    private class PhoneCallListener extends PhoneStateListener {
        private boolean isPhoneCalling = false;
        String LOG_TAG = "LOGGING 123";
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
            }
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "OFFHOOK");
                isPhoneCalling = true;
            }
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }
    //Atunci cand selecteaza contactul
    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            //phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);
            // Set the value to the textviews
            //Seteaza variabilele globale pe care le foloseste mai sus
            PickedName = name;
            pickedPhoneNumber = cursor.getString(phoneIndex);
            nameOfPickedContact.setText(name);
            //textView2.setText(phoneNo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
