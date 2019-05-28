package com.learnteachcenter.ltcreikiclockv3.model.datasources.remote;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;
import com.learnteachcenter.ltcreikiclockv3.app.AppExecutors;

// CacheObject: Type for the Resource data. (database cache)
// RequestObject: Type for the API response. (network request)
public abstract class NetworkBoundResource<CacheObject, RequestObject> {

    private static final String TAG = "Reiki";

    private AppExecutors appExecutors;
    private MediatorLiveData<Resource<CacheObject>> results = new MediatorLiveData<>();

    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();
    }

    private void init() {
        // Update LiveData for loading status
        results.setValue((Resource<CacheObject>) Resource.loading(null));

        // observe LiveData source from local DB
        final LiveData<CacheObject> dbSource = loadFromDatabase();

        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {
                results.removeSource(dbSource);

                if(shouldFetch(cacheObject)) {
                    fetchFromNetwork(dbSource);
                } else {
                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(Resource.success(cacheObject));
                        }
                    });
                }
            }
        });
    }

    /**
     * 1) observe local db
     * 2) if <condition/> query the network
     * 3) stop observing the local db
     * 4) insert new data into local db
     * 5) begini observing local db again to see the refreshed data from network
     * @param dbSource
     */
    private void fetchFromNetwork(final LiveData<CacheObject> dbSource) {

        Log.d(TAG, "fetchFromNetwork: called.");

        // update LiveData for loading status
        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(@Nullable CacheObject cacheObject) {
                setValue(Resource.loading(cacheObject));
            }
        });

        final LiveData<ApiResponse<RequestObject>> apiResponse = createNetworkCall();

        results.addSource(apiResponse, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(@Nullable final ApiResponse<RequestObject> requestObjectApiResponse) {
                results.removeSource(dbSource);
                results.removeSource(apiResponse);

                /*
                    3 cases:
                     1) ApiSuccessResponse
                     2) ApiErrorResponse
                     3) ApiEmptyResponse
                 */

                if(requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse) {
                    Log.d(TAG, "onChanged: ApiSuccessResponse");

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {

                            // save the response to the local db
                            saveNetworkCallResult((RequestObject) processResponse((ApiResponse.ApiSuccessResponse) requestObjectApiResponse));

                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    results.addSource(loadFromDatabase(), new Observer<CacheObject>() {
                                        @Override
                                        public void onChanged(@Nullable CacheObject cacheObject) {
                                            setValue(Resource.success(cacheObject));
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse) {

                    Log.d(TAG, "onChanged: ApiEmptyResponse");

                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            results.addSource(loadFromDatabase(), new Observer<CacheObject>() {
                                @Override
                                public void onChanged(@Nullable CacheObject cacheObject) {
                                    setValue(Resource.success(cacheObject));
                                }
                            });
                        }
                    });
                }
                else if(requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse) {

                    Log.d(TAG, "onChanged: ApiErrorResponse");

                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(@Nullable CacheObject cacheObject) {
                            setValue(
                                    Resource.error(
                                            ((ApiResponse.ApiErrorResponse) requestObjectApiResponse).getErrorMessage(),
                                    cacheObject
                                    )
                            );
                        }
                    });
                }
            }
        });
    }

    private CacheObject processResponse(ApiResponse.ApiSuccessResponse response) {
        return (CacheObject) response.getBody();
    }

    private void setValue(Resource<CacheObject> newValue) {
        if(results.getValue() != newValue) {
            results.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveNetworkCallResult(@NonNull RequestObject item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable CacheObject data);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<CacheObject> loadFromDatabase();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createNetworkCall();

    // Returns a LiveData object that represents the source that's implemented
    // in the base class.
    public final LiveData<Resource<CacheObject>> asLiveData() { return results; }
}
