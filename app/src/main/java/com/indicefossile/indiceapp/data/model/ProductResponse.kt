package com.indicefossile.indiceapp.data.model

data class ProductResponse(
    val code: String,
    val product: Product
)

data class Product(
    val code: String,
    val product_name: String?,
    val brands: String?,
    val quantity: String?,
    val ingredients_text: String?,
    val nutrition_grades: String?,
    val nutriscore_data: NutriscoreData?,
    val nutriments: Map<String, Any>?,
    val ecoscore_data: EcoscoreData?,
    val nova_groups_tags: List<String>?,
    val images: Images?
)

data class NutriscoreData(
    val grade: String?,
    val score: Int?,
    val energy_points: Int?,
    val sugars_value: Double?
)

data class EcoscoreData(
    val grade: String?,
    // Autres champs éventuels de l'ecoscore_data
    val adjustments: Adjustments?,
    val missing: Missing?,
    val missing_data_warning: Int?,
    val score: Int?,
    val scores: Map<String, Int>?,
    val status: String?,
    // Ajoutez ici la propriété previous_data pour récupérer l'agribalyse imbriquée
    val agribalyse: Agribalyse ?
)

data class Agribalyse(
    val agribalyse_food_code: String?,
    val agribalyse_proxy_food_code: String?,
    val co2_agriculture: Double?,
    val co2_consumption: Double?,
    val co2_distribution: Double?,
    val co2_packaging: Double?,
    val co2_processing: Double?,
    val co2_total: Double?,
    val co2_transportation: Double?,
    val code: String?,
    val dqr: String?,
    val ef_agriculture: Double?,
    val ef_consumption: Double?,
    val ef_distribution: Double?,
    val ef_packaging: Double?,
    val ef_processing: Double?,
    val ef_total: Double?,
    val ef_transportation: Double?,
    val is_beverage: Int?,
    val name_en: String?,
    val name_fr: String?,
    val score: Int?,
    val version: String?
)

// Si vous avez besoin de modéliser les ajustements ou les informations manquantes, vous pouvez ajouter :

data class Adjustments(
    // Exemple de champ
    val origins_of_ingredients: OriginsOfIngredients?
)

data class OriginsOfIngredients(
    val aggregated_origins: List<AggregatedOrigin>?,
    val epi_score: Float?,
    val epi_value: Float?,
    val origins_from_categories: List<String>?,
    val origins_from_origins_field: List<String>?,
    val transportation_score: Float?,
    val transportation_scores: Map<String, Float>?,
    val transportation_value: Float?,
    val transportation_values: Map<String, Float>?,
    val value: Float?,
    val values: Map<String, Float>?,
    val warning: String?
)

data class AggregatedOrigin(
    val epi_score: String?, // Parfois sous forme de chaîne, vérifiez le type réel
    val origin: String?,
    val percent: Int?,
    val transportation_score: Int?
)

data class Missing(
    // Exemple de structure si besoin
    val labels: Int?,
    val origins: Int?,
    val packagings: Int?
)

data class Images(
    val front_fr: ImageData?
)

data class ImageData(
    val imgid: String,
    val rev: String?,
    val sizes: Map<String, SizeData>?
)

data class SizeData(
    val w: Int,
    val h: Int
)
