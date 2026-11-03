package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class FirebaseConnectivityChecker {

    private static final String TAG = "FirebaseCheck";

    private FirebaseConnectivityChecker() {
    }

    public static void verifyFirestoreConnection() {
        FirebaseApp app = FirebaseApp.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d(TAG, "Firebase hazir (oturum acildiktan sonra senkronize edilecek). projectId="
                    + app.getOptions().getProjectId());
            return;
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> payload = new HashMap<>();
        payload.put("checkedAt", System.currentTimeMillis());
        payload.put("platform", "android");
        payload.put("packageName", app.getOptions().getApplicationId());
        payload.put("projectId", app.getOptions().getProjectId());

        firestore.collection("diagnostics")
                .document("android_connection_test")
                .set(payload)
                .continueWithTask(task -> firestore.collection("diagnostics")
                        .document("android_connection_test")
                        .get())
                .addOnSuccessListener(snapshot -> Log.d(
                        TAG,
                        "Firestore baglandi. projectId="
                                + app.getOptions().getProjectId()
                                + ", documentExists="
                                + snapshot.exists()
                ))
                .addOnFailureListener(e -> Log.e(TAG, "Firestore baglanti testi basarisiz", e));
    }
}
