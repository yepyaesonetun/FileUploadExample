package com.ypst.primeyz.fileuploadexample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.StringTokenizer;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements UploadStatusDelegate {

    private Button btnUploadFile;
    private String uploadedFileName;
    private StringTokenizer tokens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUploadFile = findViewById(R.id.btnUploadFile);

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File PathToFile(String path) {
        File tempFileToUpload;
        tempFileToUpload = new File(path);
        return tempFileToUpload;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;

                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = this.getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                    }

                    Toast.makeText(this, displayName, Toast.LENGTH_LONG).show();
                    Toast.makeText(this, uriString, Toast.LENGTH_SHORT).show();

//                    uploadToServer(uriString);
                    Log.e("AA", "File Path " + path);

//                    String contentType = getMimeType(path, this, displayName);
//                    Log.e("ContentType: ", contentType + " ");
                    Log.e("ContentType: File  ", getMimeType(getExtension(displayName)) + " " );


                    MultipartUploadRequest request =
                            null;
                    try {
                        request = new MultipartUploadRequest(this, "http://paradisoextraapi.azurewebsites.net/api/photo/uploadPhotoAndroid")
                                .addFileToUpload(uriString, "file")
                                .addParameter("ReceiverID","1")
                                .addParameter("ReceiverName","A")
                                .addParameter("SenderName","B")
                                .addParameter("SenderID","2")
                                .addParameter("Messages", "AAA")
                                .addParameter("FileExtension","." + getExtension(displayName))
                                .addParameter("ContentType", getMimeType(getExtension(displayName)))
                                .setNotificationConfig(new UploadNotificationConfig())
                                .setMaxRetries(2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    request.setDelegate(this);
                    request.startUpload();

//                    String extension = path;
//                    int lastDot = extension.lastIndexOf('.');
//                    if (lastDot != -1) {
//                        extension = extension.substring(lastDot + 1);
//                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private static String getExtension(@Nullable String fileName){

        if(fileName == null || TextUtils.isEmpty(fileName)){
            return null;
        }

        char[] arrayOfFilename = fileName.toCharArray();
        for(int i = arrayOfFilename.length-1; i > 0; i--){
            if(arrayOfFilename[i] == '.'){
                return fileName.substring(i+1, fileName.length());
            }
        }
        return null;
    }

    public static String getMimeType(String fileExtension){
        String mimeType
                = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        return mimeType;
    }


    private void uploadToServer(String filePath) {
        Retrofit retrofit = RetrofitApiClient.getClient();
        ApiInterface uploadAPIs = retrofit.create(ApiInterface.class);
        //Create a file object using file path
        File file = new File(filePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload", file.getName(), fileReqBody);
        //Create request body with text description and text media type
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        //
        Call call = uploadAPIs.fileUpload(part, description);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Fail " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("A", "onFailure: " + t.getMessage());
            }
        });
    }


    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {

    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        Log.e("AA", "onError: " + exception.getMessage());
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.e("AA", "onCompleted");
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Log.e("AA", "onCancel");
    }
}
