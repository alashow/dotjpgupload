package tm.veriloft.dotjpgupload;
/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.util.Config;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class U {
    /**
     * Hide Given view, by setting visibility View.GONE
     *
     * @param _view view for hide
     */
    public static void hideView( View _view ) {
        if (_view != null) {
            _view.setVisibility(View.GONE);
        }
    }

    /**
     * Hide view if it's visibility equals android.view.View.VISIBLE
     */
    public static void hideViewIfVisible( View _view ) {
        if (_view != null) if (_view.getVisibility() == View.VISIBLE) hideView(_view);
    }

    /**
     * Show given view, by setting visibility View.VISIBLE
     *
     * @param _view view for show
     */
    public static void showView( View _view ) {
        if (_view != null) {
            _view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hides if visible, show if hidden given view
     *
     * @param _view view for toggle visibility
     */
    public static void toggleView( View _view ) {
        if (_view != null) {
            if (_view.getVisibility() == View.VISIBLE) hideView(_view);
            else showView(_view);
        }
    }

    /**
     * Shows aplication error alert dialog
     *
     * @param _activity activity of caller
     */
    public static void applicationError( Activity _activity ) {
        try {
            if (_activity != null) {
                new AlertDialog.Builder(_activity).setMessage(_activity.getString(R.string.exception)).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ) {
                    }
                }).show();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Quick  access to log
     *
     * @param message
     */
    public static void l( Object object, String message ) {
        if (! Config.DEBUG) return;
        if (message != null && message.equals("")) return;
        Log.d("DotjpgUpload / " + object.getClass().getSimpleName(), message);
    }

    public static void l( String tag, String message ) {
        if (! Config.DEBUG) return;
        if (message != null && message.equals("")) return;
        Log.d("DotjpgUpload / " + tag, message);
    }

    public static void l( String message ) {
        if (! Config.DEBUG) return;
        if (message != null && message.equals("")) return;
        Log.d("DotjpgUpload", message);
    }

    public static void l( Object object, int message ) {
        l(object, "Integer = " + message);
    }

    public static void e( String message ) {
        Log.e("Error", message);
    }

    /**
     * Changes title and divider color of alert dialog
     *
     * @param alertDialog AlertDialog object for change
     * @param color       Color for change
     */
    public static void customAlertDialog( AlertDialog alertDialog, @ColorRes int color ) {
        try {
            int textViewId = alertDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
            if (textViewId != 0) {
                TextView tv = (TextView) alertDialog.findViewById(textViewId);
                tv.setTextColor(color);
            }
            int dividerId = alertDialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                View divider = alertDialog.findViewById(dividerId);
                divider.setBackgroundColor(color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRealPathFromURI( Activity context, Uri contentUri ) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * For picasso image loadings
     *
     * @return #f2f2f2 colored drawable
     */
    public static Drawable imagePlaceholder() {
        return new ColorDrawable(Color.parseColor("#f2f2f2"));
    }

    /**
     * Constructing progress dialog for action loading
     *
     * @param _context context
     * @return configured progress dialog
     */
    public static ProgressDialog createActionLoading( Context _context ) {
        ProgressDialog dialog = new ProgressDialog(_context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage(_context.getString(R.string.loading));
        return dialog;
    }

    /**
     * Constructing progress dialog for action loading
     *
     * @param _context context
     * @return configured progress dialog
     */
    public static ProgressDialog createProgressActionLoading( Context _context ) {
        ProgressDialog dialog = new ProgressDialog(_context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle(R.string.loading);
        return dialog;
    }

    /**
     * Showing or hiding keyboard for editText
     *
     * @param view view of edittext
     * @param show show or hide
     */
    public static void keyboard( View view, boolean show ) {
        if (show) showKeyboard(view);
        else hideKeyboard(view);
    }

    public static void showKeyboard( View view ) {
        if (view == null) return;
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }

    public static void hideKeyboard( View view ) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (! imm.isActive()) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void runOnUIThread( Runnable runnable ) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread( Runnable runnable, long delay ) {
        if (delay == 0) {
            ApplicationLoader.applicationHandler.post(runnable);
        } else {
            ApplicationLoader.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread( Runnable runnable ) {
        ApplicationLoader.applicationHandler.removeCallbacks(runnable);
    }

    public static boolean copyFile( File sourceFile, File destFile ) throws IOException {
        if (! destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return true;
    }

    public static String md5( String md5 ) {
        if (md5 == null) {
            return null;
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAboveOfVersion( int version ) {
        return android.os.Build.VERSION.SDK_INT >= version;
    }

    public static String getTrimmedString( String src ) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    /**
     * @return true if passwords not empty and both equals
     */
    public static boolean isPasswordsNormal( EditText passwordView, EditText passwordRetypeView ) {
        if (passwordRetypeView == null || passwordView == null)
            return false;
        String password = passwordView.getText().toString();
        return U.getTrimmedString(password).length() >= 1 && password.equals(passwordRetypeView.getText().toString());
    }

    public static String asString( byte[] bytes ) {
        return new String(bytes);
    }

    public static String humanReadableDate( long timeInMillis ) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(timeInMillis));
    }

    public static long computerReadableDate( String date ) {
        try {
            U.l("date : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(date).getTime());
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    public static String humanReadableTimestamp() {
        return humanReadableDate(Calendar.getInstance().getTimeInMillis());
    }

    public static void showCenteredToast( Context context, String string ) {
        if (context == null)
            return;
        if (string == null || string.equals(""))
            string = "null";
        Toast toast = Toast.makeText(context, string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showCenteredToast( Context context, @StringRes int stringRes ) {
        showCenteredToast(context, context.getString(stringRes));
    }
}
