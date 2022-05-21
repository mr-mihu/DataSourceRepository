package com.skyd.imomoe.model.impls.custom

import android.app.Activity
import com.skyd.imomoe.bean.ClassifyBean
import com.skyd.imomoe.bean.PageNumberBean
import com.skyd.imomoe.model.interfaces.IClassifyModel

class CustomClassifyModel : IClassifyModel {
    override suspend fun getClassifyData(partUrl: String): Pair<ArrayList<Any>, PageNumberBean?> {
        return Pair(ArrayList(), null)
    }

    override fun clearActivity() {
    }

    override suspend fun getClassifyTabData(): ArrayList<ClassifyBean> {
        return ArrayList()
    }

    override fun setActivity(activity: Activity) {
    }
}
