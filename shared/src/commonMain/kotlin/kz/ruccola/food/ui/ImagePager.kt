package kz.ruccola.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kz.ruccola.food.dishImageUrl

@Composable
fun ImagePager(
    imageUrls: List<String>,
    onBack: () -> Unit,
) {
    if (imageUrls.isEmpty()) return

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge),
    ) {
        val squareHeight = maxWidth
        val pagerState = rememberPagerState(pageCount = { imageUrls.size })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(squareHeight),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                AsyncImage(
                    model = dishImageUrl(imageUrls[page]),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (imageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(imageUrls.size) { index ->
                        val isSelected = index == pagerState.currentPage
                        val dotColor = Color.White.copy(alpha = if (isSelected) 0.9f else 0.4f)
                        val dotWidth = if (isSelected) 16.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .size(width = dotWidth, height = 8.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = Color.Black.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(50),
                                )
                                .background(
                                    color = dotColor,
                                    shape = RoundedCornerShape(50),
                                ),
                        )
                    }
                }
            }
        }
    }
}
