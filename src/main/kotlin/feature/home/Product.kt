package feature.home

import kotlinx.serialization.Serializable
import utils.formatDays

@Serializable
data class Product(
    val name: String? = null,
    val description: String? = null,
    val pictureUrl: String? = null,
    val barcode: String? = null,
    val qr: String? = null,
    val insertDate: Long,
    val expireDate: Long
) {
    val nameFormatted = listOfNotNull(name, description).joinToString().ifEmpty { barcode }

    override fun toString() = "<a href='https://jackl.dev/home/$qr'>${expireDate.formatDays}</a> $nameFormatted"
}