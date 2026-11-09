package cit.edu.primera.core.util

import cit.edu.primera.feature.dashboard.domain.DashboardBusinessLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar
import java.util.Date

class PregnancyCalculatorTest {

    @Test
    fun calculateEdd_fromLmp_returnsCorrect280DaysFutureDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -70) // 10 weeks ago
        val lmpDate = calendar.time

        val calculatedEdd = PregnancyCalculator.calculateEdd(lmpDate = lmpDate)
        assertNotNull(calculatedEdd)

        val expectedCal = Calendar.getInstance().apply {
            time = lmpDate
            add(Calendar.DAY_OF_YEAR, 280)
        }
        assertEquals(expectedCal.get(Calendar.DAY_OF_YEAR), Calendar.getInstance().apply { time = calculatedEdd!! }.get(Calendar.DAY_OF_YEAR))

        val gestationalWeeks = PregnancyCalculator.getGestationalWeeks(calculatedEdd)
        assertEquals(10, gestationalWeeks)

        val trimester = DashboardBusinessLogic.getTrimester(gestationalWeeks)
        assertEquals("first trimester", trimester)
    }

    @Test
    fun calculateEdd_fromUltrasoundScan_recalculatesGestationalAgeAndTrimester() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -14) // Scan was 2 weeks ago
        val scanDate = calendar.time
        val scanWeeks = 18
        val scanDays = 0

        // At scan date 2 weeks ago, fetus was 18 weeks -> today fetus is 20 weeks
        val calculatedEdd = PregnancyCalculator.calculateEdd(
            scanDate = scanDate,
            scanWeeks = scanWeeks,
            scanDays = scanDays
        )
        assertNotNull(calculatedEdd)

        val currentWeeks = PregnancyCalculator.getGestationalWeeks(calculatedEdd)
        assertEquals(20, currentWeeks)

        val trimester = DashboardBusinessLogic.getTrimester(currentWeeks)
        assertEquals("second trimester", trimester)
    }

    @Test
    fun calculateEdd_priorityOrder_explicitEddOverridesUltrasoundAndLmp() {
        val explicitEddCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30) // 30 days remaining -> week 35/36 (third trimester)
        }
        val explicitEdd = explicitEddCal.time

        val lmpCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -70) // 10 weeks (first trimester)
        }
        val lmpDate = lmpCal.time

        val resolvedEdd = PregnancyCalculator.calculateEdd(
            eddDate = explicitEdd,
            lmpDate = lmpDate
        )

        assertEquals(explicitEdd, resolvedEdd)
    }

    @Test
    fun trimesterBoundaries_verifyFirstSecondThirdTrimesterRanges() {
        // First Trimester: weeks 1..13
        assertEquals("first trimester", DashboardBusinessLogic.getTrimester(1))
        assertEquals("first trimester", DashboardBusinessLogic.getTrimester(13))

        // Second Trimester: weeks 14..27
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(14))
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(20))
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(27))

        // Third Trimester: weeks 28..42
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(28))
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(36))
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(40))
    }

    @Test
    fun profileChangeSimulation_changingLmpToSecondAndThirdTrimester_recalculatesCorrectly() {
        // 1. User sets LMP to 140 days ago (20 weeks) -> 2nd trimester
        val lmp2ndTrimester = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -140)
        }.time

        val edd2nd = PregnancyCalculator.calculateEdd(lmpDate = lmp2ndTrimester)
        val weeks2nd = PregnancyCalculator.getGestationalWeeks(edd2nd)
        assertEquals(20, weeks2nd)
        assertEquals("second trimester", DashboardBusinessLogic.getTrimester(weeks2nd))

        // 2. User updates LMP in Profile to 210 days ago (30 weeks) -> 3rd trimester
        val lmp3rdTrimester = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -210)
        }.time

        val edd3rd = PregnancyCalculator.calculateEdd(lmpDate = lmp3rdTrimester)
        val weeks3rd = PregnancyCalculator.getGestationalWeeks(edd3rd)
        assertEquals(30, weeks3rd)
        assertEquals("third trimester", DashboardBusinessLogic.getTrimester(weeks3rd))
    }
}
