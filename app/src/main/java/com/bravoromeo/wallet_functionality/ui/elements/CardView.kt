package com.bravoromeo.wallet_functionality.ui.elements

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bravoromeo.wallet_functionality.R
import com.bravoromeo.wallet_functionality.ui.theme.Wallet_functionalityTheme

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCardView(){
    Wallet_functionalityTheme {
        CardView()
    }
}

@Composable
fun CardView(
    modifier: Modifier = Modifier,
    cardImageResourceId: Int? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 1.dp,
        modifier = modifier
            .padding(8.dp)
    ) {
        Image(painter = painterResource(id = cardImageResourceId ?: R.drawable.solred_card_900), contentDescription = "")
    }
}
