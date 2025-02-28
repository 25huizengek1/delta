package dev.shadoe.delta.data

import android.net.ITetheringConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.shadoe.hotspotapi.exceptions.BinderAcquisitionException
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

@Module
@InstallIn(SingletonComponent::class)
object TetheringModule {
    @TetheringSystemService
    @Provides
    fun provideTetheringManager(): ITetheringConnector {
        return SystemServiceHelper
            .getSystemService("tethering")
            ?.let { ShizukuBinderWrapper(it) }
            ?.let { ITetheringConnector.Stub.asInterface(it) }
            ?: throw BinderAcquisitionException(
                "Unable to get ITetheringConnector",
            )
    }
}
