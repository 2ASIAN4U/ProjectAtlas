package tk.sbschools.projectatlas;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Address;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    int MYACTIVITY_FLOC_REQCODE = 10;
    TextView locText, addrText, distanceDisp;
    ProgressBar progressLoc, progressAddr;
    ImageView disp;
    List<Address> geoCodeResults;
    AnimationDrawable geoAnime;
    Location lastKnown;
    double disTraveled;
    long timeElaspedLocation;
    int gpsCalibrationDelay;
    ArrayList<Location> pastLocations;
    LocationManager locManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Project Atlas- V0.4 Alpha - Kevin S");
        setContentView(R.layout.activity_main);

        locManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        final Geocoder geocoder = new Geocoder(this);

        gpsCalibrationDelay = 2;

        locText = (TextView) findViewById(R.id.textView_locationText);
        progressLoc = (ProgressBar) findViewById(R.id.locProgress);
        addrText = (TextView) findViewById(R.id.textView_address);
        progressAddr = (ProgressBar) findViewById(R.id.addrProgress);
        progressLoc.getIndeterminateDrawable().setColorFilter(Color.parseColor("#00FF00"),
                android.graphics.PorterDuff.Mode.SRC_ATOP);
        progressAddr.getIndeterminateDrawable().setColorFilter(Color.parseColor("#00FF00"),
                android.graphics.PorterDuff.Mode.SRC_ATOP);

        distanceDisp = (TextView) findViewById(R.id.textView_distance);
        disp = (ImageView) findViewById(R.id.imageView_display);
        disp.setBackgroundResource(R.drawable.spinningglobe);
        geoAnime = (AnimationDrawable) disp.getBackground();
        geoAnime.start();
        disTraveled = 0;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (gpsCalibrationDelay <= 0) {
                    locText.setText("(" + (double) location.getLatitude() + ", "
                            + (double) location.getLongitude() + ")");
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    try {
                        geoCodeResults = geocoder.getFromLocation(location.getLatitude(),
                                location.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addrText.setText(geoCodeResults.get(0).getAddressLine(0) + ", "
                            + geoCodeResults.get(0).getAddressLine(1) + ", "
                            + geoCodeResults.get(0).getAddressLine(2));
                    findViewById(R.id.loadingPanelTwo).setVisibility(View.GONE);//Swap and Fix to GPS
                    if (location.getAccuracy() <= 25 || (location.getAccuracy() < lastKnown.getAccuracy() && lastKnown.getAccuracy() <= 25)) {
                        if (location.distanceTo(lastKnown) >= ((location.getAccuracy() + lastKnown.getAccuracy()) * 0.8)) { //.8 for some overlap lenience
                            disTraveled += location.distanceTo(lastKnown);
                            lastKnown = location;
                        }
                    } else {
                        locText.setText(locText.getText() + "*");
                    }
                    DecimalFormat numberFormat = new DecimalFormat("#.00");
                    distanceDisp.setText("Distance Traveled: " + numberFormat.format(disTraveled));
                } else {
                    gpsCalibrationDelay--;
                    if (location.getAccuracy() <= 20) {
                        lastKnown = location;
                        gpsCalibrationDelay--;
                        timeElaspedLocation = SystemClock.elapsedRealtime();
                    } else {
                        gpsCalibrationDelay++;
                    }
                    locText.setText("Location: Calibrating...");
                }
                System.err.println(location.toString());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.err.println("Does not have perms.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, MYACTIVITY_FLOC_REQCODE);
            return;
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener); //Swap and Fix to GPS
        lastKnown = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //locationManager.removeUpdates(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        System.err.println("Does not have perms.");
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, MYACTIVITY_FLOC_REQCODE);
                        return;
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                }
                return;
            }
        }
    }
    public void disTraveledReset(View v){
        disTraveled = 0.0;
        System.err.println("Location Reset");
    }
}
