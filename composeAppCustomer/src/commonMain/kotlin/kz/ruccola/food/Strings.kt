package kz.ruccola.food

import androidx.compose.runtime.compositionLocalOf

interface Strings {
    val locale: String
    val tabSchedule: String
    val tabProfile: String
    val tabChat: String
    val screenProfileTitle: String
    val screenChatListTitle: String
    val labelEmail: String
    val labelName: String
    val labelAddress: String
    val loadingPlan: String
    val noPlanSelected: String
    val choosePlan: String
    val chosenPlanTitle: String
    val labelCalories: String
    val labelTotalPrice: String
    val labelStartDate: String
    val labelEndDate: String
    val editPersonalInfoTitle: String
    val firstName: String
    val lastName: String
    val address: String
    val cancel: String
    val save: String
    val saving: String
    val saveFailed: String
    val logOut: String
    val login: String
    val register: String
    val email: String
    val password: String
    val confirmPassword: String
    val loggingIn: String
    val registering: String
    val loginFailed: String
    val registerFailed: String
    val goToRegister: String
    val backToLogin: String
    val errorPrefix: String
    val noVariants: String
    val withVariants: String
    val periodDays: String
    val noPlansAvailable: String
    val labelPricePerDay: String
    val formatKcal: String
    val loading: String
    val noDishes: String
    val mealBreakfast: String
    val mealBrunch: String
    val mealLunch: String
    val mealAfternoonSnack: String
    val mealDinner: String
    val chatPlaceholder: String
    val chatEmpty: String
    val chatSupportTitle: String
    val daysQuantityOne: String
    val daysQuantityFew: String
    val daysQuantityMany: String
    val languageSectionTitle: String
    val themeSectionTitle: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val screenDishDetailsTitle: String
}

object RuStrings : Strings {
    override val locale = "ru-RU"
    override val tabSchedule = "Расписание"
    override val tabProfile = "Профиль"
    override val tabChat = "Сообщения"
    override val screenProfileTitle = "Профиль"
    override val screenChatListTitle = "Сообщения"
    override val labelEmail = "Email: %s"
    override val labelName = "Имя: %s %s"
    override val labelAddress = "Адрес: %s"
    override val loadingPlan = "Загрузка плана..."
    override val noPlanSelected = "План не выбран"
    override val choosePlan = "Выберите план"
    override val chosenPlanTitle = "Выбранный план"
    override val labelCalories = "Калории: %s"
    override val labelTotalPrice = "Общая цена: %s"
    override val labelStartDate = "Дата начала: %s"
    override val labelEndDate = "Дата окончания: %s"
    override val editPersonalInfoTitle = "Редактировать личную информацию"
    override val firstName = "Имя"
    override val lastName = "Фамилия"
    override val address = "Адрес"
    override val cancel = "Отмена"
    override val save = "Сохранить"
    override val saving = "Сохранение..."
    override val saveFailed = "Не удалось сохранить"
    override val logOut = "Выйти"
    override val login = "Войти"
    override val register = "Регистрация"
    override val email = "Email"
    override val password = "Пароль"
    override val confirmPassword = "Подтвердите пароль"
    override val loggingIn = "Вход..."
    override val registering = "Регистрация..."
    override val loginFailed = "Ошибка входа"
    override val registerFailed = "Ошибка регистрации"
    override val goToRegister = "Зарегистрироваться"
    override val backToLogin = "Назад к входу"
    override val errorPrefix = "Ошибка: %s"
    override val noVariants = "Без вариантов"
    override val withVariants = "С вариантами"
    override val periodDays = "Период дней"
    override val noPlansAvailable = "Нет доступных планов для выбранной опции"
    override val labelPricePerDay = "Цена за день: %s"
    override val formatKcal = "%s ккал"
    override val loading = "Загрузка..."
    override val noDishes = "Нет блюд"
    override val mealBreakfast = "Завтрак"
    override val mealBrunch = "Второй завтрак"
    override val mealLunch = "Обед"
    override val mealAfternoonSnack = "Полдник"
    override val mealDinner = "Ужин"
    override val chatPlaceholder = "Введите сообщение"
    override val chatEmpty = "Сообщений нет"
    override val chatSupportTitle = "Служба поддержки"
    override val daysQuantityOne = "%d день"
    override val daysQuantityFew = "%d дня"
    override val daysQuantityMany = "%d дней"
    override val languageSectionTitle = "Язык"
    override val themeSectionTitle = "Тема"
    override val themeSystem = "Системная"
    override val themeLight = "Светлая"
    override val themeDark = "Темная"
    override val screenDishDetailsTitle = "Детали блюда"
}

object EnStrings : Strings {
    override val locale = "en-US"
    override val tabSchedule = "Schedule"
    override val tabProfile = "Profile"
    override val tabChat = "Messages"
    override val screenProfileTitle = "Profile"
    override val screenChatListTitle = "Messages"
    override val labelEmail = "Email: %s"
    override val labelName = "Name: %s %s"
    override val labelAddress = "Address: %s"
    override val loadingPlan = "Loading plan..."
    override val noPlanSelected = "No plan selected"
    override val choosePlan = "Choose plan"
    override val chosenPlanTitle = "Chosen plan"
    override val labelCalories = "Calories: %s"
    override val labelTotalPrice = "Total price: %s"
    override val labelStartDate = "Start date: %s"
    override val labelEndDate = "End date: %s"
    override val editPersonalInfoTitle = "Edit personal information"
    override val firstName = "First name"
    override val lastName = "Last name"
    override val address = "Address"
    override val cancel = "Cancel"
    override val save = "Save"
    override val saving = "Saving..."
    override val saveFailed = "Save failed"
    override val logOut = "Log out"
    override val login = "Login"
    override val register = "Register"
    override val email = "Email"
    override val password = "Password"
    override val confirmPassword = "Confirm password"
    override val loggingIn = "Logging in..."
    override val registering = "Registering..."
    override val loginFailed = "Login failed"
    override val registerFailed = "Registration failed"
    override val goToRegister = "Go to register"
    override val backToLogin = "Back to login"
    override val errorPrefix = "Error: %s"
    override val noVariants = "No variants"
    override val withVariants = "With variants"
    override val periodDays = "Period days"
    override val noPlansAvailable = "No plans available"
    override val labelPricePerDay = "Price per day: %s"
    override val formatKcal = "%s kcal"
    override val loading = "Loading..."
    override val noDishes = "No dishes"
    override val mealBreakfast = "Breakfast"
    override val mealBrunch = "Brunch"
    override val mealLunch = "Lunch"
    override val mealAfternoonSnack = "Afternoon snack"
    override val mealDinner = "Dinner"
    override val chatPlaceholder = "Enter message"
    override val chatEmpty = "No messages yet"
    override val chatSupportTitle = "Support"
    override val daysQuantityOne = "%d day"
    override val daysQuantityFew = "%d days"
    override val daysQuantityMany = "%d days"
    override val languageSectionTitle = "Language"
    override val themeSectionTitle = "Theme"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val screenDishDetailsTitle = "Dish details"
}

object KkStrings : Strings {
    override val locale = "kk-KZ"
    override val tabSchedule = "Кесте"
    override val tabProfile = "Профиль"
    override val tabChat = "Чат"
    override val screenProfileTitle = "Профиль"
    override val screenChatListTitle = "Хабарламалар"
    override val labelEmail = "Email: %s"
    override val labelName = "Аты-жөні: %s %s"
    override val labelAddress = "Мекен-жайы: %s"
    override val loadingPlan = "Жоспар жүктелуде..."
    override val noPlanSelected = "Жоспар таңдалмаған"
    override val choosePlan = "Жоспарды таңдаңыз"
    override val chosenPlanTitle = "Таңдалған жоспар"
    override val labelCalories = "Калориялар: %s"
    override val labelTotalPrice = "Жалпы бағасы: %s"
    override val labelStartDate = "Басталу күні: %s"
    override val labelEndDate = "Аяқталу күні: %s"
    override val editPersonalInfoTitle = "Жеке ақпаратты өзгерту"
    override val firstName = "Аты"
    override val lastName = "Тегі"
    override val address = "Мекен-жайы"
    override val cancel = "Бас тарту"
    override val save = "Сақтау"
    override val saving = "Сақталуда..."
    override val saveFailed = "Сақтау мүмкін болмады"
    override val logOut = "Шығу"
    override val login = "Кіру"
    override val register = "Тіркелу"
    override val email = "Email"
    override val password = "Құпия сөз"
    override val confirmPassword = "Құпия сөзді растаңыз"
    override val loggingIn = "Кіру..."
    override val registering = "Тіркелу..."
    override val loginFailed = "Кіру қатесі"
    override val registerFailed = "Тіркелу қатесі"
    override val goToRegister = "Тіркелу"
    override val backToLogin = "Кіруге қайту"
    override val errorPrefix = "Қате: %s"
    override val noVariants = "Нұсқасыз"
    override val withVariants = "Нұсқалармен"
    override val periodDays = "Күндер кезеңі"
    override val noPlansAvailable = "Таңдалған нұсқа үшін қолжетімді жоспарлар жоқ"
    override val labelPricePerDay = "Күніне бағасы: %s"
    override val formatKcal = "%s ккал"
    override val loading = "Жүктелуде..."
    override val noDishes = "Тағамдар жоқ"
    override val mealBreakfast = "Таңғы ас"
    override val mealBrunch = "Екінші таңғы ас"
    override val mealLunch = "Түскі ас"
    override val mealAfternoonSnack = "Бесін ас"
    override val mealDinner = "Кешкі ас"
    override val chatPlaceholder = "Хабарламаңызды енгізіңіз"
    override val chatEmpty = "Хабарламалар жоқ"
    override val chatSupportTitle = "Қолдау"
    override val daysQuantityOne = "%d күн"
    override val daysQuantityFew = "%d күн"
    override val daysQuantityMany = "%d күн"
    override val languageSectionTitle = "Тіл"
    override val themeSectionTitle = "Тақырып"
    override val themeSystem = "Жүйе"
    override val themeLight = "Жарық"
    override val themeDark = "Қараңғы"
    override val screenDishDetailsTitle = "Тағам мәліметтері"
}

val LocalStrings = compositionLocalOf<Strings> { RuStrings }
