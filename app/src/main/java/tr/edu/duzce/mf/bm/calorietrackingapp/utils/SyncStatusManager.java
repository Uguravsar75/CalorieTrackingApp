package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SyncStatusManager {

    private static final String PREFS_NAME = "sync_status_prefs";
    private static final String KEY_LAST_SUCCESS_AT = "last_success_at";
    private static final String KEY_LAST_ERROR = "last_error";

    private final SharedPreferences prefs;

    public SyncStatusManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void markSuccess() {
        prefs.edit()
                .putLong(KEY_LAST_SUCCESS_AT, System.currentTimeMillis())
                .remove(KEY_LAST_ERROR)
                .apply();
    }

    public void markFailure(Throwable throwable) {
        prefs.edit()
                .putString(KEY_LAST_ERROR, readableMessage(throwable))
                .apply();
    }

    public long getLastSuccessAt() {
        return prefs.getLong(KEY_LAST_SUCCESS_AT, 0L);
    }

    public String getLastError() {
        return prefs.getString(KEY_LAST_ERROR, null);
    }

    public boolean hasError() {
        String error = getLastError();
        return error != null && !error.trim().isEmpty();
    }

    private String readableMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        String message = current != null ? current.getMessage() : null;
        return message == null || message.trim().isEmpty() ? "Unknown sync error" : message;
    }
}
