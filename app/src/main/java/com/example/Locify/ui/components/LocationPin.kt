package com.example.Locify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.Locify.R

@Composable
fun LocationPin(
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Image(
        painter = painterResource(id = R.drawable.ic_location_pin),
        contentDescription = "Location Pin",
        modifier = modifier.size(size.dp)
    )
}