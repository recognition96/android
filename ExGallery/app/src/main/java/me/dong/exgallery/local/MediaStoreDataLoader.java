package me.dong.exgallery.local;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads metadata from the media store for images and videos.
 */
public class MediaStoreDataLoader extends AsyncTaskLoader<List<MediaStoreData>> {
    private static final String[] IMAGE_PROJECTION =
            new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.ORIENTATION
            };

//    private static final String[] VIDEO_PROJECTION =
//            new String[]{
//                    MediaStore.Video.VideoColumns._ID,
//                    MediaStore.Video.VideoColumns.DATE_TAKEN,
//                    MediaStore.Video.VideoColumns.DATE_MODIFIED,
//                    MediaStore.Video.VideoColumns.MIME_TYPE,
//                    "O AS " + MediaStore.Images.ImageColumns.ORIENTATION,
//            };

    private List<MediaStoreData> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver mForceLoadContentObserver = new ForceLoadContentObserver();

    public MediaStoreDataLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(List<MediaStoreData> data) {
        if (!isReset() && isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (cached != null) {
            deliverResult(cached);
        }
        if (takeContentChanged() || cached == null) {
            forceLoad();
        }
        registerContentObserver();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        cached = null;
        unregisterContentObserver();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        unregisterContentObserver();
    }

    @Override
    public List<MediaStoreData> loadInBackground() {
        List<MediaStoreData> data = queryImages();
//        data.addAll(queryVideos());
//        Collections.sort(data, new Comparator<MediaStoreData>() {
//            @Override
//            public int compare(MediaStoreData t, MediaStoreData t1) {
//                return Long.valueOf(t.dateTaken).compareTo(t1.dateTaken);
//            }
//        });
        return data;
    }

    private List<MediaStoreData> queryImages() {
        return query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.DATE_MODIFIED,
                MediaStore.Images.ImageColumns.MIME_TYPE, MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStoreData.Type.IMAGE);
    }

//    private List<MediaStoreData> queryVideos() {
//        return query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
//                MediaStore.Video.VideoColumns.DATE_TAKEN, MediaStore.Video.VideoColumns._ID,
//                MediaStore.Video.VideoColumns.DATE_TAKEN, MediaStore.Video.VideoColumns.DATE_MODIFIED,
//                MediaStore.Video.VideoColumns.MIME_TYPE, MediaStore.Images.ImageColumns.ORIENTATION,
//                MediaStoreData.Type.VIDEO);
//    }

    private List<MediaStoreData> query(Uri contentUri, String[] projection, String sortByCol,
                                       String idCol, String dateTakenCol, String dateModifiedCol,
                                       String mimeTypeCol, String orientationCol, MediaStoreData.Type type) {
        final List<MediaStoreData> data = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver()
                .query(contentUri, projection, null, null, sortByCol + " DESC");

        if (cursor == null) {
            return data;
        }

        try {
            final int idColNum = cursor.getColumnIndexOrThrow(idCol);
            final int dateTakenColNum = cursor.getColumnIndexOrThrow(dateTakenCol);
            final int dateModifiedColNum = cursor.getColumnIndexOrThrow(dateModifiedCol);
            final int dmimeTypeColNum = cursor.getColumnIndexOrThrow(mimeTypeCol);
            final int orientationColNum = cursor.getColumnIndexOrThrow(orientationCol);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColNum);
                long dateTaken = cursor.getLong(dateTakenColNum);
                String mimeType = cursor.getString(dmimeTypeColNum);
                long dateModified = cursor.getLong(dateModifiedColNum);
                int orientation = cursor.getInt(orientationColNum);

                data.add(new MediaStoreData(id, Uri.withAppendedPath(contentUri, Long.toString(id)),
                        mimeType, dateModified, orientation, type, dateTaken));
            }
        } finally {
            cursor.close();
        }
        return data;
    }

    private void registerContentObserver() {
        if (!observerRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false,
                    mForceLoadContentObserver);
            cr.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false,
                    mForceLoadContentObserver);

            observerRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (observerRegistered) {
            observerRegistered = false;

            getContext().getContentResolver().unregisterContentObserver(mForceLoadContentObserver);
        }
    }
}
