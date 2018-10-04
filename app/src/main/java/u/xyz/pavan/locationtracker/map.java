package u.xyz.pavan.locationtracker;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.xyz.pavan.locationtracker.R;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class map extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener {
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000 * 3);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, " error has occured please retry again", Toast.LENGTH_SHORT).show();
    }
    private StorageReference storageRef;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference, group;
    private GoogleMap mMap;
    private final int r = 1;
    private String[] p = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.VIBRATE, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String uid, username, userEmail, photouri;
    private Bitmap bitmap;
    private FirebaseUser firebaseUser;
    private CircleImageView circleImageView;
    private View header;
    private ArrayList<String> arrayList, arrayListkey;
    private Map<String, String> map;
    private SupportMapFragment mapFragment;
    private LatLng sydney;
    private double Latitude, Longitude;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private WordListOpenHelper mdb;
    private String GroupName = null;
    private LatLng origin = null;
    private LatLng destination = null , latLng=null,alarm_position=null,yHome=null;
    private FirebaseStorage storage;
    private  final static  int MY_PERMISSIONS_REQUEST=1;
    private GeoFire geoFire,Home;
    //private MediaPlayer alr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // GETTING USERNAME AND USER EMAIL FOR FIRST INTENT
        username = getIntent().getStringExtra("username");
        userEmail = getIntent().getStringExtra("userEmail");
        uid= getIntent().getStringExtra("uid");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20));
            }
        });
        arrayList = new ArrayList<String>();
        arrayListkey = new ArrayList<String>();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        circleImageView = header.findViewById(R.id.imageView);
        setheader();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Groups");
        try {
            if (ActivityCompat.checkSelfPermission(this, p[2]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,p, r);
                mMap.setMyLocationEnabled(true);
            }
        } catch (Exception e) {

        }
        //set google api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        // CREATING GROUPS IN DATABASE
        mdb = new WordListOpenHelper(this);
        // RETRIVING DATA FORM TABLE
        Cursor c = mdb.retrive();
        StringBuffer stringBuffer = new StringBuffer();
        try {
            while (c.moveToNext()) {
                GroupName = c.getString(1);
                // Do something with data
            }
        } finally {
            c.close();
        }
// load image
        if (true) {
            FileInputStream fin = null;
            try {
                fin = openFileInput("dispic.png");
                bitmap = BitmapFactory.decodeStream(fin);
            } catch (Exception e) {
            } finally {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
//geo fire
        database =FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Location");
        geoFire=new GeoFire(databaseReference);
        databaseReference=database.getReference("Home");
        Home=new GeoFire(databaseReference);

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.exit) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return true;
        } else if (id == R.id.satellite)
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        else if (id == R.id.hybrid)
            mMap.setMapType(mMap.MAP_TYPE_HYBRID);
        else if (id == R.id.terrain)
            mMap.setMapType(mMap.MAP_TYPE_TERRAIN);


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.create_group) {
            showAlert("creategroup");
            // Handle the camera action
        } else if (id == R.id.join_group) {
            showAlert("joingroup");
        } else if (id == R.id.set_Home) {
            showAlert("sethome");
        } else if (id == R.id.see_friends) {
            gotofriends();
        } else if (id == R.id.share_friends) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "hi pavan");
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } else if (id == R.id.logout) {
            showAlert("logout");
        }
        else if(id==R.id.set_alarm){
            showAlert("set_alarm");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(getCroppedBitmap(bitmap));
            // Add a marker in Sydney and move the camera
            sydney = new LatLng(Latitude, Longitude);
            mMap.addMarker(new MarkerOptions().position(sydney).title(firebaseUser.getDisplayName()).icon(icon));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);


    }

    public void setheader() {
        FileInputStream fin = null;
        try {
            fin = openFileInput("dispic.png");
            Bitmap bitmap = BitmapFactory.decodeStream(fin);
            circleImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TextView t = (TextView) header.findViewById(R.id.username);
        t.setText(username);
        TextView t1 = (TextView) header.findViewById(R.id.userEmail);
        t1.setText(userEmail);
    }

    public void showAlert(String data) {
        if (data.compareTo("creategroup") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("the group must contain spacial symbal expect '@'and '#'");
            alert.setTitle("enter group name");
            alert.setView(edittext);
            alert.setPositiveButton("create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String YouEditTextValue = edittext.getText().toString();
                    if (checkgroupName(YouEditTextValue)) {
                        creategroup(YouEditTextValue);
                    } else {
                        Toast.makeText(map.this, "enter valid group name", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButt0n) {

                }
            });

            alert.show();
        } else if (data.compareTo("joingroup") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("enter any exiting group name");
            alert.setTitle("enter group name");
            alert.setView(edittext);
            alert.setPositiveButton("join", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String YouEditTextValue = edittext.getText().toString();
                    if (checkgroupName(YouEditTextValue)) {
                        joingroup(YouEditTextValue);
                    } else {
                        Toast.makeText(map.this, "enter valid group name", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButt0n) {

                }
            });

            alert.show();
        } else if (data.compareTo("sethome") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("enter the adress");
            alert.setTitle("it should be perfect");
            alert.setView(edittext);
            alert.setPositiveButton("set", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String YouEditTextValue = edittext.getText().toString();
                    if (true) {
                        sethome(YouEditTextValue);
                    } else
                        onClick(dialog, whichButton);
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButt0n) {

                }
            });

            alert.show();
        } else if (data.compareTo("logout") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setMessage("enter the adress");
            alert.setTitle("it should be perfect");

            alert.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    sign_out();
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButt0n) {

                }
            });

            alert.show();
        }
        else if (data.compareTo("stop") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setMessage("enter the adress");
            alert.setTitle("it should be perfect");

            alert.setPositiveButton("stop", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    stopService(new Intent(map.this,alarm.class));
                }
            });



            alert.show();
        }
        else if (data.compareTo("set_alarm") == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            alert.setMessage("enter the adress");
            alert.setTitle("it should be perfect");
            alert.setView(edittext);
            alert.setPositiveButton("set", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String YouEditTextValue = edittext.getText().toString();
                   set_alarm(YouEditTextValue);
                }
            });

            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButt0n) {

                }
            });

            alert.show();
        }

    }

    private void set_alarm(String place) {
        if (place.compareTo("") != 0) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> list = new ArrayList<Address>();
            try {
                list = geocoder.getFromLocationName(place, 1);
            } catch (Exception e) {
            }
            Address address = list.get(0);
            alarm_position = new LatLng(address.getLatitude(), address.getLongitude());
            geoFire.setLocation(uid, new GeoLocation(Latitude, Longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Toast.makeText(map.this, "pavan", Toast.LENGTH_SHORT).show();
                }
            });
            mMap.addCircle(new CircleOptions().center(alarm_position).fillColor(0x220000FF).radius(500).strokeColor(Color.BLUE).strokeWidth(5.0f));
            GeoQuery geoQuery= geoFire.queryAtLocation(new GeoLocation(alarm_position.latitude,alarm_position.longitude),0.5f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    send_notification();
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
    }}

    private void send_notification() {

       startService(new Intent(this,alarm.class));
       showAlert("stop");
    }

    private void sign_out() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignIn.getClient(this, gso).signOut();
        startActivity(new Intent(map.this, sign_in_page.class));
        finish();
    }

    public boolean checkgroupName(String name) {
        if (name.indexOf('@') == -1 && name.indexOf('#') == -1)
            return false;
        else
            return true;
    }

    public void creategroup(String data) {
        GroupName = data;
        ContentValues values = new ContentValues();
        values.put("GROUPNAMES", GroupName);
        mdb.insert_database(values);
        FirebaseDatabase database;
        DatabaseReference databaseReference;
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Groups");
        databaseReference.child(data).child(username).setValue(Latitude + "," + Longitude);

    }

    public void joingroup(String data) {
        GroupName = data;
        GroupName = data;
        ContentValues values = new ContentValues();
        values.put("GROUPNAMES", GroupName);
        mdb.insert_database(values);
        FirebaseDatabase database;
        DatabaseReference databaseReference;
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Groups");
        databaseReference.child(data).child(username).setValue(Latitude + "," + Longitude);

    }

    public void sethome(String data) {
        if (data.compareTo("") != 0) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> list = new ArrayList<Address>();
            try {
                list = geocoder.getFromLocationName(data, 1);
            } catch (Exception e) {
            }
            Address address = list.get(0);
           latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(firebaseUser.getDisplayName()+" Home"));
           sethome(latLng.latitude,latLng.longitude);
            GeoQuery geoQuery= Home.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.5f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    show_notification();
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Toast.makeText(map.this, "error has been accoured", Toast.LENGTH_SHORT).show();

                }
            });

    }}
    public void sethome(Double latitude,Double longitude){
        if(latitude!=0 &&longitude!=0){
         yHome=new LatLng(latitude,longitude);
        mMap.addCircle(new CircleOptions().center(yHome).fillColor(0x220000FF).radius(500).strokeColor(Color.BLUE).strokeWidth(5.0f));

    }}

    private void show_notification() {
        NotificationManagerCompat managerCompat=NotificationManagerCompat.from(this);
        android.support.v4.app.NotificationCompat.Builder mynot=new android.support.v4.app.NotificationCompat.Builder(this);
        mynot.setContentTitle("nofication from Location tracker");
        mynot.setContentText("your close to your home");
        mynot.setSmallIcon(R.drawable.pav);
        Intent intent=new Intent(this,map.class);
        PendingIntent pd=PendingIntent.getActivity(this,1,intent,0);
        mynot.setContentIntent(pd);
        mynot.setAutoCancel(true);
        managerCompat.notify(1,mynot.build());

    }

    public void gotofriends() {
    if(GroupName!=null)
        new load(this).execute();
    else
        Toast.makeText(this, "set the group name", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mMap.clear();
            if(latLng!=null){
                mMap.addMarker(new MarkerOptions().position(latLng).title(firebaseUser.getDisplayName()+"home"));
                mMap.addCircle(new CircleOptions().center(latLng).fillColor(0x220000FF).radius(500).strokeColor(Color.BLUE).strokeWidth(5.0f)
                );}
                if(alarm_position!=null)
                    mMap.addCircle(new CircleOptions().center(alarm_position).fillColor(0x220000FF).radius(500).strokeColor(Color.BLUE).strokeWidth(5.0f));
            if (Latitude != location.getLatitude() &&
                    Longitude != location.getLongitude()) {
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();
            }
            if (origin != null && destination != null) {
                DrawRouteMaps.getInstance(this)
                        .draw(origin, destination, mMap);
                DrawMarker.getInstance(this).draw(mMap, origin, "Origin Location");
                DrawMarker.getInstance(this).draw(mMap, destination, "Destination Location");

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(origin)
                        .include(destination).build();
                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
            }
            FirebaseDatabase database;
            DatabaseReference databaseReference;
            database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("Groups");
            if (GroupName != null)
                databaseReference.child(GroupName).child(username).setValue(Latitude + "," + Longitude);
            LatLng pavan = new LatLng(location.getLatitude(), location.getLongitude());

            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(getCroppedBitmap(bitmap));
            // Add a marker in Sydney and move the camera
            sydney = new LatLng(Latitude, Longitude);
            mMap.addMarker(new MarkerOptions().position(sydney).title(firebaseUser.getDisplayName()).icon(icon));
            if (arrayList != null) {
                for (String x : arrayList) {
                    if (arrayListkey.indexOf(username) != arrayList.indexOf(x)) {
                        String s[] = x.split(",");
                        String lt = s[0].trim();
                        String lg = s[1].trim();
                        LatLng l = new LatLng(Double.parseDouble(lt), Double.parseDouble(lg));
                        mMap.addMarker(new MarkerOptions().position(l).title(arrayListkey.get(arrayList.indexOf(x))));

                    }
                }
            }
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED/*grantResults[0]== PackageManager.PERMISSION_GRANTED&&grantResults[1]== PackageManager.PERMISSION_GRANTED*/){
                    mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(1000 * 3);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                geoFire.setLocation(uid, new GeoLocation(Latitude, Longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                       
                    }
                });
                    Home.setLocation(uid, new GeoLocation(Latitude, Longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
            }
            else
                    Toast.makeText(this, "permission are not granted", Toast.LENGTH_SHORT).show();}


        }
        }
    @Override
    public boolean onMarkerClick(Marker marker) {
        origin = new LatLng(Latitude, Longitude);
        destination= marker.getPosition();
        DrawRouteMaps.getInstance(this)
                .draw(origin, destination, mMap);
        DrawMarker.getInstance(this).draw(mMap, origin, "Origin Location");
        DrawMarker.getInstance(this).draw(mMap, destination, "Destination Location");
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(destination).build();
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
        return false;
    }

    public class load extends AsyncTask{
        Context c;
        LinearLayout linlaHeaderProgress;
        ProgressDialog dialog;
        load(Context c){
          this.c=c;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             linlaHeaderProgress = (LinearLayout) findViewById(R.id.pd);
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            loadf();
            int i=0;
            while(arrayList.isEmpty()){

            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(1);
        }
        @Override
        protected void onPostExecute(Object o) {

            super.onPostExecute(o);

            linlaHeaderProgress.setVisibility(View.GONE);
        }

    }
    private void loadf() {
        final FirebaseDatabase database;
        DatabaseReference databaseReference;
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Groups");
        databaseReference.child(GroupName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                arrayListkey.add(key);
                arrayList.add(value);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                int index = arrayListkey.indexOf(key);
                String value = dataSnapshot.getValue(String.class);
                arrayList.set(index, value);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}



