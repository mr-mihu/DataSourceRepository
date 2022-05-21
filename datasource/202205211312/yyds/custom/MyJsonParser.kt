package com.skyd.imomoe.model.impls.custom

import com.google.gson.*
import java.lang.reflect.Type
import java.util.HashMap

class MyJsonParser<T>(
    private var typeElementName: String,
    private var targetClass: Class<T>
) {
    private val typeClassMap = HashMap<String, Class<out T>>()
    private val typeAdapter: TargetDeserializer = TargetDeserializer()
    private lateinit var gson: Gson

    fun addTypeElementValueWithClassType(
        typeElementValue: String,
        classValue: Class<out T>
    ): MyJsonParser<T> {
        typeClassMap[typeElementValue] = classValue
        return this
    }

    fun <V> fromJson(json: String, jsonFeedsClass: Type): V {
        return gson.fromJson(json, jsonFeedsClass)
    }

    inner class TargetDeserializer : JsonDeserializer<T?> {
        private fun JsonElement.getString(): String {
            return if (isJsonNull) "" else asString
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): T? {
            val jsonObject = json.asJsonObject
            val jsonElement = jsonObject[typeElementName]

            val contentType = jsonElement?.getString()
            // 未注册的类型直接返回null
            if (!typeClassMap.containsKey(contentType)) {
                return null
            }
            return gson.fromJson(json, typeClassMap[contentType])
        }
    }

    fun create(): MyJsonParser<T> {
        gson = GsonBuilder().registerTypeAdapter(targetClass, typeAdapter).create()
        return this
    }
}