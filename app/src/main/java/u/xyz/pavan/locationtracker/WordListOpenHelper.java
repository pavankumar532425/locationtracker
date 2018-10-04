package u.xyz.pavan.locationtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class WordListOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    // Has to be 1 first time or app will crash.
    private static final String DATABASE_NAME = "PAVAN";
    private static final String TABLE = "GROUPS";
    // Column names...
    public static final String KEY_ID = "_id";
    public static final String KEY_WORD = "name";
    public static final String age = "age";
    private static final String WORD_LIST_TABLE_CREATE =
            "CREATE TABLE GROUPS(ID INTEGER PRIMARY KEY,GROUPNAMES TEXT);";
    private static final String Home="CREATE TABLE HOME(ID INTEGER PRIMARY KEY, LATITUDE DOUBLE,LONGITUDE DOUBLE);";
    Context context;
    public WordListOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
        Log.d(TAG, "Construct WordListOpenHelper");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WORD_LIST_TABLE_CREATE);
        db.execSQL(Home);

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(WordListOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
    public long insert_database(ContentValues values){
        SQLiteDatabase db;
        db=this.getWritableDatabase();
        long i=db.insert(TABLE,null,values);
        return i;
    }
    public Cursor retrive(){
        SQLiteDatabase db;
        db=this.getWritableDatabase();
        Cursor c=db.rawQuery("select * from GROUPS",null);
        return c;
    }

    public long insert_Home(ContentValues values){
        SQLiteDatabase db;
        db=this.getWritableDatabase();
        long i=db.insert("HOME",null,values);
        return i;
    }
    public Cursor retrive_Home(){
        SQLiteDatabase db;
        db=this.getWritableDatabase();
        Cursor c=db.rawQuery("select * from HOME",null);
        return c;
    }
}
