package br.app.egger.jarvis.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.app.egger.jarvis.model.BrainNote
import br.app.egger.jarvis.model.SecondBrain
import br.app.egger.jarvis.model.VersionHistory
import br.app.egger.jarvis.session.JarvisSession
import br.app.egger.jarvis.session.JarvisState
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RootScreen(session: JarvisSession) {
    val state by session.state.collectAsState()
    val userLine by session.userLine.collectAsState()
    val assistantLine by session.assistantLine.collectAsState()
    val notes by session.notes.collectAsState()
    val isActivated by session.isActivated.collectAsState()
    val isStarting by session.isStarting.collectAsState()

    var typedCommand by remember { mutableStateOf("") }
    var showingSettings by remember { mutableStateOf(false) }
    var showingVersionHistory by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<BrainNote?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Color(0xFF021721),
                        Color.Black
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            TopPanel(
                state = state,
                isActivated = isActivated,
                isStarting = isStarting,
                onSettingsClick = { showingSettings = true },
                onToggleActivation = {
                    if (isActivated) session.stop() else session.start()
                }
            )

            OrbView(
                state = state,
                onTap = { session.sendTypedCommand("status") }
            )

            DialogueCard(
                userLine = userLine,
                assistantLine = assistantLine,
                typedCommand = typedCommand,
                onTypedCommandChange = { typedCommand = it },
                onSend = {
                    val cmd = typedCommand
                    typedCommand = ""
                    session.sendTypedCommand(cmd)
                }
            )

            SecondBrainSection(
                notes = notes,
                onNoteSelect = { editingNote = it }
            )

            VersionFooter(
                onVersionClick = { showingVersionHistory = true }
            )
        }
    }

    if (showingSettings) {
        SettingsDialog(
            session = session,
            onDismiss = { showingSettings = false }
        )
    }

    if (showingVersionHistory) {
        VersionHistoryDialog(
            onDismiss = { showingVersionHistory = false }
        )
    }

    editingNote?.let { note ->
        BrainNoteEditorDialog(
            note = note,
            onDismiss = { editingNote = null },
            onSave = { updated ->
                session.updateNote(updated)
                editingNote = null
            }
        )
    }
}

@Composable
private fun TopPanel(
    state: JarvisState,
    isActivated: Boolean,
    isStarting: Boolean,
    onSettingsClick: () -> Unit,
    onToggleActivation: () -> Unit
) {
    val statusColor = when (state) {
        JarvisState.IDLE -> Color.Gray
        JarvisState.LISTENING -> Color(0xFF00E5FF)
        JarvisState.THINKING -> Color(0xFFFF9100)
        JarvisState.SPEAKING -> Color(0xFF00E676)
        JarvisState.ERROR -> Color(0xFFFF5252)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(Color(0x1400E5FF), CircleShape)
                .border(1.dp, Color(0x4000E5FF), CircleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .shadow(8.dp, CircleShape, spotColor = statusColor)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = state.rawValue,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onSettingsClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    tint = Color(0xFF00E5FF)
                )
            }

            Button(
                onClick = onToggleActivation,
                enabled = !isStarting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActivated) Color(0xFFFF5252) else Color(0xFF00E5FF),
                    contentColor = if (isActivated) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActivated) "Parar" else (if (isStarting) "Ativando" else "Ativar"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrbView(
    state: JarvisState,
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state == JarvisState.LISTENING) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(scalePulse)
                .clickable { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerPt = Offset(size.width / 2, size.height / 2)

                // Outer Ring
                drawCircle(
                    color = Color(0x2E00E5FF),
                    radius = size.width / 2 - 2,
                    center = centerPt,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Core Radial Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF00E5FF),
                            Color(0x400044FF),
                            Color.Transparent
                        ),
                        center = centerPt,
                        radius = 115.dp.toPx()
                    ),
                    radius = 115.dp.toPx(),
                    center = centerPt
                )

                // Center White Core
                drawCircle(
                    color = Color.White,
                    radius = 17.dp.toPx(),
                    center = centerPt
                )
            }
        }
    }
}

@Composable
private fun DialogueCard(
    userLine: String,
    assistantLine: String,
    typedCommand: String,
    onTypedCommandChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "JARVIS",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF00E5FF)
        )

        // User Line
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0EFFFFFF), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "SENHOR",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userLine,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // Assistant Line
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0EFFFFFF), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "JARVIS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = assistantLine,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // Command Entry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = typedCommand,
                onValueChange = onTypedCommandChange,
                placeholder = { Text("ou digite e toque Enviar", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0x0Fffffff),
                    unfocusedContainerColor = Color(0x0Fffffff),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enviar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecondBrainSection(
    notes: List<BrainNote>,
    onNoteSelect: (BrainNote) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x09FFFFFF), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0x2E00E5FF), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SECOND BRAIN",
                    color = Color(0xFF00E5FF),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Contexto injetado em todos os comandos.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${notes.size} notas",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Graph Canvas
        BrainGraphCanvas(
            notes = notes,
            onSelectNote = onNoteSelect,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )

        // Cards Grid
        FlowRow(
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            notes.forEach { note ->
                val areaInfo = SecondBrain.areas[note.area]
                val color = areaInfo?.color ?: Color.Gray

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .background(Color(0x0CFFFFFF), RoundedCornerShape(8.dp))
                        .clickable { onNoteSelect(note) }
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = note.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = areaInfo?.label ?: note.area,
                            color = color,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrainGraphCanvas(
    notes: List<BrainNote>,
    onSelectNote: (BrainNote) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    var notePositions by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }

    Box(
        modifier = modifier.pointerInput(notes) {
            detectTapGestures { tapOffset ->
                notePositions.forEach { (id, pos) ->
                    val distance = (tapOffset - pos).getDistance()
                    if (distance <= 25.dp.toPx()) {
                        notes.firstOrNull { it.id == id }?.let { onSelectNote(it) }
                        return@detectTapGestures
                    }
                }
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radiusX = (size.width * 0.38f).coerceAtLeast(90.dp.toPx())
            val radiusY = (size.height * 0.32f).coerceAtLeast(80.dp.toPx())

            val positions = mutableMapOf<String, Offset>()
            val count = maxOf(notes.size, 1)

            notes.forEachIndexed { index, note ->
                val angle = -Math.PI.toFloat() / 2f + (2f * Math.PI.toFloat() * index.toFloat() / count.toFloat())
                val x = center.x + cos(angle) * radiusX
                val y = center.y + sin(angle) * radiusY
                positions[note.id] = Offset(x, y)
            }
            notePositions = positions

            // Draw Relation Quad Curves
            SecondBrain.relations.forEach { (startId, endId) ->
                val start = positions[startId]
                val end = positions[endId]
                if (start != null && end != null) {
                    val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                    val control = Offset(
                        mid.x + (center.x - mid.x) * 0.35f,
                        mid.y + (center.y - mid.y) * 0.35f
                    )

                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        quadraticTo(control.x, control.y, end.x, end.y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0x3800E5FF),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Draw Center Brain Node
            drawCircle(
                color = Color(0xFFE040FB),
                radius = 28.dp.toPx(),
                center = center
            )

            val brainEmoji = "🧠"
            val textLayoutResult = textMeasurer.measure(
                text = brainEmoji,
                style = TextStyle(fontSize = 24.sp)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(center.x - textLayoutResult.size.width / 2, center.y - textLayoutResult.size.height / 2)
            )

            // Draw Note Nodes
            notes.forEach { note ->
                positions[note.id]?.let { pos ->
                    val color = SecondBrain.areas[note.area]?.color ?: Color.Gray

                    drawCircle(
                        color = color,
                        radius = 14.dp.toPx(),
                        center = pos
                    )

                    val titleLayout = textMeasurer.measure(
                        text = note.title,
                        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    )
                    drawText(
                        textLayoutResult = titleLayout,
                        topLeft = Offset(pos.x - titleLayout.size.width / 2, pos.y + 16.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionFooter(
    onVersionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "v${VersionHistory.CURRENT_VERSION_FALLBACK} (Build ${VersionHistory.CURRENT_BUILD_FALLBACK}) — 17/07/2026",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onVersionClick() }
        )

        Text(
            text = "commit ${VersionHistory.CURRENT_COMMIT_FALLBACK}",
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}
