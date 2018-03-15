package com.iqbal.attendance.kehadiran;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IzinActivity extends Activity{

    Button btnBackIzin;
    EditText edtLocation;
    EditText edtDateFrom;
    EditText edtDateTo;
    EditText edtKeterangan;
    ImageView imageView;
    Button submitButton;
    Geocoder geocoder;
    LocationManager locationManager;
    double lat;
    double lon;
    List<Address> addresses;
    static final int REQUEST_LOCATION = 1;
    Calendar currentCalendar;
    Uri imageUri = null;
    Context mContext;
    BaseApiService mApiService;
    SharedPrefManager sharedPrefManager;
    Calendar cl;
    SimpleDateFormat showDate, showTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_izin);

        init();
        btnBackIzin.setOnClickListener(new View.OnClickListener() {
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

        getLocation();
        onStartDate();
        onFinishDate();
        submitAttend();

    }

    private void submitAttend() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestAttend(v);
            }
        });
    }

    private void requestAttend(final View view) {
        cl = Calendar.getInstance();
        showDate = new SimpleDateFormat("yyyy-dd-MM");
        showTime = new SimpleDateFormat("HH:mm");

        String idAnggota = sharedPrefManager.getSpId();
        String idStatus = sharedPrefManager.getSPPermit();
        String strDate = showDate.format(cl.getTime());
        String startDate = edtDateFrom.getText().toString();
        String endDate = edtDateTo.getText().toString();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);
        String keterangan = edtKeterangan.getText().toString();
        if (!validate()){
            return;
        }

        mApiService.permitPost(
                idAnggota, idStatus, strDate, startDate, endDate, latitude, longitude, keterangan)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("error").equals("false")) {
                                    // Jika login berhasil maka data nama yang ada di response API
                                    // akan diparsing ke activity selanjutnya.
//                                    Toast.makeText(mContext, "Login Berhasil", Toast.LENGTH_SHORT).show();
                                    String id = jsonRESULTS.getJSONObject("UserItem").getString("id_anggota");
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_ID, id);
                                    // Shared Pref ini berfungsi untuk menjadi trigger session login
                                    sharedPrefManager.saveSPBooleanIzin(SharedPrefManager.SP_PERMIT, true);
                                    startActivity(new Intent(mContext, MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                } else {
                                    // Jika login gagal
                                    onSubmitFailed(view);
//                                    String error_message = jsonRESULTS.getString("error_msg");
//                                    Toast.makeText(mContext, error_message, Toast.LENGTH_SHORT).show();
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

    private void onSubmitFailed(View v) {
        Snackbar.make(v, "Submit Gagal", Snackbar.LENGTH_LONG).show();
        submitButton.setEnabled(true);
    }

    private boolean validate() {

        boolean valid = true;

        String lokasi = edtLocation.getText().toString();
        String keterangan = edtKeterangan.getText().toString();

        if (lokasi.isEmpty() || keterangan == "Tidak dapat menemukan lokasi.") {
            valid = false;
        } else {
            edtLocation.setError(null);
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
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Status : Izin\n" +
                "Dari : " + edtDateFrom.getText().toString() + "\n" +
                "Sampai : " + edtDateTo.getText().toString() + "\n" +
                "Keterangan : " + edtKeterangan.getText().toString() + "\n \n" +
                location);
        try {
            startActivityForResult(whatsappIntent, 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(IzinActivity.this, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Share gagal.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onFinishDate() {
        edtDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar = Calendar.getInstance();
                int year = currentCalendar.get(Calendar.YEAR);
                int month = currentCalendar.get(Calendar.MONTH);
                int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(IzinActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                edtDateTo.setText(selectedYear + "-" + selectedMonth + "-" + selectedDay);
                                currentCalendar.set(selectedYear, selectedMonth, selectedDay);
                            }
                        }, year, month, day);
                mDatePicker.show();
            }
        });
    }

    private void onStartDate() {
        edtDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCalendar = Calendar.getInstance();
                int year = currentCalendar.get(Calendar.YEAR);
                int month = currentCalendar.get(Calendar.MONTH);
                int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(IzinActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                edtDateFrom.setText(selectedYear + "-" + selectedMonth + "-" + selectedDay);
                                currentCalendar.set(selectedYear, selectedMonth, selectedDay);
                            }
                        }, year, month, day);
                mDatePicker.show();
            }
        });
    }

    private void getLocation() {
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

                                    edtLocation.setText(fullAddress);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }, 3000);

            } else {
                edtLocation.setError("Tidak dapat menemukan lokasi.");
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

    private void init() {
        btnBackIzin = findViewById(R.id.btn_back_izin);
        edtLocation = findViewById(R.id.edt_lokasi_izin);
        edtDateFrom = findViewById(R.id.edt_mulai_izin);
        edtDateTo = findViewById(R.id.edt_selesai_izin);
        edtKeterangan = findViewById(R.id.edt_keterangan_izin);
        imageView = findViewById(R.id.img_izin);
        submitButton = findViewById(R.id.btn_submit_izin);
    }

}