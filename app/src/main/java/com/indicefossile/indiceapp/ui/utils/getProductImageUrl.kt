package com.indicefossile.indiceapp.ui.utils

fun getProductImageUrl(
    productBarcode: String,
    imageType: String = "front_fr",
    resolution: String = "400",
    rev: String? = null,
    rawImage: Boolean = false,
    rawImageNumber: String? = null
): String {
    val paddedBarcode = productBarcode.padStart(13, '0')
    val regex = Regex("^(...)(...)(...)(.*)$")
    val matchResult = regex.find(paddedBarcode)
    val folderPath = if (matchResult != null && matchResult.groupValues.size >= 5) {
        "${matchResult.groupValues[1]}/${matchResult.groupValues[2]}/${matchResult.groupValues[3]}/${matchResult.groupValues[4]}"
    } else {
        paddedBarcode
    }

    return if (rawImage) {
        val filename = if (resolution == "full") {
            "$rawImageNumber.jpg"
        } else {
            "$rawImageNumber.$resolution.jpg"
        }
        "https://images.openfoodfacts.org/images/products/$folderPath/$filename"
    } else {
        val filename = if (rev != null) {
            if (resolution == "full") "$imageType.$rev.full.jpg" else "$imageType.$rev.$resolution.jpg"
        } else {
            "$imageType.full.jpg"
        }
        "https://images.openfoodfacts.org/images/products/$folderPath/$filename"
    }
}

