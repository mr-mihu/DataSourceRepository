package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.IRankListModel

class CustomRankListModel : IRankListModel {
    override suspend fun getRankListData(partUrl: String): Pair<MutableList<Any>, PageNumberBean?> {
        return Pair(ArrayList(), null)
    }
}
