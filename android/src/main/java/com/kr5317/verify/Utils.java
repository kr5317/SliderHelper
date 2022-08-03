package com.kr5317.verify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.webkit.WebView;

public class Utils {
    public static void destroyWebView(WebView webView, String jsInterfaceName) {
        try {
            if (webView != null && !"destroy".equals(webView.getTag())) {
                webView.setTag("destroy");
                webView.removeJavascriptInterface(jsInterfaceName);
                webView.setWebChromeClient(null);
                webView.setWebViewClient(null);
                webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                webView.clearHistory();
                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                webView.onPause();
                webView.removeAllViews();
                webView.destroyDrawingCache();
                webView.destroy();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean setClipText(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text));
            return true;
        }
        return false;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics()) + 0.5f);
    }
}
