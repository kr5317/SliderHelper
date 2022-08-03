package com.kr5317.verify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.awt.BorderLayout
import java.awt.Container
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.security.Permission
import java.security.Principal
import java.security.cert.Certificate
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.swing.JPanel


private var initURL = false

class TicketEvent(val ticket: String)

data class LoginVerifyEvent(val url: String, val event: LoginVerifyResultEvent)
class LoginVerifyResultEvent(var ticket: String?)

private fun initURL() {
    if (initURL) return
    Platform.setImplicitExit(false)
    initURL = true
    URL.setURLStreamHandlerFactory {
        val handler = Class.forName("sun.net.www.protocol.$it.Handler")
            .getDeclaredConstructor().newInstance() as URLStreamHandler
        if ("https" != it) return@setURLStreamHandlerFactory handler
        val method = URLStreamHandler::class.java.getDeclaredMethod("openConnection", URL::class.java)
        method.isAccessible = true
        return@setURLStreamHandlerFactory object : URLStreamHandler() {
            override fun openConnection(u: URL): URLConnection {
                val urlConnection = method.invoke(handler, u) as HttpsURLConnection
                if (u.toString().contains("t.captcha.qq.com/cap_union_new_verify")) {
                    return HttpURLConnectionProxy(u, urlConnection)
                }
                return urlConnection
            }

            override fun openConnection(u: URL, p: Proxy?): URLConnection {
                return openConnection(u)
            }
        }
    }
}

@Composable
fun Verify() {
    initURL()
    var verify by remember { mutableStateOf<LoginVerifyEvent?>(null) }
    EventBusReceive<LoginVerifyEvent> {
        verify = null
        verify = it
    }
    verify?.let { event ->
        VerifyDialog(event.url) {
            verify = null
            event.event.ticket = it
            EventBus.send(event.event)
        }
    }
}

@Composable
private fun VerifyDialog(url: String, onResult: (ticket: String?) -> Unit) {
    EventBusReceive<TicketEvent> {
        onResult(it.ticket)
    }
    val dialogState = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(480.dp, 480.dp))
    Dialog(
        onCloseRequest = { onResult(null) },
        title = "验证码",
        state = dialogState,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Verify(url)
        }
    }
}

@Composable
private fun DialogWindowScope.Verify(url: String) {
    val jfxPanel = remember { JFXPanel() }
    val container = this.window
    JavaFXPanel(
        root = container,
        panel = jfxPanel,
        onCreate = {
            Platform.runLater {
                val webView = WebView()
                webView.engine.userAgent = "QQClient"
                jfxPanel.scene = Scene(webView)
                webView.engine.load(url)
            }
        }
    )
}

@Composable
private fun JavaFXPanel(
    root: Container,
    panel: JFXPanel,
    onCreate: () -> Unit
) {
    val container = remember { JPanel().apply { layout = BorderLayout(0, 0) } }
    val density = LocalDensity.current.density

    Layout(
        content = {},
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size
            container.setBounds(
                (location.x / density).toInt(),
                (location.y / density).toInt(),
                (size.width / density).toInt(),
                (size.height / density).toInt()
            )
            container.validate()
            container.repaint()
        },
        measurePolicy = { _, _ ->
            layout(0, 0) {}
        }
    )

    DisposableEffect(Unit) {
        container.add(panel)
        root.add(container)
        onCreate()
        onDispose {
            container.remove(panel)
            root.remove(container)
        }
    }
}

class HttpURLConnectionProxy(url: URL, private val c: HttpsURLConnection) : HttpsURLConnection(url) {
    override fun getInputStream(): InputStream {
        val inputStream = c.inputStream
        val text = inputStream.bufferedReader().use { it.readText() }
        val key = "\"ticket\":\""
        if (text.contains(key)) {
            val text2 = text.substring(text.indexOf(key) + key.length)
            val text3 = text2.substring(0, text2.indexOf("\""))
            if (text3.isNotEmpty()) {
                EventBus.send(TicketEvent(text3))
            }
        }
        return text.byteInputStream()
    }

    override fun connect() {
        c.connect()
    }

    override fun disconnect() {
        c.disconnect()
    }

    override fun usingProxy(): Boolean {
        return c.usingProxy()
    }

    override fun getCipherSuite(): String {
        return c.cipherSuite
    }

    override fun getLocalCertificates(): Array<Certificate> {
        return c.localCertificates
    }

    override fun getServerCertificates(): Array<Certificate> {
        return c.serverCertificates
    }

    override fun getPeerPrincipal(): Principal {
        return c.peerPrincipal
    }

    override fun getLocalPrincipal(): Principal {
        return c.localPrincipal
    }

    override fun setHostnameVerifier(v: HostnameVerifier?) {
        c.hostnameVerifier = v
    }

    override fun getHostnameVerifier(): HostnameVerifier {
        return c.hostnameVerifier
    }

    override fun setSSLSocketFactory(sf: SSLSocketFactory?) {
        c.sslSocketFactory = sf
    }

    override fun getSSLSocketFactory(): SSLSocketFactory {
        return c.sslSocketFactory
    }

    override fun getSSLSession(): Optional<SSLSession> {
        return c.sslSession
    }

    override fun setAllowUserInteraction(allowuserinteraction: Boolean) {
        c.allowUserInteraction = allowuserinteraction
    }

    override fun setAuthenticator(auth: Authenticator?) {
        c.setAuthenticator(auth)
    }

    override fun setChunkedStreamingMode(chunklen: Int) {
        c.setChunkedStreamingMode(chunklen)
    }

    override fun setConnectTimeout(timeout: Int) {
        c.connectTimeout = timeout
    }

    override fun setReadTimeout(timeout: Int) {
        c.readTimeout = timeout
    }

    override fun setRequestMethod(method: String?) {
        c.requestMethod = method
    }

    override fun setRequestProperty(key: String?, value: String?) {
        c.setRequestProperty(key, value)
    }

    override fun setDefaultUseCaches(defaultusecaches: Boolean) {
        c.defaultUseCaches = defaultusecaches
    }

    override fun setDoInput(doinput: Boolean) {
        c.doInput = doinput
    }

    override fun setDoOutput(dooutput: Boolean) {
        c.doOutput = dooutput
    }

    override fun setFixedLengthStreamingMode(contentLength: Int) {
        c.setFixedLengthStreamingMode(contentLength)
    }

    override fun setFixedLengthStreamingMode(contentLength: Long) {
        c.setFixedLengthStreamingMode(contentLength)
    }

    override fun setIfModifiedSince(ifmodifiedsince: Long) {
        c.ifModifiedSince = ifmodifiedsince
    }

    override fun setInstanceFollowRedirects(followRedirects: Boolean) {
        c.instanceFollowRedirects = followRedirects
    }

    override fun setUseCaches(usecaches: Boolean) {
        c.useCaches = usecaches
    }

    override fun getContentLength(): Int {
        return c.contentLength
    }

    override fun getAllowUserInteraction(): Boolean {
        return c.allowUserInteraction
    }

    override fun getConnectTimeout(): Int {
        return c.connectTimeout
    }

    override fun getContent(): Any {
        return c.content
    }

    override fun getContent(classes: Array<out Class<*>>?): Any {
        return c.getContent(classes)
    }

    override fun getContentEncoding(): String? {
        return c.contentEncoding
    }

    override fun getContentLengthLong(): Long {
        return c.contentLengthLong
    }

    override fun getContentType(): String {
        return c.contentType
    }

    override fun getDate(): Long {
        return c.date
    }

    override fun getDefaultUseCaches(): Boolean {
        return c.defaultUseCaches
    }

    override fun getDoInput(): Boolean {
        return c.doInput
    }

    override fun getDoOutput(): Boolean {
        return c.doOutput
    }

    override fun getExpiration(): Long {
        return c.expiration
    }

    override fun getErrorStream(): InputStream? {
        return c.errorStream
    }

    override fun getHeaderField(name: String?): String {
        return c.getHeaderField(name)
    }

    override fun getHeaderField(n: Int): String {
        return c.getHeaderField(n)
    }

    override fun getHeaderFields(): MutableMap<String, MutableList<String>> {
        return c.headerFields
    }

    override fun getHeaderFieldKey(n: Int): String {
        return c.getHeaderFieldKey(n)
    }

    override fun getIfModifiedSince(): Long {
        return c.ifModifiedSince
    }

    override fun getInstanceFollowRedirects(): Boolean {
        return c.instanceFollowRedirects
    }

    override fun getLastModified(): Long {
        return c.lastModified
    }

    override fun getOutputStream(): OutputStream {
        return c.outputStream
    }

    override fun getPermission(): Permission {
        return c.permission
    }

    override fun getReadTimeout(): Int {
        return c.readTimeout
    }

    override fun getRequestMethod(): String {
        return c.requestMethod
    }

    override fun getRequestProperties(): MutableMap<String, MutableList<String>> {
        return c.requestProperties
    }

    override fun getResponseCode(): Int {
        return c.responseCode
    }

    override fun getResponseMessage(): String {
        return c.responseMessage
    }

    override fun getURL(): URL {
        return c.url
    }

    override fun getUseCaches(): Boolean {
        return c.useCaches
    }

    override fun getRequestProperty(key: String?): String {
        return c.getRequestProperty(key)
    }

    override fun getHeaderFieldDate(name: String?, Default: Long): Long {
        return c.getHeaderFieldDate(name, Default)
    }

    override fun getHeaderFieldInt(name: String?, Default: Int): Int {
        return c.getHeaderFieldInt(name, Default)
    }

    override fun getHeaderFieldLong(name: String?, Default: Long): Long {
        return c.getHeaderFieldLong(name, Default)
    }
}