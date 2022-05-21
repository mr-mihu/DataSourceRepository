package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.IMonthAnimeModel

class CustomMonthAnimeModel : IMonthAnimeModel {
    override suspend fun getMonthAnimeData(partUrl: String): Pair<ArrayList<Any>, PageNumberBean?> {
        return Pair(ArrayList(), null)
    }
}