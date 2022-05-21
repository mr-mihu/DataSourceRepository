package com.skyd.imomoe.model.impls.custom

import com.skyd.imomoe.model.interfaces.IUtil

class CustomUtil : IUtil {

    override fun getDetailLinkByEpisodeLink(episodeUrl: String): String {
        var detailPartUrl = episodeUrl.replaceFirst(CustomConst().actionUrl.ANIME_PLAY(), CustomConst().actionUrl.ANIME_DETAIL())
        //去掉参数
        var indexOf = detailPartUrl.indexOf("?")
        if(indexOf!=-1){
            detailPartUrl = detailPartUrl.substring(0, indexOf)
        }
        return detailPartUrl
    }
}
