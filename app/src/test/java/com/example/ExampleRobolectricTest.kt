package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FamilyRole
import com.example.viewmodel.FamRutaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FamRuta", appName)
    }

    @Test
    fun `verify family circle initialization`() {
        val viewModel = FamRutaViewModel()
        val members = viewModel.uiState.value.familyMembers
        assertTrue(members.size >= 4)

        val wife = members.firstOrNull { it.role == FamilyRole.ESPOSA }
        assertNotNull(wife)
        assertEquals("María", wife?.name)

        val son = members.firstOrNull { it.role == FamilyRole.HIJO }
        assertNotNull(son)
        assertEquals("Lucas", son?.name)

        val daughter = members.firstOrNull { it.role == FamilyRole.HIJA }
        assertNotNull(daughter)
        assertEquals("Sofía", daughter?.name)
    }

    @Test
    fun `verify traffic alerts and cameras loaded`() {
        val viewModel = FamRutaViewModel()
        val alerts = viewModel.uiState.value.trafficAlerts
        assertTrue(alerts.isNotEmpty())
    }

    @Test
    fun `verify family reunion and delay animation triggers`() {
        val viewModel = FamRutaViewModel()
        viewModel.triggerFamilyReunionSimulation()
        val state = viewModel.uiState.value
        assertNotNull(state.reunionEvent)
        assertEquals("María (Esposa)", state.reunionEvent?.member1Name)

        viewModel.toggleRouteDelaySimulation()
        assertTrue(viewModel.uiState.value.isRouteDelayed)

        viewModel.dismissReunionEvent()
        assertEquals(null, viewModel.uiState.value.reunionEvent)
    }
}
