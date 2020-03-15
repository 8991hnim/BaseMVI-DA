package m.tech.basemvi.ui

import m.tech.basemvi.models.Post

data class MainViewState(
    var posts: List<Post>? = ArrayList()
)