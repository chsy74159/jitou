package com.jitou.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.R
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.QueueState
import com.jitou.app.model.ReminderUiState
import java.time.LocalDate

@Composable
internal fun HomeTopBar(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "几头",
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
            Text(
                text = "jitou haircut tracker",
                color = MutedInk,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
        }
        ProfileChip(onClick = onProfileClick)
    }
}

@Composable
private fun ProfileChip(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .background(Ink, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(start = 7.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.cartoon_avatar),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = "我的",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
internal fun AvatarHeroCard(
    daysSinceLast: Int,
    status: String,
    reminder: ReminderUiState,
    onReminderClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(318.dp)
            .shadow(18.dp, RoundedCornerShape(36.dp), ambientColor = Color(0x18000000), spotColor = Color(0x14000000))
            .background(Color.White, RoundedCornerShape(36.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(36.dp)),
    ) {
        HeroTag(
            text = "TODAY",
            containerColor = Ink,
            textColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 18.dp, start = 18.dp),
        )
        HeroTag(
            text = "SOFT CUT",
            containerColor = Mint,
            textColor = Ink,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 18.dp),
        )
        HeroAvatar(modifier = Modifier.align(Alignment.TopCenter))
        DaysCounter(
            daysSinceLast = daysSinceLast,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, top = 56.dp),
        )
        ReminderStatusBlock(
            reminder = reminder,
            status = status,
            onReminderClick = onReminderClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HeroTag(
    text: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun HeroAvatar(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.cartoon_avatar),
        contentDescription = "卡通头像",
        modifier = modifier
            .padding(top = 54.dp)
            .size(178.dp)
            .clip(RoundedCornerShape(42.dp)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun DaysCounter(
    daysSinceLast: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 108.dp, height = 108.dp)
            .background(Yellow, RoundedCornerShape(30.dp))
            .border(width = 2.dp, color = Ink, shape = RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = daysSinceLast.toString(),
                color = Ink,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
            Text(
                text = "天没剪",
                color = Ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
        }
    }
}

@Composable
private fun ReminderStatusBlock(
    reminder: ReminderUiState,
    status: String,
    onReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WarmPanel, RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = if (reminder.enabled) "提醒 ${reminder.time.toReminderText()}" else "提醒已关闭",
            color = MutedInk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )
        StatusPill(status = status, onClick = onReminderClick)
    }
}

@Composable
internal fun SummaryStrip(
    lastDate: LocalDate?,
    averageIntervalDays: Int,
    nextDate: LocalDate,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCapsule(
            label = "上次",
            value = lastDate?.format(DateFormatter) ?: "暂无",
            tint = Peach,
            modifier = Modifier.weight(1f),
        )
        SummaryCapsule(
            label = "均值",
            value = if (averageIntervalDays == 0) "暂无" else "${averageIntervalDays}天",
            tint = Mint,
            modifier = Modifier.weight(1f),
        )
        SummaryCapsule(
            label = "建议下次",
            value = nextDate.format(DateFormatter),
            tint = Yellow,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCapsule(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(78.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x0F000000), spotColor = Color(0x0A000000))
            .background(tint, RoundedCornerShape(24.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = Color(0xFF57544E),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )
        Text(
            text = value,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(status: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFF4F1EA), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "头毛状态：$status",
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Yellow,
            )
        }
    }
}

@Composable
private fun SoftActionButton(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Ink,
        ),
        border = BorderStroke(2.dp, Color(0x12000000)),
        elevation = null,
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
        )
    }
}

@Composable
internal fun PrimaryActionRow(
    queueButtonText: String,
    onQueueClick: () -> Unit,
    onNowClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(28.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "几时头",
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
            )
            Text(
                text = "tap to update",
                color = MutedInk,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SoftActionButton(
                text = queueButtonText,
                containerColor = WarmPanel,
                modifier = Modifier.weight(0.95f),
                onClick = onQueueClick,
            )
            SoftActionButton(
                text = "记录剪头",
                containerColor = Yellow,
                modifier = Modifier.weight(1.25f),
                onClick = onNowClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCut,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Ink,
                )
            }
        }
    }
}

@Composable
internal fun CoopPanel(
    proposal: HaircutProposal?,
    friendName: String,
    showFriendQueueNotice: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showFriendQueueNotice) 176.dp else 150.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x14000000), spotColor = Color(0x0F000000))
            .background(Ink, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
    ) {
        CoopBadge(modifier = Modifier.align(Alignment.TopEnd))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 16.dp, top = 30.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoopLeadingIcon()
            Spacer(modifier = Modifier.width(16.dp))
            CoopContent(
                proposal = proposal,
                friendName = friendName,
                showFriendQueueNotice = showFriendQueueNotice,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CoopBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 14.dp, end = 14.dp)
            .background(Yellow, RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "CO-OP",
            color = Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun CoopLeadingIcon() {
    Box(
        modifier = Modifier
            .size(54.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Groups,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Ink,
        )
    }
}

@Composable
private fun CoopContent(
    proposal: HaircutProposal?,
    friendName: String,
    showFriendQueueNotice: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (proposal == null) {
            Text(
                text = "暂无约头计划",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SmallOutlinedLabel("双方还没有发起日程")
            SmallYellowLabel("发起约剪")
        } else {
            Text(
                text = "与${friendName}约头计划",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SmallOutlinedLabel("初定时间：${proposal.proposedDate.format(DateFormatter)}")
            SmallYellowLabel("状态：${proposal.statusLabel()}")
            if (showFriendQueueNotice) {
                SmallOutlinedLabel(QueueState.queueNoticeText(isOwnCard = true))
            }
        }
    }
}

@Composable
private fun SmallOutlinedLabel(text: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SmallYellowLabel(text: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .background(Yellow, RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
