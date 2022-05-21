package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import com.skyd.imomoe.bean.ClassifyBean
import com.skyd.imomoe.bean.ClassifyTab1Bean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.model.interfaces.IClassifyModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.Util.toEncodedUrl
import com.skyd.imomoe.util.html.source.web.GettingUtil
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.lang.ref.SoftReference

class CustomClassifyModel : IClassifyModel {
    private var mActivity: SoftReference<Activity>? = null

    override fun setActivity(activity: Activity) {
        mActivity = SoftReference(activity)
    }

    override fun clearActivity() {
        GettingUtil.instance.releaseAll()
        mActivity = null
    }

    override suspend fun getClassifyData(partUrl: String): Pair<ArrayList<Any>, PageNumberBean?> {
        val classifyList: ArrayList<Any> = ArrayList()
        val url: String = (Api.MAIN_URL + partUrl).toEncodedUrl()
        val document = JsoupUtil.getDocument(url)
        //获取节点
        var element = document.body()

        //分页数据
        var pageElement: Elements = element.select("div.pagination")
        //有分页
        var pageNumberBean: PageNumberBean? = CustomParseHtmlUtil.parseNextPages(pageElement)

        //非首页标签
        CustomAnimeShowModel().setNotHomeDate(partUrl, element, classifyList)

        return Pair(classifyList, pageNumberBean)
    }

    override suspend fun getClassifyTabData(): ArrayList<ClassifyBean> {
        val classifyTabList: ArrayList<ClassifyBean> = ArrayList()
        var document = JsoupUtil.getDocument(Api.MAIN_URL)

        var fortElements: Elements = document.select("body>#menu>.area dl>dt>a>font")

        for (fort in fortElements) {
            //过滤不需要的数据
            var parent = fort.parent() ?: continue
            if ("a" != parent.tagName()) continue
            if ("#" != parent.attr("href")) continue

            var title = fort.text()
            if (title.contains("小说") || title.contains("图片")) continue
            var classifyDataList = getClassifyDataList(fort)
            classifyTabList.add(
                ClassifyBean("", title, classifyDataList)
            )
        }

//        val activity = mActivity?.get()
//        if (activity == null || activity.isDestroyed) throw Exception("activity不存在或状态错误")
//        activity.runOnUiThread {
//            classifyTabList.forEach {
//                it.classifyDataList.forEach { item ->
//                    UnknownActionUrl.actionMap[item.actionUrl] =
//                        object : UnknownActionUrl.Action {
//                            override fun action() {
//                                activity.startActivity(
//                                    Intent(activity, ClassifyActivity::class.java)
//                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                        .putExtra("partUrl", item.actionUrl)
//                                )
//                            }
//                        }
//                }
//            }
//        }

        return classifyTabList
    }

    /**
     * 获取分类列表
     */
    private fun getClassifyDataList(fort: Element?): ArrayList<ClassifyTab1Bean> {
        val classifyDataList = ArrayList<ClassifyTab1Bean>()
        if (fort == null) return classifyDataList

        var parent = fort.parent()?.parent() ?: return classifyDataList
        var menuList = parent.nextElementSibling() ?: return classifyDataList
        var aTabs = menuList.select("font>dd>a")

        for (a in aTabs) {
            classifyDataList.add(
                ClassifyTab1Bean(
                    a.attr("href").replace(Api.MAIN_URL, ""),
                    a.attr("href"),
                    a.text()
                )
            )
        }
        return classifyDataList
    }
}
