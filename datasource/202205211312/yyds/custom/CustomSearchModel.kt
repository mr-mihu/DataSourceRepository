package com.skyd.imomoe.model.impls.custom

import com.google.gson.reflect.TypeToken
import com.skyd.imomoe.bean.AnimeCover3Bean
import com.skyd.imomoe.bean.BaseBean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.ISearchModel
import com.skyd.imomoe.net.RetrofitManager


class CustomSearchModel : ISearchModel {
    override suspend fun getSearchData(
        keyWord: String,
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        val myJsonParser: MyJsonParser<BaseBean> =
            MyJsonParser(typeElementName ="type", targetClass = BaseBean::class.java)
                .addTypeElementValueWithClassType("AnimeCover3", AnimeCover3Bean::class.java)
                .create()

        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getSearchData(keyWord)

        val first = myJsonParser.fromJson<ArrayList<Any>>(
            raw.data?.first.toString(),
            object : TypeToken<ArrayList<BaseBean>>() {}.type
        )

        val second = myJsonParser.fromJson<PageNumberBean?>(
            raw.data?.second.toString(),
            object : TypeToken<PageNumberBean?>() {}.type
        )

        return first to second
    }
}