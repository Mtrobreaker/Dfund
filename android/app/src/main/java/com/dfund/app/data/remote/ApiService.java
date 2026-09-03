package com.dfund.app.data.remote;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @Multipart
    @POST("/api/voice/interact")
    Call<VoiceResponse> processVoiceInteraction(
        @Part MultipartBody.Part audioFile,
        @Part("text_prompt") RequestBody textPrompt,
        @Part("device_id") RequestBody deviceId,
        @Part("preferred_language") RequestBody preferredLanguage
    );

    @GET("/api/savings/calculate-sip")
    Call<Object> calculateSip(
        @Query("monthly_amount") double monthlyAmount,
        @Query("annual_rate") double annualRate,
        @Query("tenure_years") int tenureYears
    );
}
