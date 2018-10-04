package u.xyz.pavan.locationtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.xyz.pavan.locationtracker.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import static com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent;

public class sign_in_page extends AppCompatActivity {
    private SignInButton mgoogle;
    private static final int RC_SIGN_IN=1;
    private GoogleApiClient mgoogleapiclient;
    private String TAG="pavan",username,userEmail,uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mauth;
    private ProgressBar pd;
    private TextView textView;
    public static GoogleSignInClient mGoogleSignInClient;
    public static GoogleSignInAccount account;
    private  GoogleSignInOptions gso;
    private  WordListOpenHelper mdb;

    Uri downloadUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_page);
        pd=(ProgressBar)findViewById(R.id.progressBar);
        textView=(TextView)findViewById(R.id.textView);
        mgoogle=(SignInButton)findViewById(R.id.b1);
        mAuth=FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(sign_in_page.this, gso);
        account = GoogleSignIn.getLastSignedInAccount(sign_in_page.this);
        mdb=new WordListOpenHelper(this);
        if(account!=null) {
            username=account.getDisplayName();
            userEmail=account.getEmail();
            uid=account.getId();
            Intent intent =new Intent(this,map.class);
            intent.putExtra("username",username);
            intent.putExtra("userEmail",userEmail);
            intent.putExtra("uid",uid);

            startActivity(intent);
        }
        else{
            mgoogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   signIn();
                    textView.setVisibility(View.VISIBLE);
                    pd.setVisibility(View.VISIBLE);

                }
            });

        }
    }
    private class Loaderx extends AsyncTask
    {
        @Override
        protected Object doInBackground(Object[] objects)
        {
            initializer("x");
            return null;
        }

        @Override
        protected void onPostExecute(Object o)
        {
            super.onPostExecute(o);
            //Toast.makeText(LoadingScreen.this,"finished",Toast.LENGTH_SHORT);
        }
    }
    public void initializer(final String message)
    {
        if(message.compareTo("x")!=0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(sign_in_page.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            textView.setVisibility(View.VISIBLE);
            pd.setVisibility(View.VISIBLE);
            setPro(10, "connecting");
            if (account == null) {
            } else {
                setPro(40, "Loading Google Account");
                try {
                    FileOutputStream fos = openFileOutput("dispic.png", Context.MODE_PRIVATE);
                    Bitmap bitmap = null;
                    InputStream inputStream = new URL(account.getPhotoUrl().toString()).openStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    setPro(60, "Loading your DP");
                    username=account.getDisplayName();
                    userEmail=account.getEmail();
                    uid=account.getId();
                    setPro(80,"loading your name");
                    uploadphoto(bitmap);

                } catch (Exception ex) {
                    initializer("couldn't write dp");
                }
            }
        }

        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    showAlert();
                }
            });
        }}
    public void showAlert()
    {
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(sign_in_page.this);
        builder1.setMessage("No internet Connection");
        builder1.setCancelable(true);

        builder1.setPositiveButton("try again", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                initializer("no connection");
            }
        });

        builder1.setNegativeButton("exit" , new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                finish();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public void gotoMainScreen()
    {
        setPro(100,"loading maps for you");
        Intent intent =new Intent(this,map.class);
        intent.putExtra("username",username);
        intent.putExtra("userEmail",userEmail);
        intent.putExtra("uid",uid);
        startActivity(intent);
    }
    private boolean signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        return true;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = getSignedInAccountFromIntent(data);
            try {
                /* Google Sign In was successful, authenticate with Firebase */
                GoogleSignInAccount account = task.getResult(ApiException.class);
                this.account=account;
                new Loaderx().execute(new Object());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });
    }
    public void setPro(final int p, final String text)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.setProgress(p);
                textView.setText(text);
            }
        });
    }
    public void uploadphoto(Bitmap bitmap){
        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference(username);
        StorageReference mountainsRef = storageRef.child(username+".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(sign_in_page.this, "failed  man", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
              downloadUrl = taskSnapshot.getDownloadUrl();
                gotoMainScreen();
            }
        });
    }

}
