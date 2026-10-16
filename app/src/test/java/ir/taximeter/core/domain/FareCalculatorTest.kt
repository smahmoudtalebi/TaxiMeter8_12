package ir.taximeter.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FareCalculatorTest {
    @Test fun belowMinimumUsesMinimumAndRoundsToThousand() {
        assertEquals(
            FareResult.Calculated(20_000),
            FareCalculator.calculateSharedFare(1_000, 5_000, 20_000, true, false)
        )
    }

    @Test fun distanceFareAboveMinimumIsUsedAndRoundedToThousand() {
        assertEquals(
            FareResult.Calculated(30_000),
            FareCalculator.calculateSharedFare(6_000, 5_000, 20_000, true, false)
        )
    }

    @Test fun amountRoundsDownToThousand() {
        assertEquals(24_000L, FareCalculator.roundDownToThousand(24_999L))
        assertEquals(24_000L, FareCalculator.roundDownToThousand(24_500L))
        assertEquals(25_000L, FareCalculator.roundDownToThousand(25_000L))
    }

    @Test fun minimumFareIsPreservedByRoundingMinimumUp() {
        assertEquals(16_000L, FareCalculator.calculateSharedFare(1_000, 5_000, 15_500, true, false).let {
            (it as FareResult.Calculated).amountToman
        })
    }

    @Test fun unresolvedGpsGapStillReturnsEstimateFromRecordedDistance() {
        assertEquals(
            FareResult.NeedsReview(20_000),
            FareCalculator.calculateSharedFare(4_000, 5_000, 20_000, false, true)
        )
    }

    @Test fun gapEstimateUsesRecordedGpsDistanceAboveMinimum() {
        assertEquals(
            FareResult.NeedsReview(30_000),
            FareCalculator.calculateSharedFare(6_100, 5_000, 20_000, false, true)
        )
    }

    @Test fun verifiedFlagAloneDoesNotOverrideGapButKeepsEstimate() {
        assertEquals(
            FareResult.NeedsReview(20_000),
            FareCalculator.calculateSharedFare(4_000, 5_000, 20_000, true, true)
        )
    }
}
