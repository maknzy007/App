package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdConfig
import com.example.data.model.AppItem
import com.example.data.model.StoreNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AppItem::class, StoreNotification::class, AdConfig::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "play_store_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.appDao().insertApps(initialApps)
                        database.appDao().saveAdConfig(AdConfig())
                        database.appDao().insertNotification(
                            StoreNotification(
                                title = "مرحباً بك في Play Market! 🎉",
                                message = "تم إطلاق متجر التطبيقات الجديد بنجاح. يمكنك استعراض وتحميل أحدث التطبيقات والألعاب الآن.",
                                type = "NEW_APP",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }

        val initialApps = listOf(
            AppItem(
                id = "app_1",
                title = "واتساب مسنجر",
                packageName = "com.whatsapp",
                category = "تواصل",
                developer = "WhatsApp LLC",
                version = "2.24.12",
                versionCode = 22412,
                sizeMb = 48.5,
                rating = 4.7f,
                reviewsCount = "182M",
                downloadsCount = "5B+",
                description = "تطبيق مراسلة وتواصل فوري بسيط وموثوق وخاص يتيح لك البقاء على اتصال دائم مع أصدقائك وعائلتك عبر الرسائل والمكالمات الصوتية والمرئية عالية الدقة.",
                whatsNew = "• إضافة فلاتر جديدة للمكالمات المرئية.\n• تحسين استقرار المجموعات ومشاركة الملفات كبيرة الحجم.",
                iconEmoji = "💬",
                iconColorHex = 0xFF25D366,
                bannerColorHex = 0xFF128C7E,
                apkUrl = "https://example.com/apks/whatsapp.apk",
                isFeatured = true,
                isTopChart = true
            ),
            AppItem(
                id = "app_2",
                title = "تليجرام الرسمي",
                packageName = "org.telegram.messenger",
                category = "تواصل",
                developer = "Telegram FZ-LLC",
                version = "10.9.1",
                versionCode = 10901,
                sizeMb = 55.2,
                rating = 4.8f,
                reviewsCount = "14M",
                downloadsCount = "1B+",
                description = "مراسلة فورية نقية وبسيطة وسريعة وآمنة ومتزامنة عبر جميع أجهزتك. تخزين سحابي غير محدود وقنوات عامة ومجموعات فائقة.",
                whatsNew = "• قصص القنوات مع خيارات تخصيص متقدمة.\n• وضع توفير الطاقة وإدارة التخزين الذكية.",
                iconEmoji = "✈️",
                iconColorHex = 0xFF0088CC,
                bannerColorHex = 0xFF006699,
                apkUrl = "https://example.com/apks/telegram.apk",
                isFeatured = true,
                isTopChart = true
            ),
            AppItem(
                id = "app_3",
                title = "سباق الصحراء السريع 3D",
                packageName = "com.games.desertracing",
                category = "الألعاب",
                developer = "Apex Velocity Studio",
                version = "1.4.0",
                versionCode = 140,
                sizeMb = 94.0,
                rating = 4.9f,
                reviewsCount = "450K",
                downloadsCount = "10M+",
                description = "انطلق في أشرس سباقات السيارات الصحراوية بدقة رسوميات فائقة ومؤثرات واقعية، قم بترقية محركك ونافس أبطال السرعة حول العالم.",
                whatsNew = "• إضافة 3 مسارات رملية جديدة وسيارات دفع رباعي حديثة.\n• ترقية محرك الرسوميات ودعم 120 إطار في الثانية.",
                iconEmoji = "🏎️",
                iconColorHex = 0xFFFF5722,
                bannerColorHex = 0xFFD84315,
                apkUrl = "https://example.com/apks/desert_racing.apk",
                isFeatured = true,
                isTopChart = true
            ),
            AppItem(
                id = "app_4",
                title = "القرآن الكريم كاملاً بدون نت",
                packageName = "com.islamic.quran.audio",
                category = "أدوات",
                developer = "الهدى للتطبيقات الإسلامية",
                version = "3.2.0",
                versionCode = 320,
                sizeMb = 34.0,
                rating = 5.0f,
                reviewsCount = "1.2M",
                downloadsCount = "50M+",
                description = "المصحف الشريف كاملاً برسم العثماني مع التفسير الميسر، أذكار الصباح والمساء، اتجاه القبلة، ومواقيت الصلاة الدقيقة وتلاوات لأشهر القراء.",
                whatsNew = "• ميزة الاستماع بدون إنترنت.\n• إضافة تفاسير جديدة وخط مصحف المدينة عالي النقاء.",
                iconEmoji = "📖",
                iconColorHex = 0xFF00796B,
                bannerColorHex = 0xFF004D40,
                apkUrl = "https://example.com/apks/quran.apk",
                isFeatured = false,
                isTopChart = true
            ),
            AppItem(
                id = "app_5",
                title = "محرر الصور الاحترافي برو",
                packageName = "com.photo.editor.pro",
                category = "إنتاجية",
                developer = "Creative Lens Labs",
                version = "2.8.5",
                versionCode = 285,
                sizeMb = 42.1,
                rating = 4.6f,
                reviewsCount = "890K",
                downloadsCount = "25M+",
                description = "استوديو متكامل لتحرير الصور وصناعة الملصقات وإزالة الخلفيات بالذكاء الاصطناعي مع أكثر من 500 فلتر ومؤثر بصري احترافي.",
                whatsNew = "• أداة إزالة العناصر الذكية بنقرة واحدة.\n• قوالب تصميم جديدة للشبكات الاجتماعية.",
                iconEmoji = "🎨",
                iconColorHex = 0xFF9C27B0,
                bannerColorHex = 0xFF6A1B9A,
                apkUrl = "https://example.com/apks/photo_editor.apk",
                isFeatured = false,
                isTopChart = false
            ),
            AppItem(
                id = "app_6",
                title = "مدير الملفات السريع والمستكشف",
                packageName = "com.tools.filemanager.fast",
                category = "أدوات",
                developer = "CleanTech Utilities",
                version = "4.1.0",
                versionCode = 410,
                sizeMb = 18.3,
                rating = 4.7f,
                reviewsCount = "320K",
                downloadsCount = "15M+",
                description = "مستكشف ملفات فائق السرعة، تنظيف الذاكرة المؤقتة، نقل الملفات عبر الواي فاي، إدارة التطبيقات وتشفير الملفات الحساسة بخزنة آمنة.",
                whatsNew = "• محلل تخزين ذكي لكشف الملفات المكررة والكبيرة.\n• دعم فك ضغط صيغ RAR و 7Z.",
                iconEmoji = "📁",
                iconColorHex = 0xFF0288D1,
                bannerColorHex = 0xFF01579B,
                apkUrl = "https://example.com/apks/file_manager.apk",
                isFeatured = false,
                isTopChart = false
            ),
            AppItem(
                id = "app_7",
                title = "حرب النجوم: معركة المجرة",
                packageName = "com.games.galaxystarwars",
                category = "الألعاب",
                developer = "Nova Nebula Games",
                version = "1.0.8",
                versionCode = 108,
                sizeMb = 120.0,
                rating = 4.8f,
                reviewsCount = "600K",
                downloadsCount = "5M+",
                description = "لعبة فضاء استراتيجية حماسية، ابنِ أسطولك الفضائي، تحكم في بوارج حربية عملاقة وسيطر على الكواكب والمجرات.",
                whatsNew = "• تحالفات الفضاء الجماعية والحروب بين المجرات.\n• ترقيات أسلحة ليزرية جديدة.",
                iconEmoji = "🚀",
                iconColorHex = 0xFF673AB7,
                bannerColorHex = 0xFF4527A0,
                apkUrl = "https://example.com/apks/galaxy_wars.apk",
                isFeatured = true,
                isTopChart = false
            ),
            AppItem(
                id = "app_8",
                title = "مشغل الفيديو فائق الجودة 4K",
                packageName = "com.media.player.ultra4k",
                category = "ترفيه",
                developer = "CinemaStream Team",
                version = "5.0.2",
                versionCode = 502,
                sizeMb = 26.4,
                rating = 4.7f,
                reviewsCount = "750K",
                downloadsCount = "30M+",
                description = "مشغل وسائط يدعم كافة صيغ الفيديو والصوت مع تشغيل الترجمات التلقائية، المعادل الصوتي المتقدم، وتشغيل الفيديو في نافذة عائمة.",
                whatsNew = "• تسريع العتاد لتشغيل 4K و 8K بسلاسة فائقة.\n• ميزة المؤقت الليلي والتحكم في الإيماءات.",
                iconEmoji = "🎬",
                iconColorHex = 0xFFE91E63,
                bannerColorHex = 0xFFAD1457,
                apkUrl = "https://example.com/apks/video_player.apk",
                isFeatured = false,
                isTopChart = false
            )
        )
    }
}
