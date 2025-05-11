package com.ferguson.trythree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ferguson.trythree.`class`.BookRow
import com.ferguson.trythree.ui.theme.TrythreeTheme
import com.ferguson.trythree.viewmodels.BookTableViewModel
import com.ferguson.trythree.viewmodels.CartTableViewModel
import com.ferguson.trythree.viewmodels.PurchasesTableViewModel

//import androidx.lifecycle.viewmodel.compose.viewModel

const val stubUserId = 2 //STUB!!!!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val bookViewModel: BookTableViewModel = BookTableViewModel()
            val purchasesViewModel: PurchasesTableViewModel = PurchasesTableViewModel()
            val cartViewModel: CartTableViewModel = CartTableViewModel()

            TrythreeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        BookTableScreen(bookViewModel)
                        PurchasesTableScreen(purchasesViewModel)
                        CartTableScreen(cartViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BookTableScreen(viewModel: BookTableViewModel) {
    LaunchedEffect(Unit) {
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
            BookRowView(
                book = book,
                onCheckChange = { viewModel.updateChecked(book.isbn, it) },
                onQuantityChange = { viewModel.updateQuantity(book.isbn, it) }
            )
        }

        Button(
            onClick = {
                val cartItems = viewModel.getSelectedBooks()
                println("Selected items: $cartItems")
                viewModel.addBookstoCart(stubUserId)
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

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
fun PurchasesTableScreen(viewModel: PurchasesTableViewModel = PurchasesTableViewModel()) {
    val tableData by remember { derivedStateOf { viewModel.tableData } }

    LaunchedEffect(Unit) {
        viewModel.loadPurchasesTable(stubUserId)
    }

    if (tableData.isEmpty()) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            for ((index, row) in tableData.withIndex()) {
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
                            style = if (index == 0)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartTableScreen(viewModel: CartTableViewModel = CartTableViewModel()) {
    val tableData by remember { derivedStateOf { viewModel.tableData } }

    LaunchedEffect(Unit) {
        viewModel.loadCartTable(stubUserId)
    }

    if (tableData.isEmpty() && viewModel.callMade==false) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else if (tableData.isEmpty() && viewModel.callMade==true){
        Text("No items in cart", modifier = Modifier.padding(16.dp))
        println("stub")
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            for ((index, row) in tableData.withIndex()) {
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
                            style = if (index == 0)
                                MaterialTheme.typography.titleMedium
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        Button(
            onClick = {
                viewModel.purchaseCart(stubUserId)
                viewModel.clearCart()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Checkout Cart")
        }
    }
}
