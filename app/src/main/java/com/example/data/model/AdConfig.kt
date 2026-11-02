package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_config")
data class AdConfig(
    @PrimaryKey
    val id: Int = 1,
    val isBannerEnabled: Boolean = true,
    val isPopUnderEnabled: Boolean = true,
    val isAdMobEnabled: Boolean = true,
    val admobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val admobBannerUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    val admobInterstitialUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    val customBannerTitle: String = "عروض المتجر الحصرية! تسوق الآن بخصم يصل إلى 60%",
    val customBannerSubtitle: String = "انقر هنا لتصفح أحدث العروض والخدمات المميزة",
    val customBannerClickUrl: String = "https://play.google.com",
    val popUnderTitle: String = "إعلان ترويجي ممول: استكشف أفضل ألعاب العام",
    val popUnderClickUrl: String = "https://play.google.com",
    val popUnderTriggerEveryDownloads: Int = 1
)
