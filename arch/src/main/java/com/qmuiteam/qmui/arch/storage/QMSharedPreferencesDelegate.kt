package com.qmuiteam.qmui.arch.storage

import kotlin.reflect.KProperty

/**
 */
class QMSharedPreferencesDelegate<T>(val sp: QMSharedPreferences, val key: String, val default: T) {
    companion object {
        private const val TAG = "HSharedPreferencesDelegate"
    }

    /**
     * 目前泛类型支持Int、Long、Float、Boolean、String、Set<String>
     */
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return try {
            val value = sp.getValue(key, default)
            value
        } catch (e: Exception) {
            default
        }
    }

    /**
     * 目前泛类型支持Int、Long、Float、Boolean、String、Set<String>
     */
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        try {
            sp.putValue(key, value)
        } catch (e: Exception) {

        }
    }
}
