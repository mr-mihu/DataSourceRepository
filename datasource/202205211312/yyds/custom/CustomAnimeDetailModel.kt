package com.skyd.imomoe.model.impls.custom

import com.google.gson.reflect.TypeToken
import com.skyd.imomoe.bean.*
import com.skyd.imomoe.model.interfaces.IAnimeDetailModel
import com.skyd.imomoe.net.RetrofitManager

class CustomAnimeDetailModel : IAnimeDetailModel {
    override suspend fun getAnimeDetailData(
        partUrl: String
    ): Triple<ImageBean, String, ArrayList<Any>> {
        val myJsonParser: MyJsonParser<BaseBean> =
            MyJsonParser(typeElementName = "type", targetClass = BaseBean::class.java)
                .addTypeElementValueWithClassType("AnimeCover1", AnimeCover1Bean::class.java)
                .addTypeElementValueWithClassType("AnimeDescribe1", AnimeDescribe1Bean::class.java)
                .addTypeElementValueWithClassType("AnimeInfo1", AnimeInfo1Bean::class.java)
                .addTypeElementValueWithClassType("Header1", Header1Bean::class.java)
                .addTypeElementValueWithClassType(
                    "HorizontalRecyclerView1",
                    HorizontalRecyclerView1Bean::class.java
                )
                .addTypeElementValueWithClassType("ImageBean", ImageBean::class.java)
                .create()

        val raw = RetrofitManager
            .get()
            .create(DataSourceService::class.java)
            .getAnimeDetailData(CustomConst.MAIN_URL + partUrl)

        val first = raw.data!!.first!!

        val second: String = raw.data.second

        val third = myJsonParser.fromJson<ArrayList<Any>>(
            raw.data.third.toString(),
            object : TypeToken<ArrayList<BaseBean>>() {}.type
        )

        return Triple(first, second, third)
    }
}