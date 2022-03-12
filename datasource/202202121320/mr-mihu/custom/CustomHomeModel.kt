package com.skyd.imomoe.model.impls.custom

import android.widget.Toast
import com.skyd.imomoe.bean.TabBean
import com.skyd.imomoe.config.Api
import com.skyd.imomoe.config.UnknownActionUrl
import com.skyd.imomoe.model.interfaces.IHomeModel
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.eventbus.SelectHomeTabEvent
import com.skyd.imomoe.util.showToast
import org.greenrobot.eventbus.EventBus
import org.jsoup.select.Elements

class CustomHomeModel : IHomeModel {
    override suspend fun getAllTabData(): ArrayList<TabBean> {
        "请稍后正在加载数据...".showToast(Toast.LENGTH_LONG)
        return ArrayList<TabBean>().apply {
            try{
                val document = JsoupUtil.getDocument(Api.MAIN_URL)
                //主标题
                val mainTabs: Elements = document.select("div#menu .first a")
                for (i in mainTabs.indices) {
                    val url = mainTabs[i].attr("href")
                    if(!url.startsWith("/")){ //非站内链接
                        continue
                    }
                    val name = mainTabs[i].text()
                    add(TabBean(url, Api.MAIN_URL + url, name))
                    UnknownActionUrl.actionMap[url] = object : UnknownActionUrl.Action {
                        override fun action() {
                            EventBus.getDefault().post(SelectHomeTabEvent(url))
                        }
                    }
                }
            }catch (e:Exception){
                //首页
                val actionUrl = "/"
                add(TabBean(actionUrl, Api.MAIN_URL, "首页"))
                UnknownActionUrl.actionMap[actionUrl] = object : UnknownActionUrl.Action {
                    override fun action() {
                        EventBus.getDefault().post(SelectHomeTabEvent(actionUrl))
                    }
                }
            }
        }
    }
}