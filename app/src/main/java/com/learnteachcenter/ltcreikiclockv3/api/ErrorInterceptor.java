package com.learnteachcenter.ltcreikiclockv3.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

// https://stackoverflow.com/questions/38562873/retrofit2-check-response-code-globally
// https://stackoverflow.com/questions/48340581/retrofit-redirect-to-loginactivity-if-response-code-is-401
// https://stackoverflow.com/questions/5794506/android-clear-the-back-stack

public class ErrorInterceptor implements Interceptor {

    private Context context;

    public ErrorInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // before request
        Request request = chain.request();

        // execute request
        Response response = chain.proceed(request);

        // after request, inspect status codes of unsuccessful responses
        switch (response.code()) {
            case 401:
                // do something else
                Log.e("Reiki", "Unauthorized error for: " + request.url());

                // redirect to login
                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);

                throw new IOException("Unauthorized!!");
        }

        return response;
    }
}
