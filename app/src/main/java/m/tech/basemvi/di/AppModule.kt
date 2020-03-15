package m.tech.basemvi.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import m.tech.basemvi.util.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import m.tech.basemvi.R
import m.tech.basemvi.SessionManager
import m.tech.basemvi.api.ApiService
import m.tech.basemvi.persistence.AppDatabase
import m.tech.basemvi.persistence.AppDatabase.Companion.DATABASE_NAME
import m.tech.basemvi.persistence.PostDao
import m.tech.basemvi.util.Constants.BASE_URL
import m.tech.basemvi.util.PreferenceKeys
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule {
    @Module
    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun provideSharedPreferences(application: Application): SharedPreferences {
            return application.getSharedPreferences(
                PreferenceKeys.APP_PREFERENCES,
                Context.MODE_PRIVATE
            )
        }

        @Singleton
        @Provides
        @JvmStatic
        fun provideSharedPrefsEditor(sharedPreferences: SharedPreferences): SharedPreferences.Editor {
            return sharedPreferences.edit()
        }

        @Singleton
        @Provides
        @JvmStatic
        fun provideRetrofitBuilder(): Retrofit.Builder {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
        }

        //TODO: change this --- (optional: provide api services in different modules)
        @Singleton
        @Provides
        @JvmStatic
        fun provideApiService(retrofitBuilder: Retrofit.Builder): ApiService {
            return retrofitBuilder.build().create(ApiService::class.java)
        }

        @Singleton
        @Provides
        @JvmStatic
        fun provideAppDatabase(app: Application): AppDatabase {
            return Room
                .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }

        //TODO:(optional provide DAO here or in different modules)
        @Singleton
        @Provides
        @JvmStatic
        fun providePostDao(db: AppDatabase): PostDao {
            return db.getPostDao()
        }

        @Singleton
        @Provides
        fun provideRequestOptions(): RequestOptions {
            return RequestOptions
                .placeholderOf(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
        }

        @Singleton
        @Provides
        @JvmStatic
        fun provideRequestManager(
            application: Application,
            requestOptions: RequestOptions
        ): RequestManager {
            return Glide.with(application)
                .setDefaultRequestOptions(requestOptions)
        }


        @Singleton
        @Provides
        @JvmStatic
        fun provideSessionManager(application: Application): SessionManager {
            return SessionManager(application)
        }
    }


}