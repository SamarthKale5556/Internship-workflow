package com.example.blindassistcompanion.di

import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.blindassistcompanion.data.ai.GeminiGenerativeAiRepositoryImpl
import com.example.blindassistcompanion.data.ble.BleScannerImpl
import com.example.blindassistcompanion.data.local.AppDatabase
import com.example.blindassistcompanion.data.local.FamilyDao
import com.example.blindassistcompanion.data.ml.FaceEmbeddingPipelineImpl
import com.example.blindassistcompanion.data.remote.PiCameraClientImpl
import com.example.blindassistcompanion.data.repository.BleRepositoryImpl
import com.example.blindassistcompanion.data.repository.FamilyRepositoryImpl
import com.example.blindassistcompanion.data.repository.DeviceRepositoryImpl
import com.example.blindassistcompanion.data.repository.HotspotRepositoryImpl
import com.example.blindassistcompanion.data.tts.PiTtsRepositoryImpl
import com.example.blindassistcompanion.domain.repository.BleRepository
import com.example.blindassistcompanion.domain.repository.FamilyRepository
import com.example.blindassistcompanion.domain.repository.DeviceRepository
import com.example.blindassistcompanion.domain.repository.HotspotRepository
import com.example.blindassistcompanion.domain.repository.GenerativeAiRepository
import com.example.blindassistcompanion.domain.repository.TtsRepository
import com.example.blindassistcompanion.domain.repository.PiCameraClient
import com.example.blindassistcompanion.data.ml.FaceEmbeddingPipeline
import com.example.blindassistcompanion.domain.repository.AudioRecorder
import com.example.blindassistcompanion.data.audio.BluetoothAudioRecorderImpl
import com.example.blindassistcompanion.data.sarvam.SarvamApiService
import com.example.blindassistcompanion.data.tts.SarvamTtsRepositoryImpl
import com.example.blindassistcompanion.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE family_members ADD COLUMN phoneNumber TEXT NOT NULL DEFAULT ''")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "blind_assist_db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    @Provides
    @Singleton
    fun provideFamilyDao(database: AppDatabase): FamilyDao {
        return database.familyDao()
    }

    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter? {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        return bluetoothManager?.adapter
    }

    @Provides
    @Singleton
    fun provideSarvamApiService(): SarvamApiService {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("api-subscription-key", BuildConfig.SARVAM_API_KEY)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.sarvam.ai/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(SarvamApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTtsRepository(
        impl: PiTtsRepositoryImpl
    ): TtsRepository

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(
        impl: BluetoothAudioRecorderImpl
    ): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindPiCameraClient(
        impl: PiCameraClientImpl
    ): PiCameraClient

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(
        impl: FamilyRepositoryImpl
    ): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindHotspotRepository(
        impl: HotspotRepositoryImpl
    ): HotspotRepository

    @Binds
    @Singleton
    abstract fun bindBleRepository(
        impl: BleRepositoryImpl
    ): BleRepository

    @Binds
    @Singleton
    abstract fun bindFaceEmbeddingPipeline(
        impl: FaceEmbeddingPipelineImpl
    ): FaceEmbeddingPipeline

    @Binds
    @Singleton
    abstract fun bindGenerativeAiRepository(
        impl: GeminiGenerativeAiRepositoryImpl
    ): GenerativeAiRepository
}
