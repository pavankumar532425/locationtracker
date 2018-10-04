package u.xyz.pavan.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by pavan on 25/2/18.
 */

public class alarm extends Service {
    private MediaPlayer alr;
    private Vibrator vibrator;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        alr=MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(600000);
        alr.setLooping(true);
        alr.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        alr.stop();
        vibrator.cancel();
        super.onDestroy();
    }
}


