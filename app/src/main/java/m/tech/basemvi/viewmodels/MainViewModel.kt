package m.tech.basemvi.viewmodels

import androidx.lifecycle.LiveData
import m.tech.basemvi.models.Post
import m.tech.basemvi.repositories.PostRepository
import m.tech.basemvi.ui.MainStateEvent
import m.tech.basemvi.ui.MainStateEvent.*
import m.tech.basemvi.ui.MainViewState
import m.tech.basemvi.util.DataState
import javax.inject.Inject

class MainViewModel
@Inject
constructor(
    private val repository: PostRepository
) : BaseViewModel<MainStateEvent, MainViewState>() {
    override fun initNewViewState(): MainViewState {
        return MainViewState()
    }

    override fun handleStateEvent(stateEvent: MainStateEvent): LiveData<DataState<MainViewState>> {
        when (stateEvent) {
            is GetPosts -> {
                return repository.getPosts()
            }

            is GetPostsDoNotSaveDb -> {
                return repository.getPostsDoNotSaveDb()
            }
        }
    }

    fun setPosts(list: List<Post>) {
        val update = getCurrentViewStateOrNew()
        update.posts = list
        setViewState(update)
    }

    fun cancelActiveJobs() {
        repository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}