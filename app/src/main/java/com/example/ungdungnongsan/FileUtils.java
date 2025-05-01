package com.example.ungdungnongsan;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FileUtils {
	/**
	 * Lấy đường dẫn thực từ Uri
	 */
	public static String getPath(Context context, Uri uri) {
		// Nếu Android KitKat trở lên and Uri kiểu document
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
				    && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return context.getExternalFilesDir(null) + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
				String id = DocumentsContract.getDocumentId(uri);
				Uri contentUri = Uri.parse("content://downloads/public_downloads")
						                 .buildUpon().appendPath(id).build();
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				Uri contentUri = null;
				if ("image".equals(split[0])) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				return getDataColumn(context, contentUri, "_id=?", new String[]{ split[1] });
			}
		}
		// Nếu scheme là “content”
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// Nếu scheme là “file”
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null) cursor.close();
		}
		return null;
	}
}
