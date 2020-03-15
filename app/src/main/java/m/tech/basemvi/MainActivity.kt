package m.tech.basemvi

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import m.tech.basemvi.ui.MainStateEvent
import m.tech.basemvi.ui.MainStateEvent.*
import m.tech.basemvi.viewmodels.MainViewModel
import m.tech.basemvi.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    val TAG = "AppDebug"

    @Inject
    lateinit var provider: ViewModelProviderFactory

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, provider).get(MainViewModel::class.java)

        subscribeObserver()

        start.setOnClickListener {
            viewModel.setStateEvent(GetPostsDoNotSaveDb())
        }

        stop.setOnClickListener {
            viewModel.cancelActiveJobs()
        }
    }

    private fun subscribeObserver() {
        viewModel.dataState.observe(this, Observer { dataState ->
            Log.d(TAG, "Loading...")

            dataState.error?.let { event ->
                Log.d(TAG, "Error handling... Cancel loading here")
                event.getContentIfNotHandled()?.let {
                    Log.d(TAG, "Error not handled $it")
                }
            }

            dataState.data?.let { viewState ->
                Log.d(TAG, "Data handling... Cancel loading here")
                viewState.posts?.let {
                    Log.d(TAG, "Got posts returned from API, setting to ViewState... ${it.size}")
                    viewModel.setPosts(it)
                }
            }
        })

        viewModel.viewState.observe(this, Observer { viewState ->
            viewState.posts?.let {
                Log.d(
                    TAG,
                    "Got posts returned from DataState, setting to RecyclerView... ${it.size}"
                )
            }
        })
    }

}













