package xyz.mpv.rex.di

import xyz.mpv.rex.repository.wyzie.WyzieSearchRepository
import xyz.mpv.rex.ui.player.managers.PlaybackManager
import okhttp3.OkHttpClient
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import java.util.concurrent.TimeUnit

import xyz.mpv.rex.ui.browser.miniplayer.MiniPlayerStateManager

val domainModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single { WyzieSearchRepository(androidContext(), get(), get(), get()) }
    single { PlaybackManager(get()) }
    single { MiniPlayerStateManager() }
    single { xyz.mpv.rex.jellyfin.api.JellyfinApi(get(), get(), get()) }
    single { xyz.mpv.rex.jellyfin.remote.JellyfinRemoteClient(get(), get(), get(), androidContext()) }
}


