package cz.jaro.rozvrh.suplovani

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface SuplovaniApi {

    @FormUrlEncoded
    @POST("zmeny.aspx")
    fun zmeny(
        @HeaderMap headers: Map<String, String>,
        @Field("__EVENTVALIDATION") eventValidation: String,
        @Field("__VIEWSTATE") viewState: String,
        @Field("DateEdit\$State") dateEditState: String,
        @Field("DateEdit") dateEdit: String,
        @Field("FilterDropDown_VI") filterDropDownVI: Int,
        @Field("FilterDropDown") filterDropDown: String
    ): Call<ResponseBody>

    @POST("zmeny.aspx")
    suspend fun zmenyGet(
        @HeaderMap headers: Map<String, String>,
    ): Response<ResponseBody>
}