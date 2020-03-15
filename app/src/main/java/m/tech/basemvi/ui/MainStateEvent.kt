package m.tech.basemvi.ui

sealed class MainStateEvent {
    class GetPosts : MainStateEvent()
    class GetPostsDoNotSaveDb : MainStateEvent()
}