package tm.veriloft.dotjpgupload;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
    public static String END_POINT = "http://dotjpg.co";
    public static String END_UPLOAD_POINT = END_POINT + "/upload/index.php";

    private boolean isUploading = false;
    private AsyncHttpClient asyncHttpClient;
    private File fileForUpload;

    @InjectView(R.id.imageView) ImageView imageView;
    @InjectView(R.id.upload) CircularProgressButton circularProgressButton;
    @InjectView(R.id.progress) ProgressBar progressView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        asyncHttpClient = new AsyncHttpClient();

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

    public void addImage( Uri uri ) {
        String path = U.getRealPathFromURI(MainActivity.this, uri);
        File file = new File(path);
        if (file.exists()) {
            fileForUpload = file;
            Picasso.with(this).load(fileForUpload).into(imageView);

        } else U.showCenteredToast(this, R.string.image_not_found);
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
        try {
            params.put("image", fileForUpload, "image/jpg");
        } catch (Exception e) {
            U.showCenteredToast(this, R.string.image_not_found);
            return;
        }

        asyncHttpClient.post(getApplicationContext(), END_UPLOAD_POINT, params, new JsonHttpResponseHandler() {
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
                U.showCenteredToast(MainActivity.this, String.format(getString(R.string.endpoint_error), getString(R.string.error) + ": " + statusCode));
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
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", END_POINT + "/" + response.getString("image"));
                        clipboard.setPrimaryClip(clip);
                        U.showCenteredToast(MainActivity.this, String.format(getString(R.string.link_copied), response.getString("image")));
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
                U.showCenteredToast(MainActivity.this, R.string.network_error);
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                circularProgressButton.setProgress(- 1);
                U.showCenteredToast(MainActivity.this, R.string.network_error);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse ) {
                circularProgressButton.setProgress(- 1);
                U.showCenteredToast(MainActivity.this, R.string.network_error);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override public void onCancel() {
                isUploading = false;
                U.hideView(progressView);
                super.onCancel();
            }

            @Override public void onFinish() {
                isUploading = false;
                U.hideView(progressView);
                super.onFinish();
            }
        });
    }
}
