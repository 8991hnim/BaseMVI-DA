package m.tech.basemvi.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import m.tech.basemvi.SessionManager
import m.tech.basemvi.util.ApiResponse
import m.tech.basemvi.util.ApiResponse.*
import m.tech.basemvi.util.Constants.ERROR_NO_INTERNET
import m.tech.basemvi.util.Constants.ERROR_UNKNOWN
import m.tech.basemvi.util.Constants.NETWORK_TIMEOUT
import m.tech.basemvi.util.Constants.UNABLE_TO_RESOLVE_HOST
import m.tech.basemvi.util.DataState

abstract class NetworkBoundResource<ResponseObject, CacheObject, ViewStateType>(
    private val sessionManager: SessionManager
) {

    private val TAG = "AppDebug"

    private val result = MediatorLiveData<DataState<ViewStateType>>()
    private lateinit var job: CompletableJob
    private lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.Loading())

        val dbSource = loadFromDb()

        result.addSource(dbSource) {
            result.removeSource(dbSource)
            if (shouldFetch(it)) {
                if (sessionManager.isConnectedToTheInternet()) {
                    fetchFromNetwork()
                } else {
                    onCompleteJob(
                        DataState.Error(
                            message = ERROR_NO_INTERNET,
                            data = it
                        )
                    )
                }
            } else {
                onCompleteJob(DataState.Success(data = it))
            }
        }


    }

    private fun fetchFromNetwork() {
        coroutineScope.launch {
            withContext(Main) {
                val apiResponse = createCall();

                result.addSource(apiResponse) { response ->
                    result.removeSource(apiResponse)
                    coroutineScope.launch {
                        handleApiResponse(response)
                    }
                }
            }
        }

        //TODO: set timeout for internet request
        GlobalScope.launch(IO) {
            delay(NETWORK_TIMEOUT)

            if (!job.isCompleted) {
                Log.e(TAG, "NetworkBoundResource: Job network timeout.")
                job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
            }
        }
    }

    private suspend fun handleApiResponse(response: ApiResponse<ResponseObject>) {
        when (response) {
            is ApiSuccessResponse -> {
                if (shouldLoadFromDb()) {
                    saveCallResult(processResponse(response))

                    withContext(Main) {
                        result.addSource(loadFromDb()) {
                            onCompleteJob(
                                DataState.Success(
                                    data = it
                                )
                            )
                        }
                    }
                } else {
                    onCompleteJob(
                        DataState.Success(
                            data = createViewStateFromApiResponse(processResponse(response))
                        )
                    )
                }
            }

            is ApiEmptyResponse -> {
                withContext(Main) {
                    result.addSource(loadFromDb()) {
                        onCompleteJob(
                            DataState.Error(
                                message = "Empty Api Response",
                                data = it
                            )
                        )
                    }
                }
            }

            is ApiErrorResponse -> {
                withContext(Main) {
                    result.addSource(loadFromDb()) {
                        onCompleteJob(
                            DataState.Error(
                                message = response.errorMessage,
                                data = it
                            )
                        )
                    }
                }
            }
        }
    }

    private fun processResponse(response: ApiSuccessResponse<ResponseObject>): ResponseObject =
        response.body


    @UseExperimental(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {
        job = Job()
        job.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.d(TAG, "NetworkBoundResource: Job has been cancelled.")
                        cause?.let {
                            onCompleteJob(DataState.Error(it.message.toString(), null))
                        } ?: onCompleteJob(DataState.Error(ERROR_UNKNOWN, null))
                    } else if (job.isCompleted) {
                        Log.d(TAG, "NetworkBoundResource: Job has been completed.")
                    }
                }
            }
        )
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    private fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        if (result.value != dataState) {
            result.value = dataState
        }
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract fun createViewStateFromApiResponse(response: ResponseObject): ViewStateType

    abstract fun saveCallResult(response: ResponseObject)

    abstract fun shouldLoadFromDb(): Boolean

    abstract fun createCall(): LiveData<ApiResponse<ResponseObject>>

    abstract fun shouldFetch(data: ViewStateType?): Boolean

    abstract fun loadFromDb(): LiveData<ViewStateType>

    abstract fun setJob(job: Job)
}