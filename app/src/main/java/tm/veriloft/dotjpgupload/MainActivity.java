/*
 * Copyright 2014. Alashov Berkeli
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.veriloft.dotjpgupload;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dd.CircularProgressButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {

    public static int RESULT_GALLERY = 0xea;
    public static String ENDPOINT = "http://example.com";
    public static String ENDPOINT_UPLOAD = ENDPOINT + "/upload/index.php";
    public static String ENDPOINT_UPLOAD_PASSWORD = "VYnu4YF9MrkNpknWGgR33ZyZ";

    private boolean isUploading = false;
    private AsyncHttpClient asyncHttpClient;
    private File fileForUpload;
    private File lastFileForUpload; //for sharing file after uploading

    @InjectView(R.id.imageView) ImageView imageView;
    @InjectView(R.id.upload) CircularProgressButton circularProgressButton;
    @InjectView(R.id.progress) ProgressBar progressView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(100000);

        circularProgressButton.setIndeterminateProgressMode(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick( View v ) {
                if (! isUploading) imageChoose();
            }
        });

        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick( View v ) {
                if (! isUploading && circularProgressButton.getProgress() == 0) uploadImage();
                else {
                    circularProgressButton.setProgress(0);
                    asyncHttpClient.cancelRequests(getApplicationContext(), true);
                }
            }
        });

        //Receiving data
        Intent intent = getIntent();
        String intentAction = intent.getAction();
        String type = intent.getType();

        //Adding received
        if (Intent.ACTION_SEND.equals(intentAction) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) addImage(imageUri);
            }
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_GALLERY)
                addImage(data.getData());
        }
    }

    /**
     * Adding file and showing it in image view
     *
     * @param uri uri of file
     */
    public void addImage( Uri uri ) {
        String path = U.getRealPathFromURI(MainActivity.this, uri);
        File file = new File(path);
        if (file.exists()) {
            fileForUpload = file;
            lastFileForUpload = file;
            circularProgressButton.setProgress(0);
            Picasso.with(this).load(fileForUpload).into(imageView);
        } else U.showCenteredToast(this, R.string.image_not_found);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected( MenuItem item ) {
        if (item.getItemId() == R.id.share && lastFileForUpload != null) {
            shareCurrentFile();
            return true;
        } else return false;
    }

    /**
     * Open Image Choose Intent
     */
    private void imageChoose() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_GALLERY);
    }

    private void uploadImage() {
        if (fileForUpload == null) {
            imageView.performClick();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("password", ENDPOINT_UPLOAD_PASSWORD);
        try {
            params.put("image", fileForUpload, "image/jpg");
        } catch (Exception e) {
            U.showCenteredToast(this, R.string.image_not_found);
            return;
        }

        asyncHttpClient.post(getApplicationContext(), ENDPOINT_UPLOAD, params, new JsonHttpResponseHandler() {
            int progress = 0;

            @Override public void onStart() {
                isUploading = true;
                circularProgressButton.setProgress(50);
                U.showView(progressView);
                super.onStart();
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, String responseString ) {
                isUploading = false;
                circularProgressButton.setProgress(- 1);
                U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), ": " + statusCode));
                super.onSuccess(statusCode, headers, responseString);
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                isUploading = false;
                try {
                    if (response.has("error")) {
                        circularProgressButton.setProgress(- 1);
                        U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), response.getString("error")));
                    } else if (response.has("image")) {
                        //Copy link to clipboard
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", ENDPOINT + "/" + response.getString("image"));
                        clipboard.setPrimaryClip(clip);

                        //Show toast with link
                        U.showCenteredToast(MainActivity.this, String.format(getString(R.string.link_copied), response.getString("image")));

                        //Share file with intent
                        shareCurrentFile();

                        fileForUpload = null;
                        circularProgressButton.setProgress(100);
                    } else {
                        circularProgressButton.setProgress(- 1);
                        U.applicationError(MainActivity.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    U.applicationError(MainActivity.this);
                }
            }

            @Override public void onProgress( int bytesWritten, int totalSize ) {
                if ((int) (100.0 / totalSize * bytesWritten) > progress) {
                    progress = (int) (100.0 / totalSize * bytesWritten);
                    progressView.setProgress(progress);
                }
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, String responseString, Throwable throwable ) {
                circularProgressButton.setProgress(- 1);
                U.l("NetworkError: error code = " + statusCode + ", errorMessage = " + ((responseString != null) ? responseString : "null"));
                U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), throwable.getLocalizedMessage()));
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                circularProgressButton.setProgress(- 1);
                U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), throwable.getLocalizedMessage()));
                U.l("NetworkError: error code = " + statusCode + ", errorMessage = " + ((errorResponse != null) ? errorResponse.toString() : "null"));
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse ) {
                circularProgressButton.setProgress(- 1);
                U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), throwable.getLocalizedMessage()));
                U.l("NetworkError: error code = " + statusCode + ", errorMessage = " + ((errorResponse != null) ? errorResponse.toString() : "null"));
            }

            @Override public void onCancel() {
                isUploading = false;
                U.hideView(progressView);
                super.onCancel();
            }

            @Override public void onFinish() {
                isUploading = false;
                U.hideView(progressView);
                progressView.setProgress(0);
                super.onFinish();
            }
        });
    }

    /**
     * Share selected image with intent
     */
    private void shareCurrentFile() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(lastFileForUpload));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }
}
