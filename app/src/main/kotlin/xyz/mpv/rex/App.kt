package xyz.mpv.rex

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import xyz.mpv.rex.database.repository.VideoMetadataCacheRepository
import xyz.mpv.rex.database.repository.HybridMediaIndexRepository
import xyz.mpv.rex.di.DatabaseModule
import xyz.mpv.rex.di.FileManagerModule
import xyz.mpv.rex.di.PreferencesModule
import xyz.mpv.rex.presentation.crash.CrashActivity
import xyz.mpv.rex.presentation.crash.GlobalExceptionHandler
import xyz.mpv.rex.utils.media.MediaLibraryEvents
import `is`.xyz.mpv.FastThumbnails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.merge
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class, kotlinx.coroutines.FlowPreview::class)
class App : Application() {
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val mediaStoreInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val rootInvalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(xyz.mpv.rex.utils.locale.LocaleHelper.wrapContext(base))
  }

  override fun onCreate() {
    super.onCreate()

    // Initialize Koin
    startKoin {
      androidContext(this@App)
      modules(
        PreferencesModule,
        DatabaseModule,
        FileManagerModule,
        xyz.mpv.rex.di.domainModule,
      )
    }

    Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(applicationContext, CrashActivity::class.java))

    // Asynchronously initialize heavy native JNI and IPC operations off the main startup thread
    applicationScope.launch(Dispatchers.IO) {
      runCatching { FastThumbnails.initialize(this@App) }
      runCatching {
        val advancedPrefs = org.koin.core.context.GlobalContext.get().get<xyz.mpv.rex.preferences.AdvancedPreferences>()
        advancedPrefs.syncMediaInfoActivityStatus(this@App)
      }
      runCatching { registerHybridIndexObservers() }
    }

    applicationScope.launch(Dispatchers.IO) {
      kotlinx.coroutines.delay(4_000)
      runCatching {
        val hybridIndex = org.koin.core.context.GlobalContext.get().get<HybridMediaIndexRepository>()
        hybridIndex.ensureFresh()
      }
    }
    applicationScope.launch(Dispatchers.IO) {
      mediaStoreInvalidations
        .debounce(1_500)
        .collect {
          runCatching {
            val hybridIndex = org.koin.core.context.GlobalContext.get().get<HybridMediaIndexRepository>()
            hybridIndex.refreshMediaStore()
          }
        }
    }
    applicationScope.launch(Dispatchers.IO) {
      rootInvalidations
        .debounce(1_500)
        .collect {
          runCatching {
            val hybridIndex = org.koin.core.context.GlobalContext.get().get<HybridMediaIndexRepository>()
            hybridIndex.ensureFresh(force = true)
          }
        }
    }

    // Perform cache maintenance after app startup settles
    applicationScope.launch(Dispatchers.IO) {
      kotlinx.coroutines.delay(8_000)
      runCatching {
        val metadataCache = org.koin.core.context.GlobalContext.get().get<VideoMetadataCacheRepository>()
        metadataCache.performMaintenance()
      }
    }
  }

  private fun registerHybridIndexObservers() {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        mediaStoreInvalidations.tryEmit(Unit)
      }
    }
    contentResolver.registerContentObserver(
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
      true,
      observer,
    )

    val storageReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        rootInvalidations.tryEmit(Unit)
      }
    }
    val storageFilter = IntentFilter().apply {
      addAction(Intent.ACTION_MEDIA_MOUNTED)
      addAction(Intent.ACTION_MEDIA_UNMOUNTED)
      addAction(Intent.ACTION_MEDIA_REMOVED)
      addDataScheme("file")
    }
    ContextCompat.registerReceiver(
      this,
      storageReceiver,
      storageFilter,
      ContextCompat.RECEIVER_EXPORTED,
    )
  }
}
