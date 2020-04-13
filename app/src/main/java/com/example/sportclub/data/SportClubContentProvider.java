package com.example.sportclub.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.sportclub.data.SportClubContract.*;


public class SportClubContentProvider extends ContentProvider {

    public static final int MEMBERS = 111;
    public static final int MEMBER_ID = 222;

    SportClubDbHelper dbHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        uriMatcher.addURI(SportClubContract.AUTHORITY, SportClubContract.PATH_MEMBERS, MEMBERS);
        uriMatcher.addURI(SportClubContract.AUTHORITY, SportClubContract.PATH_MEMBERS + "/#", MEMBER_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new SportClubDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:
                cursor = db.query(MemberEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MemberEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Не правильный URI" + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        String firstName = values.getAsString(MemberEntry.COLUMN_FIRST_NAME);
        if(firstName == null){
            throw new IllegalArgumentException("Введите имя");
        }
        String lastName = values.getAsString(MemberEntry.COLUMN_LAST_NAME);
        if(lastName == null){
            throw new IllegalArgumentException("Введите фамилию");
        }
        Integer gender = values.getAsInteger(MemberEntry.COLUMN_GENDER);
        if(gender == null || !(gender == MemberEntry.GENDER_UNKNOWN ||
                gender == MemberEntry.GENDER_MALE || gender == MemberEntry.GENDER_FEMALE)){
            throw new IllegalArgumentException("Введите правильный гендер");
        }
        String sport = values.getAsString(MemberEntry.COLUMN_SPORT);
        if(sport == null){
            throw new IllegalArgumentException("Введите вид спорта");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:
                long id = db.insert(MemberEntry.TABLE_NAME, null, values);
                if(id == -1){
                    Log.e("Mess Error", "Вставка данных в таблицу провалена " + uri);
                    return null;
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);

            default:
                throw new IllegalArgumentException("Не правильный URI" + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int rowsDeleted;

        switch (match){
            case MEMBERS:
                rowsDeleted = db.delete(MemberEntry.TABLE_NAME, selection , selectionArgs);
                break;
            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(MemberEntry.TABLE_NAME, selection , selectionArgs);
                break;


            default:
                throw new IllegalArgumentException("Невозможно удалить URI" + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(MemberEntry.COLUMN_FIRST_NAME)){
            String firstName = values.getAsString(MemberEntry.COLUMN_FIRST_NAME);
            if(firstName == null){
                throw new IllegalArgumentException("Введите имя");
            }
        }

        if(values.containsKey(MemberEntry.COLUMN_LAST_NAME)){
            String lastName = values.getAsString(MemberEntry.COLUMN_LAST_NAME);
            if(lastName == null){
                throw new IllegalArgumentException("Введите фамилию");
            }
        }

        if(values.containsKey(MemberEntry.COLUMN_GENDER)){
            Integer gender = values.getAsInteger(MemberEntry.COLUMN_GENDER);
            if(gender == null || !(gender == MemberEntry.GENDER_UNKNOWN ||
                    gender == MemberEntry.GENDER_MALE || gender == MemberEntry.GENDER_FEMALE)){
                throw new IllegalArgumentException("Введите правильный гендер");
            }
        }

        if(values.containsKey(MemberEntry.COLUMN_SPORT)){
            String sport = values.getAsString(MemberEntry.COLUMN_SPORT);
            if(sport == null){
                throw new IllegalArgumentException("Введите вид спорта");
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case MEMBERS:
                rowsUpdated = db.update(MemberEntry.TABLE_NAME, values, selection , selectionArgs);
                break;
            case MEMBER_ID:
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsUpdated = db.update(MemberEntry.TABLE_NAME, values, selection , selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Невозможно обновить URI" + uri);
        }

        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;

    }

    @Override
    public String getType(Uri uri) {

        int match = uriMatcher.match(uri);

        switch (match){
            case MEMBERS:

                return MemberEntry.CONTENT_MULTIPLY_ITEMS;

            case MEMBER_ID:

                return MemberEntry.CONTENT_SINGE_ITEMS;

            default:
                throw new IllegalArgumentException("Не известный URI" + uri);
        }

    }

}
