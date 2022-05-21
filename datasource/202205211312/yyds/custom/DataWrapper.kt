package com.skyd.imomoe.model.impls.custom

import java.io.Serializable

class DataWrapper<T>(
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null
) : Serializable