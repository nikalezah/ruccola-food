package kz.ruccola.food.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kz.ruccola.food.api.MessageDto
import kz.ruccola.food.formatDateTime

@Composable
fun ChatUi(
    messages: List<MessageDto>,
    currentUserId: Int?,
    messageBody: String,
    onMessageBodyChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    placeholder: String,
    emptyText: String,
    errorText: String?,
    isLoading: Boolean,
    inputEnabled: Boolean,
    sendEnabled: Boolean,
    locale: String,
    modifier: Modifier = Modifier,
    maxBodyLength: Int = 1000,
) {
    val maxInputLines = 6
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Logic for dynamic shape based on input height (from your web version)
    val singleLineHeightPx = with(density) { TextFieldDefaults.MinHeight.roundToPx() }
    var inputHeightPx by remember { mutableStateOf(singleLineHeightPx) }
    val isMultiLine = inputHeightPx > singleLineHeightPx + 1
    val inputShape = if (isMultiLine) RoundedCornerShape(28.dp) else RoundedCornerShape(50)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp),
                )
                .padding(12.dp),
        ) {
            when {
                isLoading && messages.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                errorText != null -> {
                    Text(
                        text = errorText,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                messages.isEmpty() -> {
                    Text(
                        text = emptyText,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(messages) { message ->
                            val isMine = currentUserId == message.senderUserId
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMine) {
                                    Arrangement.End
                                } else {
                                    Arrangement.Start
                                },
                            ) {
                                Column(
                                    horizontalAlignment = if (isMine) {
                                        Alignment.End
                                    } else {
                                        Alignment.Start
                                    },
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMine) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            },
                                        ),
                                    ) {
                                        Text(
                                            text = message.body,
                                            modifier = Modifier.padding(10.dp),
                                        )
                                    }
                                    Text(
                                        text = formatDateTime(message.createdAt, locale),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom, // Use Bottom for multi-line alignment
        ) {
            OutlinedTextField(
                value = messageBody,
                onValueChange = { value ->
                    if (value.length <= maxBodyLength) {
                        onMessageBodyChange(value)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { inputHeightPx = it.height },
                shape = inputShape,
                placeholder = { Text(placeholder) },
                enabled = inputEnabled,
                minLines = 1,
                maxLines = maxInputLines,
            )
            FilledIconButton(
                onClick = onSendMessage,
                enabled = sendEnabled && messageBody.isNotBlank(),
                modifier = Modifier.size(TextFieldDefaults.MinHeight),
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
            }
        }
    }
}
