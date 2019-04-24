package com.mg.eventbus.response

data class BaseResponse<T> (var status: Int = 0,
                        var message: String? = null,
                        var data: T? = null) {

    val isSucceed: Boolean
        get() = status == 1

    companion object {
        const val SUCCESS = 1
        const val FAIL = 0
    }

}