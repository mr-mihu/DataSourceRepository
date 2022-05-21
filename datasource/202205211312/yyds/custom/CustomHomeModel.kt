package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.model.interfaces.IHomeModel
import com.skyd.imomoe.net.RetrofitManager

class CustomHomeModel : IHomeModel {
    override suspend fun getAllTabData(): ArrayList<TabBean> {
        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getAllTabData()
        if (raw.code == 200) {
            return raw.data ?: arrayListOf()
        } else {
            error("code: ${raw.code}\nmessage: $raw.message")
        }
    }
}