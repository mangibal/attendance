package com.iqbal.attendance;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iqbal.attendance.kehadiran.CutiActivity;
import com.iqbal.attendance.kehadiran.IzinActivity;
import com.iqbal.attendance.kehadiran.RemoteActivity;
import com.iqbal.attendance.kehadiran.SakitActivity;
import com.iqbal.attendance.kehadiran.TugasActivity;
import com.iqbal.attendance.utils.SharedPrefManager;
import com.iqbal.attendance.utils.api.BaseApiService;
import com.iqbal.attendance.utils.api.UtilsApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Handler timeHandler = new Handler();
    static final int REQUEST_LOCATION = 1;
    double latti;
    double longi;
    Location kantor = new Location("");
    Location lokasiKu = new Location("");
    float distanceInMeters;
    Geocoder geocoder;
    List<Address> addresses;
    private LocationManager locationManager;
    Context mContext;
    BaseApiService mApiService;
    SharedPrefManager sharedPrefManager;
    Calendar cl;
    SimpleDateFormat showDate;
    SimpleDateFormat showTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inisialisasi CardView
        CardView cardPresent = findViewById(R.id.card_hadir);
        mContext = this;
        mApiService = UtilsApi.getAPIService(); // meng-init yang ada di package apihelper
        sharedPrefManager = new SharedPrefManager(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Method getLocation() adalah method yang digunakan untuk mendapatkan lokasi berupa latitude dan longitude
        officeLocation();
        getLocation();
//        String idAnggota = sharedPrefManager.getSpId();
//        String idStatus = sharedPrefManager.getSPPresent();
//        Toast.makeText(mContext, idAnggota + " " + idStatus, Toast.LENGTH_SHORT).show();

        //Dibawah ini adalah code untuk menampilkan waktu
        Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                cl = Calendar.getInstance();
                showDate = new SimpleDateFormat(", d/M/yyy | HH:m:s");
                String stringDate = showDate.format(cl.getTime());
                TextView dateAndTime = (TextView) findViewById(R.id.date);
                dateAndTime.setText(new SimpleDateFormat("EE", Locale.getDefault()).format(cl.getTime()) + stringDate);

                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);

//        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        final double latitude = location.getLatitude();
//        final double longitude = location.getLongitude();
        //Ketika cardPresent di click maka akan muncul AlertDialog
        cardPresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPresent();
            }
        });

    }

    private boolean validate() {

        boolean valid = true;

        if (distanceInMeters > 30) {
            valid = false;
            Toast.makeText(this, "Anda belum berada di kantor!", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private void requestPresent() {
        cl = Calendar.getInstance();
        showDate = new SimpleDateFormat("yyyy-dd-MM");
        showTime = new SimpleDateFormat("HH:mm");

        String idAnggota = sharedPrefManager.getSpId();
        String idStatus = sharedPrefManager.getSPPresent();
        String strDate = showDate.format(cl.getTime());
        String strTime = showTime.format(cl.getTime());
        String latitude = String.valueOf(latti);
        String longitude = String.valueOf(longi);
        if (!validate()) {
            return;
        }

        mApiService.presentPost(
                idAnggota, idStatus, strDate, strTime, latitude, longitude)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
//                            loading.dismiss();
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("error").equals("false")) {
                                    // Jika login berhasil maka data nama yang ada di response API
                                    // akan diparsing ke activity selanjutnya.
//                                    Toast.makeText(mContext, "Login Berhasil", Toast.LENGTH_SHORT).show();
                                    String id = jsonRESULTS.getJSONObject("UserItem").getString("id_anggota");
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_ID, id);
                                    // Shared Pref ini berfungsi untuk menjadi trigger session login
                                    sharedPrefManager.saveSPBooleanHadir(SharedPrefManager.SP_PRESENT, true);
                                    /**
                                     * Code dibawah ini digunakan untuk mengirim data ke WhatsApp beserta dengan koordinat
                                     * latitude dan longitude.
                                     * Mengirim data ini menggunakan Implisit Intent.
                                     */
                                    String uri = "https://maps.google.com/?q=" + latti + "," + longi;
                                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                    whatsappIntent.setType("text/plain");
                                    whatsappIntent.setPackage("com.whatsapp");
                                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Eh guys gue udah hadir di kantor.\n \n" + uri);
                                    try {
                                        startActivityForResult(whatsappIntent, 1);
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(MainActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
                                    }
                                    startActivity(new Intent(mContext, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                } else {
                                    // Jika login gagal
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(mContext, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
//                            loading.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

    private void countDistance() {
        distanceInMeters = kantor.distanceTo(lokasiKu);
//        Toast.makeText(this, "Distance : " + distanceInMeters, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //jika requestCode nya 1 dan hasilnya OK maka setelah share ke WA akan lanjut masuk ke MainActivity dan dialognya akan di close
        if (requestCode == 1 && resultCode == RESULT_OK) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    public void onTugas(View view) {
        startActivity(new Intent(this, TugasActivity.class));
    }

    public void onSakit(View view) {
        startActivity(new Intent(this, SakitActivity.class));
    }

    public void onIzin(View view) {
        startActivity(new Intent(this, IzinActivity.class));
    }

    public void onCuti(View view) {
        startActivity(new Intent(this, CutiActivity.class));
    }

    public void onRemote(View view) {
        startActivity(new Intent(this, RemoteActivity.class));
    }

    public void getLocation() {
        //meminta akses location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Getting Location...");
                progressDialog.show();
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                latti = location.getLatitude();
                                longi = location.getLongitude();
                                lokasiKu.setLatitude(latti);
                                lokasiKu.setLongitude(longi);
                                countDistance();
//                                try {
////                                    addresses = geocoder.getFromLocation(latti, longi, 1);
//                                    countDistance();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
                                progressDialog.dismiss();
                            }
                        },3000);
            }
        }
        return;
    }

    private void officeLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                kantor.setLatitude(-6.890157);
                kantor.setLongitude(107.580094);
            }
        }
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }

}