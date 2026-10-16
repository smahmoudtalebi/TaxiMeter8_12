package ir.taximeter.core.domain

/** Fare values are stored as whole Iranian toman and rounded down to 1,000-toman units. */
object FareCalculator {
    /**
     * Calculates the distance fare from recorded distance, applies the configured minimum, and
     * rounds down to 1,000 toman. The minimum itself is rounded UP to a 1,000-toman unit so the
     * payable fare never falls below the configured minimum.
     */
    fun calculateSharedFare(
        distanceMeters: Long,
        ratePerKmToman: Long,
        minimumFareToman: Long,
        distanceVerified: Boolean,
        hasUnresolvedGap: Boolean
    ): FareResult {
        require(distanceMeters >= 0) { "Distance cannot be negative" }
        require(ratePerKmToman >= 0) { "Rate cannot be negative" }
        require(minimumFareToman >= 0) { "Minimum fare cannot be negative" }

        val product = Math.multiplyExact(distanceMeters, ratePerKmToman)
        val distanceFare = Math.addExact(product, 500L) / 1000L
        val beforeRounding = maxOf(minimumFareToman, distanceFare)
        val estimate = roundDownKeepingMinimum(beforeRounding, minimumFareToman)

        return if (!distanceVerified || hasUnresolvedGap) {
            FareResult.NeedsReview(estimate)
        } else {
            FareResult.Calculated(estimate)
        }
    }

    /** Floors a non-negative amount to the previous 1,000-toman boundary. */
    fun roundDownToThousand(amountToman: Long): Long {
        require(amountToman >= 0L) { "Fare cannot be negative" }
        return (amountToman / 1_000L) * 1_000L
    }

    /** Rounds the configured minimum up so the final fare cannot be below it. */
    fun roundUpToThousand(amountToman: Long): Long {
        require(amountToman >= 0L) { "Fare cannot be negative" }
        val thousands = amountToman / 1_000L
        return if (amountToman % 1_000L == 0L) amountToman else Math.multiplyExact(Math.addExact(thousands, 1L), 1_000L)
    }

    /** Floors a fare, but never returns less than the configured minimum fare. */
    fun roundDownKeepingMinimum(amountToman: Long, minimumFareToman: Long): Long {
        require(amountToman >= 0L) { "Fare cannot be negative" }
        require(minimumFareToman >= 0L) { "Minimum fare cannot be negative" }
        return maxOf(roundDownToThousand(amountToman), roundUpToThousand(minimumFareToman))
    }
}

sealed interface FareResult {
    data class Calculated(val amountToman: Long) : FareResult
    data class NeedsReview(val estimatedAmountToman: Long) : FareResult
}
