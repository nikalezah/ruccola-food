package kz.ruccola.food.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappadmin.generated.resources.Res
import food.composeappadmin.generated.resources.email
import food.composeappadmin.generated.resources.logging_in
import food.composeappadmin.generated.resources.login
import food.composeappadmin.generated.resources.login_failed
import food.composeappadmin.generated.resources.password
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.ui.auth.LoginForm
import kz.ruccola.food.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory(roleFilter = { it.isAdmin })),
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginFailedMsg = stringResource(Res.string.login_failed)

    LoginForm(
        state = uiState,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onSubmit = { viewModel.login(onLoggedIn, loginFailedMsg) },
        title = stringResource(Res.string.login),
        emailLabel = stringResource(Res.string.email),
        passwordLabel = stringResource(Res.string.password),
        loginLabel = stringResource(Res.string.login),
        loggingInLabel = stringResource(Res.string.logging_in),
    )
}
