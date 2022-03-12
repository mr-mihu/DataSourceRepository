package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.model.interfaces.IUtil
import com.skyd.imomoe.model.util.JsoupUtil
import com.skyd.imomoe.util.showToast

class CustomUtil : IUtil {

    override fun getDetailLinkByEpisodeLink(episodeUrl: String): String {
        val const = CustomConst()
        var detailPartUrl = episodeUrl.substringAfter(const.actionUrl.ANIME_PLAY(), "")
        detailPartUrl = const.actionUrl.ANIME_DETAIL() + detailPartUrl
        //去掉参数
        var indexOf = detailPartUrl.indexOf("?")
        detailPartUrl = detailPartUrl.substring(0,indexOf)
        return detailPartUrl
    }

    /**
     * 初始化首页地址
     */
    fun initApi(url: String) {
        //验证网址是否可用
        var title = runCatching{
            JsoupUtil.getDocumentSynchronously(url).title()
        }.getOrDefault("")

        if("很牛影视" == title){
            println("当前网址可用")
            CustomParseHtmlUtil.mainURL = url
        }else {
            println("从新获取最新地址")
            "请稍后正在获取最新地址".showToast()
            //通过api获取最新地址，并初始化
            var homeUrl = CustomParseHtmlUtil.getHomeUrl()
            CustomParseHtmlUtil.mainURL = homeUrl
        }
    }

}
