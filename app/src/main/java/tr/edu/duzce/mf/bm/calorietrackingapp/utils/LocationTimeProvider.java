package tr.edu.duzce.mf.bm.calorietrackingapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Locale;
import java.util.TimeZone;

public final class LocationTimeProvider {

    public interface Callback {
        void onResolved(LocationTimeContext context);
    }

    public static class LocationTimeContext {
        public final TimeZone timeZone;
        public final String label;
        public final boolean fromLocation;

        LocationTimeContext(TimeZone timeZone, String label, boolean fromLocation) {
            this.timeZone = timeZone;
            this.label = label;
            this.fromLocation = fromLocation;
        }
    }

    private LocationTimeProvider() {
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static LocationTimeContext defaultContext() {
        TimeZone timeZone = TimeZone.getDefault();
        return new LocationTimeContext(timeZone, buildLabel(timeZone, false), false);
    }

    public static void resolve(Context context, Callback callback) {
        if (!hasLocationPermission(context)) {
            callback.onResolved(defaultContext());
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onResolved(fromLocation(location));
                        } else {
                            resolveLastKnown(client, callback);
                        }
                    })
                    .addOnFailureListener(error -> resolveLastKnown(client, callback));
        } catch (SecurityException exception) {
            callback.onResolved(defaultContext());
        }
    }

    private static void resolveLastKnown(FusedLocationProviderClient client, Callback callback) {
        try {
            client.getLastLocation()
                    .addOnSuccessListener(location -> callback.onResolved(
                            location != null ? fromLocation(location) : defaultContext()))
                    .addOnFailureListener(error -> callback.onResolved(defaultContext()));
        } catch (SecurityException exception) {
            callback.onResolved(defaultContext());
        }
    }

    private static LocationTimeContext fromLocation(Location location) {
        TimeZone timeZone = resolveTimeZone(location.getLatitude(), location.getLongitude());
        return new LocationTimeContext(timeZone, buildLabel(timeZone, true), true);
    }

    private static TimeZone resolveTimeZone(double latitude, double longitude) {
        if (latitude >= 35.8 && latitude <= 42.2 && longitude >= 25.5 && longitude <= 45.0) {
            return TimeZone.getTimeZone("Europe/Istanbul");
        }

        int offsetHours = Math.round((float) longitude / 15f);
        offsetHours = Math.max(-12, Math.min(14, offsetHours));
        String id = String.format(Locale.US, "GMT%+03d:00", offsetHours);
        return TimeZone.getTimeZone(id);
    }

    private static String buildLabel(TimeZone timeZone, boolean fromLocation) {
        TimeZone effectiveZone = timeZone != null ? timeZone : TimeZone.getDefault();
        boolean english = Locale.getDefault().getLanguage().equals("en");
        if (fromLocation) {
            return english
                    ? "Location time: " + effectiveZone.getID()
                    : "Konum saati: " + effectiveZone.getID();
        }
        return english
                ? "Device time: " + effectiveZone.getID()
                : "Cihaz saati: " + effectiveZone.getID();
    }
}
