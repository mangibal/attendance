package com.iqbal.attendance.utils.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by iqbal on 09/03/18.
 */

public interface BaseApiService {

    //Fungsi ini untuk memanggil API
    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> loginRequest(@Field("email") String email,
                                    @Field("password") String password);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> presentPost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("tanggal") String tanggal,
            @Field("jam_masuk") String jam_masuk,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> taskPost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("keterangan") String keterangan,
            @Field("tanggal") String tanggal,
            @Field("jam_masuk") String jam_masuk);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> sickPost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("tanggal") String tanggal,
            @Field("tgl_awal") String tgl_awal,
            @Field("tgl_akhir") String tgl_akhir,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("Keterangan") String keterangan);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> permitPost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("tanggal") String tanggal,
            @Field("tgl_awal") String tgl_awal,
            @Field("tgl_akhir") String tgl_akhir,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("Keterangan") String keterangan);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> leavePost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("tanggal") String tanggal,
            @Field("tgl_awal") String tgl_awal,
            @Field("tgl_akhir") String tgl_akhir,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("Keterangan") String keterangan);

    @FormUrlEncoded
    @POST("detailabsen")
    Call<ResponseBody> remotePost(
            @Field("id_anggota") String id_anggota,
            @Field("status_id") String status_id,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("keterangan") String keterangan,
            @Field("tanggal") String tanggal,
            @Field("jam_masuk") String jam_masuk);

//    @GET("joinabsen1")
//    Call<ResponseUsers> getUserItem();

}
