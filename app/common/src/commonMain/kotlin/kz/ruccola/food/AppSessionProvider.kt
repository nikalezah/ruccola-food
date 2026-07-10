package kz.ruccola.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun rememberAppSession(): Pair<SessionViewModelStoreOwner, () -> Unit> {
    var sessionOwner by remember { mutableStateOf(SessionViewModelStoreOwner()) }
    val resetSession: () -> Unit = {
        sessionOwner.clear()
        sessionOwner = SessionViewModelStoreOwner()
    }
    return sessionOwner to resetSession
}

@Composable
fun AppSessionProvider(sessionOwner: SessionViewModelStoreOwner, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelStoreOwner provides sessionOwner) { content() }
}
