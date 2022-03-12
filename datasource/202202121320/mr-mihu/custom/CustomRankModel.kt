package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.model.interfaces.IRankModel

class CustomRankModel : IRankModel {
    override suspend fun getRankTabData(): ArrayList<TabBean> {
        return ArrayList()
    }
}
