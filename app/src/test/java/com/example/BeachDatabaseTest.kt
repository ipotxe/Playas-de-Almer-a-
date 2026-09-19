package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BeachDatabase
import com.example.data.local.BeachEntity
import com.example.data.local.BeachInfoDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BeachDatabaseTest {

    private lateinit var database: BeachDatabase
    private lateinit var beachInfoDao: BeachInfoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BeachDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        beachInfoDao = database.beachInfoDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveBeachEntity() = runBlocking {
        val beach = BeachEntity(
            id = "playa_los_muertos",
            name = "Playa de los Muertos",
            location = "Carboneras, Cabo de Gata - Níjar",
            municipality = "Carboneras",
            zone = "CABO_DE_GATA",
            latitude = 36.9556,
            longitude = -1.8988,
            isSuitableForSnorkeling = true,
            snorkelRating = 4.8f,
            snorkelDescription = "Formaciones rocosas volcánicas sumergidas y praderas de posidonia oceánica"
        )

        beachInfoDao.insertBeach(beach)

        val retrieved = beachInfoDao.getBeachByIdDirect("playa_los_muertos")
        assertNotNull(retrieved)
        assertEquals("Playa de los Muertos", retrieved?.name)
        assertEquals("Carboneras, Cabo de Gata - Níjar", retrieved?.location)
        assertEquals(36.9556, retrieved?.latitude ?: 0.0, 0.0001)
        assertEquals(-1.8988, retrieved?.longitude ?: 0.0, 0.0001)
        assertTrue(retrieved?.isSuitableForSnorkeling == true)
        assertEquals(4.8f, retrieved?.snorkelRating ?: 0f, 0.01f)
    }

    @Test
    fun retrieveBeachesSuitableForSnorkeling() = runBlocking {
        val snorkelingBeach = BeachEntity(
            id = "cala_enmedio",
            name = "Cala de Enmedio",
            location = "Níjar (Agua Amarga), Cabo de Gata",
            municipality = "Níjar",
            zone = "CABO_DE_GATA",
            latitude = 36.9212,
            longitude = -1.9425,
            isSuitableForSnorkeling = true,
            snorkelRating = 4.9f
        )

        val nonSnorkelingBeach = BeachEntity(
            id = "playa_urbana",
            name = "Playa Urbana Portuaria",
            location = "Almería Capital",
            municipality = "Almería",
            zone = "ALMERIA_CAPITAL",
            latitude = 36.83,
            longitude = -2.46,
            isSuitableForSnorkeling = false,
            snorkelRating = 2.0f
        )

        beachInfoDao.insertBeaches(listOf(snorkelingBeach, nonSnorkelingBeach))

        val suitableList = beachInfoDao.getBeachesSuitableForSnorkeling().first()
        assertEquals(1, suitableList.size)
        assertEquals("Cala de Enmedio", suitableList[0].name)
        assertTrue(suitableList[0].isSuitableForSnorkeling)
    }

    @Test
    fun retrieveBeachesByLocationAndCoordinates() = runBlocking {
        val beach1 = BeachEntity(
            id = "cala_cocedores",
            name = "Cala de los Cocedores",
            location = "Pulpí, San Juan de los Terreros",
            municipality = "Pulpí",
            zone = "LEVANTE_ALMERIENSE",
            latitude = 37.3736,
            longitude = -1.6311,
            isSuitableForSnorkeling = true,
            snorkelRating = 4.7f
        )

        beachInfoDao.insertBeach(beach1)

        val byLocation = beachInfoDao.getBeachesByLocation("Pulpí").first()
        assertEquals(1, byLocation.size)
        assertEquals("Cala de los Cocedores", byLocation[0].name)

        val inBounds = beachInfoDao.getBeachesInBoundingBox(37.0, 37.5, -1.8, -1.5).first()
        assertEquals(1, inBounds.size)
        assertEquals("Cala de los Cocedores", inBounds[0].name)
    }
}
