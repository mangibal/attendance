package com.iqbal.attendance.kehadiran;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.iqbal.attendance.MainActivity;
import com.iqbal.attendance.R;
import com.iqbal.attendance.utils.SharedPrefManager;
import com.iqbal.attendance.utils.api.BaseApiService;
import com.iqbal.attendance.utils.api.UtilsApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TugasActivity extends AppCompatActivity {

    static final int REQUEST_LOCATION = 1;
    Button btnBackTugas;
    ImageView imageView;
    Geocoder geocoder;
    List<Address> addresses;
    LocationManager locationManager;
    double lat = 0;
    double lon = 0;

    @Bind(R.id.edt_lokasi_tugas)
    EditText edtLokasi;
    @Bind(R.id.edt_keterangan_tugas)
    EditText edtKeterangan;
    @Bind(R.id.btn_submit_tugas)
    Button submitButton;
    Context mContext;
    BaseApiService mApiService;
    SharedPrefManager sharedPrefManager;
    Calendar cl;
    SimpleDateFormat showDate, showTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tugas);

        btnBackTugas = findViewById(R.id.btn_back_tugas);
        btnBackTugas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mContext = this;
        mApiService = UtilsApi.getAPIService();
        sharedPrefManager = new SharedPrefManager(this);
        imageView = findViewById(R.id.img_tugas);

//        AlertDialog.Builder mBuilder = new AlertDialog.Builder(TugasActivity.this);
//        View mView = getLayoutInflater().inflate(R.layout.dialog_absen, null);
//        final Button mHadir = mView.findViewById(R.id.btn_share);
//        final TextView txtClose = mView.findViewById(R.id.txtclose);
//        mBuilder.setView(mView);
//        dialog = mBuilder.create();

        getLocation();
        ButterKnife.bind(this);
        submitTask();

    }

    private void submitTask() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestTask();
            }
        });
    }

    private void requestTask() {
        cl = Calendar.getInstance();
        showDate = new SimpleDateFormat("yyyy-dd-MM");
        showTime = new SimpleDateFormat("HH:mm");

        String idAnggota = sharedPrefManager.getSpId();
        String idStatus = sharedPrefManager.getSPTask();
        String strDate = showDate.format(cl.getTime());
        String strTime = showTime.format(cl.getTime());
        String keterangan = edtKeterangan.getText().toString();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);
        if (!validate()) {
            return;
        }

        mApiService.taskPost(
                idAnggota, idStatus, latitude, longitude, keterangan, strDate, strTime)
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
                                    sharedPrefManager.saveSPBooleanTugas(SharedPrefManager.SP_TASK, true);
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
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Submit Gagal.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //method untuk mendapatkan lokasi
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            final Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {

                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Getting Location...");
                progressDialog.show();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                                try {
                                    addresses = geocoder.getFromLocation(lat, lon, 1);

                                    String address = addresses.get(0).getAddressLine(0);
                                    String area = addresses.get(0).getLocality();
                                    String city = addresses.get(0).getAdminArea();
                                    String country = addresses.get(0).getCountryName();

                                    String fullAddress = address + ", " + area + ", " + city + ", " + country;

                                    edtLokasi.setText(fullAddress);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }, 3000);

            } else {
                edtLokasi.setText("Tidak dapat menemukan lokasi.");
            }
        }

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

    public boolean validate() {
        boolean valid = true;

        String lokasi = edtLokasi.getText().toString();
        String keterangan = edtKeterangan.getText().toString();

        if (lokasi.isEmpty() || keterangan == "Tidak dapat menemukan lokasi.") {
            valid = false;
        } else {
            edtLokasi.setError(null);
        }

        if (keterangan.isEmpty()) {
            edtKeterangan.setError("Anda belum mengisi keterangan");
            valid = false;
        } else {
            edtKeterangan.setError(null);
        }

        String location = "https://maps.google.com/?q=" + lat + "," + lon;

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Status : Tugas\nKeterangan : " + edtKeterangan.getText().toString()
                + "\n \n" + location);
        try {
            startActivityForResult(whatsappIntent, 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(TugasActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    public void onSubmitFailed(View view) {
        Snackbar.make(view, "Submit Gagal", Snackbar.LENGTH_LONG).show();
        submitButton.setEnabled(true);
    }

}
