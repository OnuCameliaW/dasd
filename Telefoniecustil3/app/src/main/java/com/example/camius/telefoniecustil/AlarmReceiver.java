package com.example.camius.telefoniecustil;

import java.lang.String;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Primeste numarul de telefon din MainActivity
        String result = intent.getStringExtra("phoneNo");

        Log.i("-------------IncomingCallReceiver","Incomng Number: " + result);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Il parseaza si initiaza apelul telefonic
        callIntent.setData(Uri.parse("tel:"+ result));
        context.getApplicationContext().startActivity(callIntent);
    }


}

