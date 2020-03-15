package m.tech.basemvi.di

import m.tech.basemvi.di.main.MainScope
import dagger.Module
import dagger.android.ContributesAndroidInjector
import m.tech.basemvi.MainActivity
import m.tech.basemvi.di.main.MainFragmentBuildersModule
import m.tech.basemvi.di.main.MainModule
import m.tech.basemvi.di.main.MainViewModelModule

@Module
abstract class ActivityBuilderModule {

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainFragmentBuildersModule::class, MainModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}