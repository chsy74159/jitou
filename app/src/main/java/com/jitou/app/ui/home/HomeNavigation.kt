package com.jitou.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class JitouScreen {
    Home,
    Appointment,
    Profile,
}

@Composable
internal fun JitouBottomNav(
    selected: JitouScreen,
    onSelect: (JitouScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .shadow(18.dp, RoundedCornerShape(34.dp), ambientColor = Color(0x18000000), spotColor = Color(0x18000000))
            .background(Color(0xF7FFFFFF), RoundedCornerShape(34.dp))
            .border(width = 1.dp, color = Color(0x11000000), shape = RoundedCornerShape(34.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(
            label = "首页",
            icon = Icons.Rounded.Home,
            selected = selected == JitouScreen.Home,
            onClick = { onSelect(JitouScreen.Home) },
            modifier = Modifier.weight(1f),
        )
        BottomNavItem(
            label = "约头",
            icon = Icons.Rounded.CalendarMonth,
            selected = selected == JitouScreen.Appointment,
            onClick = { onSelect(JitouScreen.Appointment) },
            modifier = Modifier.weight(1f),
        )
        BottomNavItem(
            label = "我的",
            icon = Icons.Rounded.Person,
            selected = selected == JitouScreen.Profile,
            onClick = { onSelect(JitouScreen.Profile) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) Color(0xFFE9ECEF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(23.dp),
            tint = if (selected) Color(0xFF6F72FF) else Ink,
        )
        Text(
            text = label,
            color = if (selected) Color(0xFF6F72FF) else Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}
