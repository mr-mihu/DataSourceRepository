package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IEverydayAnimeModel
import org.jsoup.select.Elements

class CustomEverydayAnimeModel : IEverydayAnimeModel {
    override suspend fun getEverydayAnimeData(): Triple<ArrayList<TabBean>, ArrayList<List<Any>>, String> {
        return Triple(ArrayList(), ArrayList(), "")
    }
}