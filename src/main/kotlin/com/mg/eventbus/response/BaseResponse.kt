package com.mg.eventbus.response

class BaseResponse {

    var status: Int = 0
    var message: String? = null
    var data: Any? = null

    val isSucceed: Boolean
        get() = status == 1
}