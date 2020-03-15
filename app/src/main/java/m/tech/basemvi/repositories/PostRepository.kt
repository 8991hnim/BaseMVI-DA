package m.tech.basemvi.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Job
import m.tech.basemvi.SessionManager
import m.tech.basemvi.api.ApiService
import m.tech.basemvi.models.Post
import m.tech.basemvi.persistence.PostDao
import m.tech.basemvi.ui.MainViewState
import m.tech.basemvi.util.AbsentLiveData
import m.tech.basemvi.util.ApiResponse
import m.tech.basemvi.util.DataState
import javax.inject.Inject

class PostRepository
@Inject
constructor(
    val sessionManager: SessionManager,
    val postDao: PostDao,
    val apiService: ApiService
) : JobManager("PostRepository") {

    /*
      Make a request and save response to db

   */
    fun getPosts(): LiveData<DataState<MainViewState>> {
        return object :
            NetworkBoundResource<List<Post>, List<Post>, MainViewState>(sessionManager) {
            override fun createViewStateFromApiResponse(response: List<Post>): MainViewState {
                //do not care if shouldLoadFromDb is true
                return MainViewState()
            }

            override fun saveCallResult(response: List<Post>) {
                postDao.insertPosts(response)
            }

            override fun shouldLoadFromDb(): Boolean {
                return true
            }

            override fun createCall(): LiveData<ApiResponse<List<Post>>> {
                return apiService.getPosts()
            }

            override fun shouldFetch(data: MainViewState?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<MainViewState> {
                return postDao.getPosts()
                    .switchMap {
                        object : LiveData<MainViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = MainViewState(posts = it)
                            }
                        }
                    }
            }

            override fun setJob(job: Job) {
                addJob("getPosts", job)
            }

        }.asLiveData()
    }

    /*
        Make a request no need to save response data to db (like POST request)

     */
    fun getPostsDoNotSaveDb(): LiveData<DataState<MainViewState>> {
        return object :
            NetworkBoundResource<List<Post>, List<Post>, MainViewState>(sessionManager) {
            override fun createViewStateFromApiResponse(response: List<Post>): MainViewState {
                //do not care if shouldLoadFromDb is true
                return MainViewState(
                    posts = response
                )
            }

            override fun saveCallResult(response: List<Post>) {
                //do not care b/c no save data
            }

            override fun shouldLoadFromDb(): Boolean {
                return false
            }

            override fun createCall(): LiveData<ApiResponse<List<Post>>> {
                return apiService.getPosts()
            }

            override fun shouldFetch(data: MainViewState?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<MainViewState> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                addJob("getPostsDoNotSaveDb", job)
            }

        }.asLiveData()
    }
}