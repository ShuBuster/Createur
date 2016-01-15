package com.example.basededonnees;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.basededonnees.DataBaseAccess.Activite;
import com.example.basededonnees.DataBaseAccess.Emploi;
import com.example.basededonnees.DataBaseAccess.Heure;


/**
 * The actual provider class for the database provider. Clients do not use it directly. Nor
 * do they see it.
 */
public class DataBaseProvider extends ContentProvider {

	// helper constants for use with the UriMatcher
	private static final int EDT_LIST = 1;
	private static final int EDT_ID = 2;
	private static final int TASK_LIST = 5;
	private static final int TASK_ID = 6;
	private static final int HEURE_LIST = 10;
	private static final int HEURE_ID = 11;

	/** URI matcher pour analyser/recuperer les URI des tables. */
	private static final UriMatcher URI_MATCHER;

	private DataBaseHandler mHandler;

	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY, DbSchema.EDT_TABLE_NAME,
				EDT_LIST);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY, DbSchema.EDT_TABLE_NAME
				+ "/#", EDT_ID);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY, DbSchema.TASK_TABLE_NAME,
				TASK_LIST);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY, DbSchema.TASK_TABLE_NAME
				+ "/#", TASK_ID);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY,
				DbSchema.HEUREM_TABLE_NAME, HEURE_LIST);
		URI_MATCHER.addURI(DataBaseAccess.AUTHORITY, DbSchema.HEUREM_TABLE_NAME
				+ "/#", HEURE_ID);
	}

	@Override
	public boolean onCreate() {
		// Je crée mon Handler comme nous l'avons vu dans le chapitre sur les
		// bases de données
		mHandler = new DataBaseHandler(getContext());

		// Et si tout s'est bien passé, je retourne true
		return ((mHandler == null) ? false : true);

		// Et voilà, on n'a pas ouvert ni touché à la base !

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mHandler.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		switch (URI_MATCHER.match(uri)) {
		case EDT_LIST:
			builder.setTables(DbSchema.EDT_TABLE_NAME);
			break;
		case EDT_ID:
			builder.setTables(DbSchema.EDT_TABLE_NAME);
			// limit query to one row at most:
			builder.appendWhere(Emploi._ID + " = " + uri.getLastPathSegment());
			break;
		case TASK_LIST:
			builder.setTables(DbSchema.TASK_TABLE_NAME);
			break;
		case TASK_ID:
			builder.setTables(DbSchema.TASK_TABLE_NAME);
			// limit query to one row at most:
			builder.appendWhere(Activite._ID + " = " + uri.getLastPathSegment());
			break;
		case HEURE_LIST:
			builder.setTables(DbSchema.HEUREM_TABLE_NAME);
			break;
		case HEURE_ID:
			builder.setTables(DbSchema.HEUREM_TABLE_NAME);
			// limit query to one row at most:
			builder.appendWhere(Heure._ID + " = " + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);
		// if we want to be notified of any changes:
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;

	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case EDT_LIST:
			return Emploi.CONTENT_TYPE;
		case EDT_ID:
			return Emploi.CONTENT_ITEM_TYPE;
		case TASK_ID:
			return Activite.CONTENT_TYPE;
		case TASK_LIST:
			return Activite.CONTENT_ITEM_TYPE;
		case HEURE_ID:
			return Heure.CONTENT_TYPE;
		case HEURE_LIST:
			return Heure.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = mHandler.getWritableDatabase();
		long id = 0;
		if (URI_MATCHER.match(uri) == EDT_LIST) {
			id = db.insert(DbSchema.EDT_TABLE_NAME, null, values);
		} else if (URI_MATCHER.match(uri) == TASK_LIST) {
			id = db.insert(DbSchema.TASK_TABLE_NAME, null, values);
		} else if (URI_MATCHER.match(uri) == HEURE_LIST) {
			id = db.insert(DbSchema.HEUREM_TABLE_NAME, null, values);
		}
		if (id > 0) {
			Uri itemUri = ContentUris.withAppendedId(uri, id);
			// notify all listeners of changes and return itemUri:
			getContext().getContentResolver().notifyChange(itemUri, null);
			return itemUri;
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mHandler.getWritableDatabase();
		int delCount = 0;

		switch (URI_MATCHER.match(uri)) {
		case EDT_LIST:
			delCount = db.delete(DbSchema.EDT_TABLE_NAME, selection,
					selectionArgs);
			break;
		case TASK_LIST:
			delCount = db.delete(DbSchema.TASK_TABLE_NAME, selection,
					selectionArgs);
			break;
		case HEURE_LIST:
			delCount = db.delete(DbSchema.HEUREM_TABLE_NAME, selection,
					selectionArgs);
			break;

		case EDT_ID:
			String idStr = uri.getLastPathSegment();
			String where = Emploi._ID + " = " + idStr;
			if (!TextUtils.isEmpty(selection)) {
				where += " AND " + selection;
			}
			delCount = db.delete(DbSchema.EDT_TABLE_NAME, where, selectionArgs);
			break;
		case TASK_ID:
			String idStr1 = uri.getLastPathSegment();
			String where1 = Activite._ID + " = " + idStr1;
			if (!TextUtils.isEmpty(selection)) {
				where1 += " AND " + selection;
			}
			delCount = db.delete(DbSchema.TASK_TABLE_NAME, where1,
					selectionArgs);
			break;
		case HEURE_ID:
			String idStr11 = uri.getLastPathSegment();
			String where11 = Heure._ID + " = " + idStr11;
			if (!TextUtils.isEmpty(selection)) {
				where11 += " AND " + selection;
			}
			delCount = db.delete(DbSchema.HEUREM_TABLE_NAME, where11,
					selectionArgs);
			break;
		default:
			// no support for deleting Task our Hours -
			// they are deleted by a trigger when the edt is deleted
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// notify all listeners of changes:
		if (delCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return delCount;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mHandler.getWritableDatabase();
		int updateCount = 0;
		switch (URI_MATCHER.match(uri)) {
		case EDT_LIST:
			updateCount = db.update(DbSchema.EDT_TABLE_NAME, values, selection,
					selectionArgs);
			break;
		case TASK_LIST:
			updateCount = db.update(DbSchema.TASK_TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case HEURE_LIST:
			updateCount = db.update(DbSchema.HEUREM_TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case EDT_ID:
			String idStr = uri.getLastPathSegment();
			String where = Emploi._ID + " = " + idStr;
			if (!TextUtils.isEmpty(selection)) {
				where += " AND " + selection;
			}
			updateCount = db.update(DbSchema.EDT_TABLE_NAME, values, where,
					selectionArgs);
			break;
		case TASK_ID:
			String idStr1 = uri.getLastPathSegment();
			String where1 = Activite._ID + " = " + idStr1;
			if (!TextUtils.isEmpty(selection)) {
				where1 += " AND " + selection;
			}
			updateCount = db.update(DbSchema.TASK_TABLE_NAME, values, where1,
					selectionArgs);
			break;
		case HEURE_ID:
			String idStr11 = uri.getLastPathSegment();
			String where11 = Heure._ID + " = " + idStr11;
			if (!TextUtils.isEmpty(selection)) {
				where11 += " AND " + selection;
			}
			updateCount = db.update(DbSchema.HEUREM_TABLE_NAME, values,
					where11, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// notify all listeners of changes:
		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;

	}

}
