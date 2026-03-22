package com.example.personalhealthcareapp

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.personalhealthcareapp.ViewModel.ViewModel
import com.example.personalhealthcareapp.chat_managment.Chat
import com.example.personalhealthcareapp.chat_managment.Conversation
import com.example.personalhealthcareapp.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalHealthCareAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreenWithDrawer()
                }
            }
        }
    }
}

// ── Root composable with ModalNavigationDrawer ───────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWithDrawer(viewModel: ViewModel = viewModel()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val conversations by viewModel.conversations.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ChatDrawerContent(
                conversations = conversations,
                activeConversationId = viewModel.activeConversationId.collectAsState().value,
                onNewChat = {
                    viewModel.startNewChat()
                    scope.launch { drawerState.close() }
                },
                onSelectConversation = { id ->
                    viewModel.switchConversation(id)
                    scope.launch { drawerState.close() }
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversation(id)
                }
            )
        }
    ) {
        ChatScreen(
            viewModel = viewModel,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

// ── Navigation Drawer Content ────────────────────────────────────────

@Composable
fun ChatDrawerContent(
    conversations: List<Conversation>,
    activeConversationId: String?,
    onNewChat: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = DrawerBackground,
        modifier = Modifier.width(300.dp)
    ) {
        // ── Header ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "NeuroPocket",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // New Chat button
            Button(
                onClick = onNewChat,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp)

        // ── Conversation History Label ───────────────────────────
        Text(
            "Recent Chats",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant
        )

        // ── Conversation List ────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(conversations, key = { it.id }) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    isActive = conversation.id == activeConversationId,
                    onClick = { onSelectConversation(conversation.id) },
                    onDelete = { onDeleteConversation(conversation.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = when {
        isActive -> SurfaceContainerHigh
        else -> DrawerBackground
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) Primary else OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = DateUtils.getRelativeTimeSpanString(
                    conversation.updatedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = DeleteRed.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Chat Screen (updated to accept menu callback) ────────────────────

@Composable
fun ChatScreen(viewModel: ViewModel, onMenuClick: () -> Unit) {
    val chatHistory by viewModel.chathistory.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    var userProjectInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when a new message is added
    val itemCount = chatHistory.size + 1 + if (isGenerating) 1 else 0
    LaunchedEffect(chatHistory.size, isGenerating) {
        if (itemCount > 1) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    // Auto-scroll as tokens stream
    val lastMessageText = chatHistory.lastOrNull()?.text.orEmpty()
    LaunchedEffect(lastMessageText) {
        if (itemCount > 1) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                HeaderGreeting()
            }
            items(chatHistory) { message ->
                ChatBubble(message)
            }
            if (isGenerating) {
                item {
                    PulseIndicator()
                }
            }
        }

        // Top header with menu icon
        TopHeader(
            onMenuClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Bottom input
        InputAnchor(
            inputState = userProjectInput,
            onInputChange = { userProjectInput = it },
            onSend = {
                if (userProjectInput.isNotBlank()) {
                    viewModel.sendMessage(userProjectInput)
                    userProjectInput = ""
                }
            },
            enabled = !isGenerating,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun HeaderGreeting() {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            buildAnnotatedString {
                append("Hi\nAshutosh.\n")
                withStyle(style = SpanStyle(color = Primary)) {
                    append("NeuroPocket")
                }
                append("\nis active.")
            },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "How can I assist your cognitive workflow today?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TopHeader(onMenuClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Surface.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: hamburger menu + logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Open chat history",
                    tint = OnSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "NeuroPocket",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(SurfaceContainerHigh, shape = CircleShape)
        )
    }
}

@Composable
fun ChatBubble(message: Chat) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = when {
        message.isError -> SurfaceContainerHigh
        isUser -> SurfaceContainerHighest
        else -> SurfaceContainerLow
    }

    val shape = if (isUser) RoundedCornerShape(6.dp) else RoundedCornerShape(12.dp)
    val textStyle = if (isUser) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    val textColor = when {
        message.isError -> Primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape)
                .padding(20.dp)
                .widthIn(max = 280.dp)
        ) {
            if (message.isLoading && message.text.isEmpty()) {
                PulseIndicator()
            } else {
                Text(text = message.text, style = textStyle, color = textColor)
            }
        }
    }
}

@Composable
fun PulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(Primary.copy(alpha = alpha), shape = CircleShape)
    )
}

@Composable
fun InputAnchor(
    inputState: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val outlineAlpha by animateFloatAsState(targetValue = if (isFocused) 0.2f else 0f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHighest.copy(alpha = 0.8f))
            .border(1.dp, Primary.copy(alpha = outlineAlpha), RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { BasicTextField(
            value = inputState,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground),
            cursorBrush = SolidColor(Primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                if (inputState.isEmpty()) {
                    Text("Inquire about a topic...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                innerTextField()
            }
        )

        val primaryGradient = Brush.linearGradient(
            colors = listOf(Primary, PrimaryContainer)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) primaryGradient else Brush.linearGradient(listOf(SurfaceContainerHigh, SurfaceContainerHigh)))
                .clickable(enabled = enabled) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Send",
                tint = OnPrimary
            )
        }
    }
}