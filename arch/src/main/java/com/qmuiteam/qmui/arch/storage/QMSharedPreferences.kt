package com.qmuiteam.qmui.arch.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.tencent.mmkv.MMKV
import java.util.*

open class QMSharedPreferences(private val fileName: String) : SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val MODE = Context.MODE_PRIVATE

        //需要在attachBaseContext时调用初始化
        fun init(context: Context) { //init the sdk
            MMKV.initialize(context)
        }
    }

    val sp by lazy { MMKV.defaultMMKV(MODE, fileName) }

    val listeners by lazy {
        /*object : SyncWeakListenerRegister<HSharedPreferencesListener>() {
            override fun onListenerCountChanged(size: Int) {
                if (size == 0) {
                    sp.registerOnSharedPreferenceChangeListener(this@HSharedPreferences)
                } else if (size == 1) {
                    sp.unregisterOnSharedPreferenceChangeListener(this@HSharedPreferences)
                }
            }
        }*/
    }

    fun <T> getValue(key: String, default: T): T {
        val dealKey = preDealKey(key)
        return when (default) {
            is Boolean -> sp.getBoolean(dealKey, default) as T
            is Int -> sp.getInt(dealKey, default) as T
            is Long -> sp.getLong(dealKey, default) as T
            is Float -> sp.getFloat(dealKey, default) as T
            is String -> sp.getString(dealKey, default) as T
            is Set<*> -> sp.getStringSet(dealKey, default as MutableSet<String>) as T
            is ByteArray -> sp.getBytes(dealKey, default) as T
            else -> throw RuntimeException("get unknown type of $default with key[$dealKey]")
        }
    }

    @SuppressLint("ApplySharedPref")
    fun <T> putValue(key: String, value: T) {
        val dealKey = preDealKey(key)
        when (value) {
            is Boolean -> sp.edit().putBoolean(dealKey, value)
            is Int -> sp.edit().putInt(dealKey, value)
            is Long -> sp.edit().putLong(dealKey, value)
            is Float -> sp.edit().putFloat(dealKey, value)
            is String -> sp.edit().putString(dealKey, value)
            is Set<*> -> sp.edit().putStringSet(dealKey, value as Set<String>)
            is ByteArray -> sp.putBytes(dealKey, value)
            else -> throw RuntimeException("put unknown type of $value with key[$dealKey]")
        }
    }

    @SuppressLint("ApplySharedPref")
    fun remove(key: String?) {
        val dealKey = preDealKey(key ?: "")
        sp.edit().remove(dealKey)
    }

    @SuppressLint("ApplySharedPref")
    fun clear() {
        sp.edit().clear()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ApplySharedPref")
    fun getAll(): MutableMap<String, *> {
        val userId: Long = userIdProvider?.getUserId() ?: 0L
        return if (userId == 0L) {
            sp.all
        } else {
            val map = mutableMapOf<String, Any?>()
            sp.all.forEach { (t, any) ->
                map[offDealKey(t)] = any
            }
            map
        }
    }

    @SuppressLint("ApplySharedPref")
    fun contains(key: String?) = sp.contains(preDealKey(key ?: ""))

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val offDealKey = offDealKey(key ?: "")
        //listeners.getListeners().forEach { it.onPreferenceChanged(this, offDealKey) }
    }

    fun registerChangeListener(l: HSharedPreferencesListener?) {
        //listeners.registerListener(l)
    }

    fun unregisterChangeListener(l: HSharedPreferencesListener?) {
        //listeners.unregisterListener(l)
    }

    private var userIdProvider: UserIdProvider? = null
    internal fun setUserIdProvider(userIdProvider: UserIdProvider) {
        this.userIdProvider = userIdProvider
    }

    protected fun preDealKey(key: String): String {
        val userId: Long = userIdProvider?.getUserId() ?: 0
        if (userId != 0L) {
            if (key.contains(String.format(Locale.ENGLISH, "&%d", userId))) {
                return key
            }
            return String.format(Locale.ENGLISH, "%s&%d", key, userId)
        }
        return key
    }

    /**
     * 脱去用户相关的修饰
     */
    private fun offDealKey(key: String): String {
        var finalKey = key
        val userId: Long = userIdProvider?.getUserId() ?: 0
        if (userId != 0L) {
            val point = key.lastIndexOf('&')
            if (point > 0) {
                finalKey = key.substring(0, point)
            }
        }
        return finalKey
    }
}

/**
 * 数据与用户绑定时
 */
interface UserIdProvider {
    /**
     * 不存在用户时返回0
     */
    fun getUserId(): Long
}

interface HSharedPreferencesListener {
    fun onPreferenceChanged(sp: MMKV, key: String)
}