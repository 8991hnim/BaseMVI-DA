package m.tech.basemvi.di.main

import dagger.Module
import dagger.Provides
import m.tech.basemvi.SessionManager
import m.tech.basemvi.api.ApiService
import m.tech.basemvi.persistence.PostDao
import m.tech.basemvi.repositories.PostRepository

@Module
class MainModule {

    @Module
    companion object {
        @MainScope
        @Provides
        fun provideMainRepository(
            sessionManager: SessionManager,
            postDao: PostDao,
            apiService: ApiService
        ): PostRepository {
            return PostRepository(sessionManager, postDao, apiService)
        }
    }


}