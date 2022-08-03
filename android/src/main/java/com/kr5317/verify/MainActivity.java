package com.kr5317.verify;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.*;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView etUrl, etCode, btnVerify, btnCopy;
    private String ticket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        etUrl = findViewById(R.id.etUrl);
        etCode = findViewById(R.id.etCode);
        btnVerify = findViewById(R.id.btnVerify);
        btnCopy = findViewById(R.id.btnCopy);

        btnVerify.setOnClickListener(v -> {
            final String url = etUrl.getText().toString();
            if (!URLUtil.isValidUrl(url)) {
                Toast.makeText(this, "请粘贴滑块验证码地址", Toast.LENGTH_LONG).show();
                return;
            }
            new VerifyDialog(this, url, ticket -> {
                this.ticket = ticket;
                etCode.setText(ticket);
            }).show();
        });
        btnCopy.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(ticket)) {
                Utils.setClipText(this, "ticket", ticket);
                Toast.makeText(this, "ticket已复制到剪切板", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "你还没有进行打开验证", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static class VerifyDialog extends Dialog {
        public VerifyDialog(Context context, String url, Call call) {
            super(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
            WebView webView = new WebView(context);
            setContentView(webView, new ViewGroup.LayoutParams(Utils.dp2px(context, 300), Utils.dp2px(context, 400)));
            final Window window = getWindow();
            if (window != null) {
                window.setGravity(Gravity.CENTER);
            }
            setCancelable(true);
            setCanceledOnTouchOutside(false);

            final WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setUserAgentString("QQClient");
            webView.addJavascriptInterface(new Bridge(ticket -> {
                webView.post(() -> {
                    dismiss();
                    call.onTicket(ticket);
                });
            }), "bridge");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    webView.evaluateJavascript("mqq.invoke = function(a,b,c){ return bridge.invoke(a,b,JSON.stringify(c))}", value -> {
                    });
                }
            });
            webView.loadUrl(url);
            setOnDismissListener(dialog -> Utils.destroyWebView(webView, "bridge"));
        }
    }

    public static class Bridge {
        private Call call;

        public Bridge(Call call) {
            this.call = call;
        }

        @JavascriptInterface
        public void invoke(String cls, String method, String text) {
            if (text != null) {
                String key = "\"ticket\":\"";
                if (text.contains(key)) {
                    String text2 = text.substring(text.indexOf(key) + key.length());
                    String text3 = text2.substring(0, text2.indexOf("\""));
                    if (!TextUtils.isEmpty(text3)) {
                        call.onTicket(text3);
                    }
                }
            }
        }
    }

    private interface Call {
        void onTicket(String ticket);
    }
}
