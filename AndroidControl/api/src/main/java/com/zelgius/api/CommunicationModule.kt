package com.zelgius.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CommunicationModule {
    @Provides
    fun provideInputStream(): IOStreamProvider = SocketIOStreamProvider("192.168.1.145", 5432)
}