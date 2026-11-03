package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public final class GoogleSignInHelper {

    private GoogleSignInHelper() {
    }

    public static String getWebClientId(Context context) {
        int resId = context.getResources().getIdentifier(
                "default_web_client_id",
                "string",
                context.getPackageName()
        );
        if (resId == 0) {
            return "";
        }
        return context.getString(resId);
    }

    public static boolean isConfigured(Context context) {
        return !getWebClientId(context).trim().isEmpty();
    }

    public static GoogleSignInClient createClient(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getWebClientId(context))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(context, options);
    }

    public static Intent createSignInIntent(Context context) {
        return createClient(context).getSignInIntent();
    }

    public static GoogleSignInAccount getAccountFromResult(Intent data) throws ApiException {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        return task.getResult(ApiException.class);
    }
}
