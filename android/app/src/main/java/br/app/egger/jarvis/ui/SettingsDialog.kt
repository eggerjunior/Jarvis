package br.app.egger.jarvis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.app.egger.jarvis.model.AIProvider
import br.app.egger.jarvis.model.JarvisVoicePreference
import br.app.egger.jarvis.session.JarvisSession

@Composable
fun SettingsDialog(
    session: JarvisSession,
    onDismiss: () -> Unit
) {
    val selectedProvider by session.selectedProvider.collectAsState()
    val anthropicApiKey by session.anthropicApiKey.collectAsState()
    val openRouterApiKey by session.openRouterApiKey.collectAsState()
    val omniRouteApiKey by session.omniRouteApiKey.collectAsState()
    val selectedModel by session.selectedModel.collectAsState()
    val candidateModel by session.candidateModel.collectAsState()
    val selectedVoicePreference by session.selectedVoicePreference.collectAsState()
    val selectedVoiceIdentifier by session.selectedVoiceIdentifier.collectAsState()
    val modelTestLine by session.modelTestLine.collectAsState()
    val isTestingModel by session.isTestingModel.collectAsState()
    val availableProviderModels by session.availableProviderModels.collectAsState()
    val isLoadingProviderModels by session.isLoadingProviderModels.collectAsState()
    val modelCatalogError by session.modelCatalogError.collectAsState()

    val currentApiKey = when (selectedProvider) {
        AIProvider.ANTHROPIC -> anthropicApiKey
        AIProvider.OPEN_ROUTER -> openRouterApiKey
        AIProvider.OMNI_ROUTE -> omniRouteApiKey
    }

    LaunchedEffect(selectedProvider, currentApiKey) {
        if (currentApiKey.isNotBlank() && availableProviderModels[selectedProvider] == null) {
            session.refreshSelectedProviderModels()
        }
    }

    var showKey by remember { mutableStateOf(false) }
    var voiceMenuExpanded by remember { mutableStateOf(false) }
    var providerSearch by remember { mutableStateOf("") }
    var modelSearch by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF040D12),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5500E5FF)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configurações",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Concluir", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // API KEY Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x5900E5FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "API KEY",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentApiKey,
                                onValueChange = {
                                    when (selectedProvider) {
                                        AIProvider.ANTHROPIC -> session.setAnthropicApiKey(it)
                                        AIProvider.OPEN_ROUTER -> session.setOpenRouterApiKey(it)
                                        AIProvider.OMNI_ROUTE -> session.setOmniRouteApiKey(it)
                                    }
                                },
                                placeholder = { Text(selectedProvider.apiKeyPlaceholder, color = Color.Gray) },
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = { showKey = !showKey },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5900E5FF))
                            ) {
                                Text(if (showKey) "Ocultar" else "Mostrar", fontSize = 12.sp)
                            }
                        }
                    }

                    // PROVEDOR Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x5900E5FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "PROVEDOR",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = providerSearch,
                            onValueChange = { providerSearch = it },
                            placeholder = { Text("Digite para buscar provedor", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            AIProvider.entries
                                .filter { it.label.contains(providerSearch, ignoreCase = true) }
                                .forEach { provider ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = provider == selectedProvider,
                                            onCheckedChange = { checked ->
                                                if (checked) session.setSelectedProvider(provider)
                                            }
                                        )
                                        Text(provider.label, color = Color.White)
                                    }
                                }
                        }
                    }

                    // MODELO Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x5900E5FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MODELO",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { session.refreshSelectedProviderModels() },
                                enabled = !isLoadingProviderModels
                            ) {
                                Text(if (isLoadingProviderModels) "Buscando…" else "Atualizar modelos")
                            }
                        }

                        OutlinedTextField(
                            value = modelSearch,
                            onValueChange = { modelSearch = it },
                            placeholder = { Text("Digite para buscar modelo", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val availableModels = session.availableModelsForSelectedProvider()
                            val filteredModels = availableModels.filter {
                                it.id.contains(modelSearch, ignoreCase = true) ||
                                    it.label.contains(modelSearch, ignoreCase = true)
                            }
                            filteredModels.forEach { model ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = model.id == candidateModel,
                                        enabled = !isLoadingProviderModels,
                                        onCheckedChange = { checked ->
                                            if (checked) session.setCandidateModel(model.id)
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(model.label, color = Color.White)
                                        Text("${model.id} · ${model.price}", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }
                            if (filteredModels.isEmpty()) {
                                Text(
                                    text = if (modelSearch.isNotBlank()) "Nenhum modelo corresponde à busca."
                                    else if (isLoadingProviderModels) "Carregando modelos…"
                                    else "Nenhum modelo carregado para este provedor.",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            if (modelCatalogError.isNotBlank()) {
                                Text(modelCatalogError, color = Color(0xFFFF8A80), fontSize = 12.sp)
                            }
                        }
                    }

                    // TESTE Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x5900E5FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TESTE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { session.testSelectedModel() },
                                enabled = !isTestingModel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isTestingModel) "Testando" else "Testar modelo", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { session.useCandidateModel() },
                                enabled = candidateModel != selectedModel && !isTestingModel,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Usar no Jarvis", fontSize = 12.sp)
                            }
                        }

                        SelectionContainer {
                            Text(
                                text = modelTestLine,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // VOZ Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1400E5FF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x5900E5FF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "VOZ",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JarvisVoicePreference.entries.forEach { voice ->
                                val isSelected = voice == selectedVoicePreference
                                Button(
                                    onClick = { session.setSelectedVoicePreference(voice) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF00E5FF) else Color(0x22FFFFFF),
                                        contentColor = if (isSelected) Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(voice.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                val availableVoices = session.availableVoiceOptions()
                                val selectedVoiceObj = availableVoices.firstOrNull { it.id == selectedVoiceIdentifier }

                                OutlinedButton(
                                    onClick = { voiceMenuExpanded = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5900E5FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedVoiceObj?.label ?: "Automática do Android",
                                        fontSize = 12.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                DropdownMenu(
                                    expanded = voiceMenuExpanded,
                                    onDismissRequest = { voiceMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Automática do Android") },
                                        onClick = {
                                            session.setSelectedVoiceIdentifier("")
                                            voiceMenuExpanded = false
                                        }
                                    )
                                    availableVoices.forEach { voice ->
                                        DropdownMenuItem(
                                            text = { Text(voice.label) },
                                            onClick = {
                                                session.setSelectedVoiceIdentifier(voice.id)
                                                voiceMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { session.testSelectedVoice() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Testar voz", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
