package com.moutamid.daiptv.utilis;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fxn.stash.Stash;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent in) {
        if (Objects.equals(in.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, MyAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(100), intent, PendingIntent.FLAG_IMMUTABLE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 1);
            calendar.set(Calendar.MINUTE, 0);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Stash.put(Constants.IS_TODAY, calendar.getTimeInMillis());
        }
    }
}
