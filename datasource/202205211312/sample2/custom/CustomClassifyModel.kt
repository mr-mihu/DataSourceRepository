package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import android.view.View
import com.skyd.imomoe.bean.ClassifyBean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.model.interfaces.IClassifyModel
import com.skyd.imomoe.util.Util.toEncodedUrl
import com.skyd.imomoe.util.html.source.GettingCallback
import com.skyd.imomoe.util.html.source.web.GettingUtil
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.lang.ref.SoftReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class CustomClassifyModel : IClassifyModel {
    private var mActivity: SoftReference<Activity>? = null

    override fun setActivity(activity: Activity) {
        mActivity = SoftReference(activity)
    }

    override fun clearActivity() {
        GettingUtil.instance.releaseAll()
        mActivity = null
    }

    override suspend fun getClassifyData(
        partUrl: String
    ): Pair<ArrayList<Any>, PageNumberBean?> {
        val classifyList: ArrayList<Any> = ArrayList()
        var pageNumberBean: PageNumberBean? = null
        val url: String = (CustomConst.MAIN_URL + partUrl).toEncodedUrl()
        val document = JsoupUtil.getDocument(url)
        val areaElements: Elements = document.getElementsByClass("area")
        for (i in areaElements.indices) {
            val areaChildren: Elements = areaElements[i].children()
            for (j in areaChildren.indices) {
                when (areaChildren[j].className()) {
                    "fire l" -> {
                        val fireLChildren: Elements = areaChildren[j].children()
                        for (k in fireLChildren.indices) {
                            when (fireLChildren[k].className()) {
                                "lpic" -> {
                                    classifyList.addAll(
                                        CustomParseHtmlUtil.parseLpic(fireLChildren[k], url)
                                    )
                                }
                                "pages" -> {
                                    pageNumberBean =
                                        CustomParseHtmlUtil.parseNextPages(fireLChildren[k])
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(classifyList, pageNumberBean)
    }

    override suspend fun getClassifyTabData(): ArrayList<ClassifyBean> =
        suspendCancellableCoroutine { cancellableContinuation ->
            val activity = mActivity?.get()
            if (activity == null || activity.isDestroyed) throw Exception("activity不存在或状态错误")
            activity.runOnUiThread {
                GettingUtil.instance.activity(activity).url(CustomConst.MAIN_URL + "/list/").start(
                    object : GettingCallback {
                        override fun onGettingSuccess(webView: View?, html: String) {
                            GettingUtil.instance.release()
                            val classifyTabList: ArrayList<ClassifyBean> = ArrayList()
                            val document = Jsoup.parse(html)
                            val fireLElements: Elements = document.getElementsByClass("area")
                                .select("[class=fire l]")
                            for (i in fireLElements.indices) {
                                val areaChildren: Elements = fireLElements[i].children()
                                for (j in areaChildren.indices) {
                                    when (areaChildren[j].className()) {
                                        "search-list" -> {
                                            classifyTabList.addAll(
                                                CustomParseHtmlUtil.parseSearchList(areaChildren[j])
                                            )
                                        }
                                    }
                                }
                            }
                            cancellableContinuation.resume(classifyTabList)
                        }

                        override fun onGettingError(webView: View?, url: String?, errorCode: Int) {
                            GettingUtil.instance.release()
                            cancellableContinuation.resumeWithException(Exception("onGettingError,url:$url,errorCode:$errorCode"))
                        }
                    },
                    Const.Request.USER_AGENT_ARRAY[Random.nextInt(Const.Request.USER_AGENT_ARRAY.size)]
                )
            }
        }
}
