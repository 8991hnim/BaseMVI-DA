package m.tech.basemvi.api

import androidx.lifecycle.LiveData
import m.tech.basemvi.models.Post
import m.tech.basemvi.util.ApiResponse
import retrofit2.http.GET

interface ApiService {
    @GET("posts")
    fun getPosts(): LiveData<ApiResponse<List<Post>>>
}