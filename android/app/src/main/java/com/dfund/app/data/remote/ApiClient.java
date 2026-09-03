package com.dfund.app.data.remote;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static volatile ApiService apiService;
    private static String currentBaseUrl = null;

    public static ApiService getApiService() {
        String baseUrl = isEmulator() ? "http://10.0.2.2:8000/" : "http://127.0.0.1:8000/";
        try {
            if (com.dfund.app.DFundApplication.getInstance() != null) {
                String saved = com.dfund.app.DFundApplication.getInstance().getSecurityManager().getServerUrl();
                if (saved != null && !saved.isEmpty() && !saved.equals("http://10.0.2.2:8000/")) {
                    baseUrl = saved;
                }
            }
        } catch (Exception ignored) {}

        if (apiService == null || !baseUrl.equals(currentBaseUrl)) {
            synchronized (ApiClient.class) {
                if (apiService == null || !baseUrl.equals(currentBaseUrl)) {
                    currentBaseUrl = baseUrl;
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .writeTimeout(20, TimeUnit.SECONDS)
                        .addInterceptor(logging)
                        .build();

                    Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }

    private static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.startsWith("unknown")
            || android.os.Build.MODEL.contains("google_sdk")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion");
    }
}
