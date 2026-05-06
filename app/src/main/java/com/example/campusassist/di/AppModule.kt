package com.example.campusassist.di

import android.content.Context
import androidx.room.Room
import com.example.campusassist.data.local.AppDatabase
import com.example.campusassist.data.local.NetworkMonitor
import com.example.campusassist.data.local.SessionManager
import com.example.campusassist.data.local.dao.DepartmentDao
import com.example.campusassist.data.local.dao.NotificationDao
import com.example.campusassist.data.local.dao.ServiceTicketDao
import com.example.campusassist.data.local.dao.UserDao
import com.example.campusassist.data.repository.DepartmentRepositoryImpl
import com.example.campusassist.data.repository.NotificationRepositoryImpl
import com.example.campusassist.data.repository.TicketRepositoryImpl
import com.example.campusassist.data.repository.UserRepositoryImpl
import com.example.campusassist.domain.repository.DepartmentRepository
import com.example.campusassist.domain.repository.NotificationRepository
import com.example.campusassist.domain.repository.TicketRepository
import com.example.campusassist.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            // .addCallback(AppDatabase.SEED_CALLBACK) <-- REMOVE THIS LINE
            .build()

    @Provides @Singleton
    fun provideTicketDao(db: AppDatabase): ServiceTicketDao = db.serviceTicketDao()

    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideNotifDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides @Singleton
    fun provideDepartmentDao(db: AppDatabase): DepartmentDao = db.departmentDao()

    @Provides @Singleton
    fun provideSessionManager(@ApplicationContext ctx: Context): SessionManager = SessionManager(ctx)

    @Provides @Singleton
    fun provideNetworkMonitor(@ApplicationContext ctx: Context): NetworkMonitor = NetworkMonitor(ctx)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindTicketRepo(impl: TicketRepositoryImpl): TicketRepository
    @Binds @Singleton abstract fun bindUserRepo(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindNotifRepo(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindDepartmentRepo(impl: DepartmentRepositoryImpl): DepartmentRepository  // new
}