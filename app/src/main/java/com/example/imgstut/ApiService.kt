
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("upload")
    fun uploadImageAndDescription(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<ResponseBody>

}

interface ApiService2 {
    @Multipart
    @POST("upload")
    fun uploadVideoAndDescription(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<ResponseBody>
}