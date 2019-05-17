package com.learnteachcenter.ltcreikiclockv3.model.basic

class Resource<T> private constructor(val status: Status, val data: T?, val message: String?) {
    companion object {

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(data: T?, msg: String?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

    }
}