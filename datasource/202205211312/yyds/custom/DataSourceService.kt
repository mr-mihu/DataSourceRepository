package com.skyd.imomoe.model.impls.custom

import com.google.gson.JsonElement
import com.skyd.imomoe.bean.ImageBean
import com.skyd.imomoe.bean.TabBean
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

internal interface DataSourceService {
    @GET("${CustomConst.MAIN_URL}/getSearchData")
    suspend fun getAllTabData(): DataWrapper<ArrayList<TabBean>>

    @GET("${CustomConst.MAIN_URL}/getSearchData")
    suspend fun getSearchData(@Query("keyword") keyword: String): DataWrapper<Pair<JsonElement, JsonElement>>

    @GET
    suspend fun getAnimeDetailData(@Url url: String): DataWrapper<Triple<ImageBean?, String, JsonElement>>

    @GET
    suspend fun getPlayData(@Url url: String): DataWrapper<Triple<JsonElement, JsonElement, JsonElement>>

    @GET
    suspend fun getAnimeDownloadUrl(@Url url: String): DataWrapper<JsonElement>
}