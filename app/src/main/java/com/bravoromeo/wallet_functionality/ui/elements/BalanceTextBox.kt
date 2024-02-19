package com.bravoromeo.wallet_functionality.ui.elements

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme
import com.bravoromeo.wallet_functionality.viewmodel.AppViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewBalanceTextBox(){
    Wallet_functionalityTheme {
        BalanceTextBox(){}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceTextBox(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel? = null,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        OutlinedTextField(
            value = viewModel?.appState?.cardBalance.toString(),
            onValueChange = onValueChange,//{ viewModel?.onCardBalanceChange(it.toLong()) },
            shape = MaterialTheme.shapes.small,
            visualTransformation = EuroVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            prefix = { Text(text = "Balance: ") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = modifier
                .fillMaxWidth()

        )
    }
}

class EuroVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Use NumberFormat to format the input text as Euro currency
        val numberFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
            maximumFractionDigits = 2
            currency = Currency.getInstance("EUR")
        }

        val formattedText = try {
            // Assuming text is a plain number without decimal points
            val number = text.toString().toDouble() / 100 // Convert to proper decimal for currency
            numberFormat.format(number)
        } catch (e: NumberFormatException) {
            "€0,00" // Default or fallback value in case of format exception
        }

        // Offset mapping for cursor position adjustments
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formattedText.length
            override fun transformedToOriginal(offset: Int): Int = text.length
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}


