package kz.ruccola.food.feature.auth

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import food.composeappcustomer.generated.resources.Res
import food.composeappcustomer.generated.resources.email
import food.composeappcustomer.generated.resources.go_to_register
import food.composeappcustomer.generated.resources.logging_in
import food.composeappcustomer.generated.resources.login
import food.composeappcustomer.generated.resources.login_failed
import food.composeappcustomer.generated.resources.password
import kz.ruccola.food.api.AuthResponseDto
import kz.ruccola.food.ui.auth.LoginForm
import kz.ruccola.food.viewmodel.LoginViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    onLoggedIn: (resp: AuthResponseDto) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory(roleFilter = { it.isCustomer })),
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
        extraActions = { TextButton(onClick = onGoToRegister) { Text(stringResource(Res.string.go_to_register)) } },
    )
}
