package com.learnteachcenter.ltcreikiclockv3.model.remote

class Resource<T> private constructor(val status: Status, val data: T?, val error: Error?) {
    companion object {

        fun <T> loading(data: T?): Resource<T> {
            return Resource(
                Status.LOADING,
                data,
                null
            )
        }

        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(data: T?, error: Error?): Resource<T> {
            return Resource(
                Status.ERROR,
                data,
                error
            )
        }

    }
}