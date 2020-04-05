package feature.supermarket

import kotlinx.serialization.Serializable

@Serializable
data class SlotResponse(
//    val messages: JsonArray,
//    val bestRuleCarnetPromo: Boolean,
//    val slotReservationMessage: Any? = null,
//    val slotReservation: Any? = null,
//    val numberOfDays: Long,
//    val startDate: String,
    val slots: List<Slot>
//    val forChangeSlot: Boolean,
//    val currentSlot: Any? = null
)

@Serializable
data class Slot(
//    val originalPrice: Double,
//    val price: Double,
//    val storeManagerID: Long,
//    val statusDescription: Any? = null,
//    val reactivable: Boolean,
//    val cutoffInsert: Long,
    val viewStatus: String,
    val uniqueTimeId: String,
//    val priceDiscount: Long,
//    val supermercatoOrdersPublicIDS: JsonArray,
//    val deliveryDiscountRuleID: Any? = null,
//    val active: Boolean,
    val startTime: String,
    val endTime: String,
    val status: String
) {
    override fun toString(): String {
        return "[$uniqueTimeId] $status $viewStatus dalle $startTime alle $endTime"
    }
}