package tfg.jorgealcolea.naosports.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import tfg.jorgealcolea.naosports.beans.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by george on 12/08/16.
 */
public class DatabaseAdapter {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    public DatabaseAdapter(Context context){
        this.context = context;

        dbHelper = new DatabaseHelper(context, 1);
    }

    private DatabaseAdapter open() throws SQLException {
        db = null;

        try {
            db = dbHelper.getWritableDatabase();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return this;
    }

    private void close() {
        try {
            dbHelper.close();
        } catch(SQLException e){
        e.printStackTrace();
        }
    }

    public boolean connect () {
        try {
            this.open();
            return db != null;
        }
        finally {
            this.close();
        }
    }

    public void insertScore(Score score){
        try {
            this.open();

            ContentValues newScore = new ContentValues();
            newScore.put("user", score.getUserName());
            newScore.put("score", score.getScore());

            db.insert("highscores", null, newScore);
        } finally {
            this.close();
        }
    }

    public List<Score> getScores(){
        try{
            this.open();
            List<Score> scores = new ArrayList<>();

            Cursor mCursor = db.query(
                    // Table.
                    "highscores",
                    // Columns.
                    new String[] {"user", "score"},
                    // Where.
                    null,
                    // Where args.
                    null,
                    // Group by.
                    null,
                    // Having.
                    null,
                    // Order by.
                    "score DESC");

            mCursor.moveToFirst();

            if (mCursor != null && mCursor.getCount() > 0) {

                while (!mCursor.isAfterLast()){
                    Score result = new Score(mCursor.getString(0), mCursor.getInt(1));
                    scores.add(result);
                    mCursor.moveToNext();
                }
            }
            return scores;
        } finally {
            this.close();
        }
    }
}
