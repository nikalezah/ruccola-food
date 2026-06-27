package kz.ruccola.food

import androidx.compose.runtime.Composable
import kz.ruccola.food.feature.dish.DishImagesViewModel

@Composable
expect fun provideImagePicker(): (DishImagesViewModel) -> Unit
