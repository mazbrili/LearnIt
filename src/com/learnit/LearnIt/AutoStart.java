package com.learnit.LearnIt;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.Utils;

public class AutoStart extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.LOG_TAG, "context setalarm = " + context.getClass().getName());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String frequency_id = sp.getString(context.getString(R.string.key_notification_frequency), "-1");
        long frequency = Utils.getFreqFromId(frequency_id);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, NotificationService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), frequency, pi);
        Toast.makeText(context, context.getString(R.string.toast_notif_start_text), Toast.LENGTH_LONG).show();
    }
}