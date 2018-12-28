package com.ypst.primeyz.fileuploadexample;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by yepyaesonetun on 11/15/18.
 **/
public interface ApiInterface {
    @Multipart
    @POST("photo/uploadPhotoAndroid")
    Call<ResponseBody> fileUpload(
            @Part MultipartBody.Part file, @Part("name") RequestBody requestBody);
}
