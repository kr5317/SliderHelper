package com.kr5317.verify

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.system.exitProcess

fun main() {
    application {
        Window(
            onCloseRequest = {
                exitApplication()
                exitProcess(0)
            },
            title = "验证码助手",
            icon = painterResource("drawable-nodpi/icon.png"),
        ) {
            val (url, setUrl) = remember { mutableStateOf("") }
            var ticket by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("请在下方输入验证码地址") }
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                Text(text = message)
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = url,
                    onValueChange = setUrl,
                )
                Spacer(Modifier.height(16.dp))
                if (ticket.isEmpty()) {
                    Text(text = "请等待验证完成后手动复制")
                } else {
                    TextField(
                        value = ticket,
                        onValueChange = { },
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button({
                    if (url.isEmpty() || !url.startsWith("http")) {
                        message = "请先输入验证码地址"
                    } else {
                        EventBus.send(LoginVerifyEvent(url, LoginVerifyResultEvent(null)))
                    }
                }) { Text("开始验证") }
                Spacer(Modifier.height(8.dp))
            }

            EventBusReceive<LoginVerifyResultEvent> {
                ticket = it.ticket ?: ""
            }
            Verify()
        }
    }
}