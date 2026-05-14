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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jitou.app.R
import com.jitou.app.model.HaircutProposal
import com.jitou.app.model.QueueState
import java.time.LocalDate

internal const val SpeechBubbleTailAnchorFraction = 0.24f
internal const val SpeechBubbleTailDepthPx = 12f

private val SpeechBubbleShape = GenericShape { size, _ ->
    val tailDepth = SpeechBubbleTailDepthPx.coerceAtMost(size.height * 0.28f)
    val bodyBottom = size.height - tailDepth
    val radius = (bodyBottom * 0.42f).coerceAtMost(22f)
    val tailCenter = size.width * SpeechBubbleTailAnchorFraction

    moveTo(radius, 0f)
    cubicTo(size.width * 0.30f, -3f, size.width * 0.67f, -2f, size.width - radius, 0f)
    quadraticTo(size.width, 0f, size.width, radius)
    lineTo(size.width, bodyBottom - radius)
    quadraticTo(size.width, bodyBottom, size.width - radius, bodyBottom)
    lineTo(tailCenter + 12f, bodyBottom)
    quadraticTo(tailCenter + 3f, bodyBottom + tailDepth * 0.72f, tailCenter - 10f, size.height)
    quadraticTo(tailCenter - 5f, bodyBottom + tailDepth * 0.26f, tailCenter - 20f, bodyBottom)
    lineTo(radius, bodyBottom)
    quadraticTo(0f, bodyBottom, 0f, bodyBottom - radius)
    lineTo(0f, radius)
    quadraticTo(0f, 0f, radius, 0f)
    close()
}

internal fun getAvatarResourceByDays(daysSinceLast: Int?): Int {
    if (daysSinceLast == null) return R.drawable.avatar_days_0_5
    return when {
        daysSinceLast <= 5 -> R.drawable.avatar_days_0_5
        daysSinceLast <= 14 -> R.drawable.avatar_days_6_14
        daysSinceLast <= 24 -> R.drawable.avatar_days_15_24
        daysSinceLast <= 30 -> R.drawable.avatar_days_25_30
        else -> R.drawable.avatar_days_over_30
    }
}

@Composable
internal fun HomeTopBar(onProfileClick: () -> Unit, daysSinceLast: Int?) {
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
        ProfileChip(onClick = onProfileClick, daysSinceLast = daysSinceLast)
    }
}

@Composable
private fun ProfileChip(onClick: () -> Unit, daysSinceLast: Int?) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .background(WarmPanel, RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .padding(start = 7.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(id = getAvatarResourceByDays(daysSinceLast)),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = "我的",
            color = Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

internal data class HairIllustrationState(
    val imageRes: Int,
    val darkImageRes: Int,
    val bubbleText: String,
    val daysText: String,
)

internal fun hairIllustrationState(daysSinceLast: Int?, status: String): HairIllustrationState {
    val bubbleText = if (daysSinceLast == null) "先记一剪" else status
    val imageRes = when (bubbleText) {
        "先记一剪", "清爽得很" -> R.drawable.hair_state_001
        "还撑得住" -> R.drawable.hair_state_002
        "差不多该约了，几时头" -> R.drawable.hair_state_003
        "是时候头了" -> R.drawable.hair_state_004
        else -> R.drawable.hair_state_002
    }
    val darkImageRes = when (imageRes) {
        R.drawable.hair_state_001 -> R.drawable.hair_state_001_dark
        R.drawable.hair_state_002 -> R.drawable.hair_state_002_dark
        R.drawable.hair_state_003 -> R.drawable.hair_state_003_dark
        R.drawable.hair_state_004 -> R.drawable.hair_state_004_dark
        else -> R.drawable.hair_state_002_dark
    }
    return HairIllustrationState(
        imageRes = imageRes,
        darkImageRes = darkImageRes,
        bubbleText = bubbleText,
        daysText = daysSinceLast?.toString() ?: "--",
    )
}

internal fun hairIllustrationImageRes(imageRes: Int, darkImageRes: Int, background: Color): Int =
    if (background.luminance() < 0.5f) darkImageRes else imageRes

@Composable
internal fun HairIllustrationHero(
    daysSinceLast: Int?,
    status: String,
    todayDate: LocalDate,
) {
    val illustration = hairIllustrationState(daysSinceLast, status)
    val imageRes = hairIllustrationImageRes(illustration.imageRes, illustration.darkImageRes, HomeBackground)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(316.dp),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "当前头毛插画",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(286.dp)
                .padding(bottom = 18.dp),
            contentScale = ContentScale.Fit,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.TopEnd)
                .offset(y = (-6).dp)
        ) {
            StatusSpeechBubble(
                text = illustration.bubbleText,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 48.dp)
            )
        }
        DaysSticker(
            daysText = illustration.daysText,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 118.dp),
        )
        TodayLine(
            text = todayLineText(todayDate),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
        )
    }
}

@Composable
private fun StatusSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Surface, SpeechBubbleShape)
            .border(width = 1.dp, color = Ink, shape = SpeechBubbleShape)
            .padding(start = 13.dp, top = 6.dp, end = 13.dp, bottom = 14.dp),
    ) {
        Text(
            text = text,
            color = Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DaysSticker(
    daysText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .rotate(-5f)
            .background(Yellow, RoundedCornerShape(24.dp))
            .border(width = 2.dp, color = Ink, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = daysText,
            color = Ink,
            fontSize = if (daysText.length > 2) 30.sp else 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
        Text(
            text = "天没剪",
            color = Ink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
private fun TodayLine(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = MutedInk,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
            .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = SoftLine, spotColor = SoftLine)
            .background(tint, RoundedCornerShape(24.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            color = MutedInk,
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
        border = BorderStroke(1.dp, SoftLine),
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
            .background(Surface, RoundedCornerShape(28.dp))
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
                text = "剪头打卡",
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
    friendDaysSinceLast: Int?,
    showFriendQueueNotice: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showFriendQueueNotice) 176.dp else 150.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = SoftLine, spotColor = SoftLine)
            .background(WarmPanel, RoundedCornerShape(28.dp))
            .border(width = 1.dp, color = SoftLine, shape = RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
    ) {
        CoopBadge(modifier = Modifier.align(Alignment.TopEnd))

        CoopLeadingIcon(
            hasProposal = proposal != null,
            friendName = friendName,
            friendDaysSinceLast = friendDaysSinceLast,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 18.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 88.dp, end = 16.dp, top = 30.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
private fun CoopLeadingIcon(
    hasProposal: Boolean,
    friendName: String,
    friendDaysSinceLast: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(54.dp)
            .background(Surface, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!hasProposal) {
            Icon(
                imageVector = Icons.Rounded.EditCalendar,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Ink,
            )
        } else {
            Image(
                painter = painterResource(id = getAvatarResourceByDays(friendDaysSinceLast)),
                contentDescription = null,
                modifier = Modifier.size(54.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .background(Yellow, RoundedCornerShape(8.dp))
                    .border(1.dp, SoftLine, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = friendName.take(2),
                    color = Ink,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
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
                color = Ink,
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
                color = Ink,
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
            .background(Surface, RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MutedInk,
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
