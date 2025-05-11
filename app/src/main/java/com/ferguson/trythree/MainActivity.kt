package com.ferguson.trythree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ferguson.trythree.classes.ActiveUser
import com.ferguson.trythree.classes.BookRow
import com.ferguson.trythree.classes.LoginState
import com.ferguson.trythree.ui.theme.TrythreeTheme
import com.ferguson.trythree.viewmodel.BookTableViewModel
import com.ferguson.trythree.viewmodel.UserLoginViewModel
import com.ferguson.trythree.viewmodels.CartTableViewModel
import com.ferguson.trythree.viewmodels.PurchasesTableViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userLoginViewModel = remember { UserLoginViewModel() }
            val loginState by userLoginViewModel.loginResult
            val reloadTrigger = remember { mutableIntStateOf(0) } //hack to force reloads

            TrythreeTheme {
                when (val state = loginState) {
                    is LoginState.Idle, is LoginState.Failure -> {
                        LoginScreen(
                            viewModel = userLoginViewModel,
                            errorMessage = (state as? LoginState.Failure)?.errorMessage
                        )
                    }
                    is LoginState.Success -> {
                        val user = state.user
                        val bookViewModel = remember { BookTableViewModel() }
                        val purchasesViewModel = remember { PurchasesTableViewModel() }
                        val cartViewModel = remember { CartTableViewModel() }

                        Scaffold(
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                //Show User Info
                                UserInfoScreen(user)

                                //Show book table
                                Text("Available Books", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                BookTableScreen(bookViewModel, user.id, reloadTrigger)

                                //Show cart screen
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Your Cart", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                CartTableScreen(cartViewModel, user.id, reloadTrigger)


                                //show past purchases table
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Past Purchases", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                PurchasesTableScreen(purchasesViewModel, user.id, reloadTrigger)

                                //logout
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { userLoginViewModel.logout() }) {
                                    Text("Log Out")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: UserLoginViewModel, errorMessage: String?) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.login(username, password) }) {
            Text("Log In")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun BookTableScreen(viewModel: BookTableViewModel, userId: Int, reloadTrigger: MutableIntState) {
    LaunchedEffect(reloadTrigger.intValue) {
        viewModel.loadBookTable()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Select", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.titleMedium)
            Text("Title", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("Price", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("Qty", modifier = Modifier.width(60.dp), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data rows
        viewModel.bookRows.forEach { book ->
            key(book.isbn + reloadTrigger.intValue) {
            BookRowView(
                book = book,
                onCheckChange = { viewModel.updateChecked(book.isbn, it) },
                onQuantityChange = { viewModel.updateQuantity(book.isbn, it) }
            )
            }
        }

        val scope = rememberCoroutineScope()

        Button(
            onClick = {
                scope.launch {
                    viewModel.addBookstoCart(userId)
                    viewModel.clearChecked()
                    reloadTrigger.intValue += 1
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add To Cart")
        }
    }
}

@Composable
fun BookRowView(book: BookRow, onCheckChange: (Boolean) -> Unit, onQuantityChange: (Int) -> Unit) {
    var localChecked by remember { mutableStateOf(book.isChecked) }
    var localQty by remember { mutableStateOf(book.quantity.toString()) }

    Row( verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = localChecked,
            onCheckedChange = {
                localChecked = it
                onCheckChange(it)

                if (!it) {
                    localQty = "0"
                    onQuantityChange(0)
                } else {
                    localQty = "1"
                    onQuantityChange(1)
                }
            },
            modifier = Modifier.weight(0.5f)
        )

        Text(book.title, modifier = Modifier.weight(1f))

        Text(
            text = "$${book.price}",
            modifier = Modifier.weight(1f)
        )

        TextField(
            value = localQty,
            onValueChange = {
                localQty = it
                it.toIntOrNull()?.let(onQuantityChange)
            },
            enabled = localChecked,
            modifier = Modifier.width(60.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun PurchasesTableScreen(viewModel: PurchasesTableViewModel = PurchasesTableViewModel(), userId: Int, reloadTrigger: MutableIntState) {
    val tableData by remember { derivedStateOf { viewModel.tableData } }

    LaunchedEffect(reloadTrigger.intValue) {
        viewModel.loadPurchasesTable(userId)
    }

    if (tableData.isEmpty()) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(16.dp)) {

            // Hardcoded headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("Times Purchased", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("ISBN", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("Title", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("Price", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            }

            // Data rows
            for (index in 1 until tableData.size) {
                val row = tableData[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (cell in row) {
                        Text(
                            text = cell,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartTableScreen(viewModel: CartTableViewModel = CartTableViewModel(), userId: Int, reloadTrigger: MutableIntState) {
    val tableData by remember { derivedStateOf { viewModel.tableData } }

    LaunchedEffect(reloadTrigger.intValue) {
        viewModel.loadCartTable(userId)
    }

    if (tableData.isEmpty() && viewModel.callMade==false) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else if (tableData.isEmpty() && viewModel.callMade==true){
        Text("No items in cart", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(16.dp)) {

            // Hardcoded headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text("Copies Selected", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("ISBN", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("Title", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text("Price", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            }

            // Data rows
            for (index in 1 until tableData.size) {
                val row = tableData[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (cell in row) {
                        Text(
                            text = cell,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch {
                    viewModel.purchaseCart(userId)
                    viewModel.clearCart()
                    reloadTrigger.intValue += 1
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Checkout Cart")
        }
    }
}

@Composable
fun UserInfoScreen(user: ActiveUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User Information", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Username: ${user.username}", style = MaterialTheme.typography.bodyLarge)
            Text("User Type: ${user.usertype}", style = MaterialTheme.typography.bodyLarge)
            Text("User ID: ${user.id}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
