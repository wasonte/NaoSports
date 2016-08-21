package tfg.jorgealcolea.naosports.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by george on 12/08/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "naoDB";

    private final String sqlCreate = "CREATE TABLE highscores (user TEXT,score INTEGER);";

    public DatabaseHelper(Context context, int version){
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva){
        db.execSQL("DROP TABLE IF EXISTS highscores");
        db.execSQL(sqlCreate);
    }
}
