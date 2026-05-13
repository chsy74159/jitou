package com.jitou.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.ui.theme.jitouColors

internal enum class JitouScreen {
    Home,
    Appointment,
    Profile,
    ;

    val pageIndex: Int
        get() = primaryScreens().indexOf(this)

    fun systemBackTarget(): JitouScreen? = when (this) {
        Home -> null
        Appointment,
        Profile -> Home
    }

    companion object {
        fun primaryScreens(): List<JitouScreen> = listOf(Home, Appointment, Profile)

        fun fromPageIndex(index: Int): JitouScreen = primaryScreens()[index.coerceIn(0, primaryScreens().lastIndex)]
    }
}

@Composable
internal fun JitouBottomNav(
    selected: JitouScreen,
    pagePosition: Float = selected.pageIndex.toFloat(),
    onSelect: (JitouScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screens = JitouScreen.primaryScreens()
    val colors = MaterialTheme.jitouColors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .shadow(12.dp, RoundedCornerShape(34.dp), ambientColor = colors.line, spotColor = colors.line)
            .background(colors.surface, RoundedCornerShape(34.dp))
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(34.dp))
            .padding(6.dp),
    ) {
        val itemWidth = maxWidth / screens.size
        val indicatorPosition = pagePosition.coerceIn(0f, (screens.size - 1).toFloat())

        Box(
            modifier = Modifier
                .offset(x = itemWidth * indicatorPosition)
                .width(itemWidth)
                .height(58.dp)
                .background(colors.surfaceMuted, RoundedCornerShape(28.dp)),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.jitouColors

    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(23.dp),
            tint = if (selected) colors.accentStrong else colors.ink,
        )
        Text(
            text = label,
            color = if (selected) colors.accentStrong else colors.ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}
