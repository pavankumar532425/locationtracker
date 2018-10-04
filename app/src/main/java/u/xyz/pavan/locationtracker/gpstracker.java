package u.xyz.pavan.locationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by pavan on 31-01-2018.
 */

public class gpstracker implements LocationListener {
    private final Context context;
    private boolean isGpsEnable=false;
    private boolean isnetworkEnable=false;
    private boolean cangetLocation=false;
    private Location location;
    private double Latitude,Longitude;
    private static final float MIN_DISTANCE_FOR_UPDATE=1;
    private static final long MIN_TIME_BW_UPDATE=1000*1*1;
    protected LocationManager locationManager;
    public  gpstracker(Context context){
        this.context=context;
        getLocation();
    }
    private Location getLocation() {
        try{
 locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
 isGpsEnable=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 isnetworkEnable=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
  if(!isGpsEnable&&!isnetworkEnable){
      Toast.makeText(context, "gps is not enable and network is not enable", Toast.LENGTH_SHORT).show();
  }
  else{
      Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
   this.cangetLocation=true;
   if(isnetworkEnable){
       if(ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
               &&ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
       {return null;}
       locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATE,MIN_DISTANCE_FOR_UPDATE,this);
       if(locationManager!=null){
           location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
           if(location!=null){
               Latitude=location.getLatitude();
               Longitude=location.getLongitude();
           }
       }
   }
   if(isGpsEnable){
       if(location==null){
           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATE,MIN_DISTANCE_FOR_UPDATE,this);
           if(locationManager!=null){
               location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
               if(location!=null){
                   Latitude=location.getLatitude();
                   Longitude=location.getLongitude();
               }
           }
       }
   }
       }

        }catch(Exception e){
            e.printStackTrace();
        }
        return  location;
    }
    public  void stopgps(){
        if(locationManager!=null){
            if(ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                    &&ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            {return ;}
            locationManager.removeUpdates(gpstracker.this);
        }
    }
    public double getLatitude(){
        if(location!=null){
      Longitude=location.getLatitude();
        }
        return Latitude;
  }
    public double getLongitude(){
      if(location!=null){
          Longitude=location.getLongitude();
      }
      return Longitude;
  }
    public boolean cangetLocation(){
      return this.cangetLocation;
  }
    public void showSettingAlerDialogt(){
      AlertDialog.Builder alertDialog =new AlertDialog.Builder(context);
      alertDialog.setTitle("GPS is settings");
      alertDialog.setMessage("GPS nor Enabled .do you want o go settings?");
      alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
              Intent intent =new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
              context.startActivity(intent);
          }
      });
      alertDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
dialog.cancel();
          }
      });
      alertDialog.show();
  }
    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            Latitude=location.getLatitude();
        }
        if(location!=null){
            Longitude=location.getLongitude();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
