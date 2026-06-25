package kz.ruccola.food

import androidx.compose.runtime.Composable
import kz.ruccola.food.viewmodel.DishImagesViewModel

@Composable
expect fun provideImagePicker(): (DishImagesViewModel) -> Unit
