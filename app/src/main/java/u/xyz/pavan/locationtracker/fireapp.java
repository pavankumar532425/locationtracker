package u.xyz.pavan.locationtracker;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by pavan on 03-02-2018.
 */

public class fireapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
