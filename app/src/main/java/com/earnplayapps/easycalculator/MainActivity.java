package com.earnplayapps.easycalculator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.*;
import com.google.android.gms.ads.rewarded.*;
import com.google.android.ump.*;

public class MainActivity {
 private static final String B=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/9214589741":"ca-app-pub-9940728659432865/2663696435";
 private static final String I=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/1033173712":"ca-app-pub-9940728659432865/4188531092";
 private static final String R=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/5224354917":"ca-app-pub-9940728659432865/2875449427";
 private WebView w; private AdView banner; private InterstitialAd inter; private RewardedAd reward; private ConsentInformation consent;
 private long lastInter; private boolean ads; private boolean bannerLoading; private boolean interLoading; private boolean rewardLoading;
 private final Handler handler=new Handler(Looper.getMainLooper());
 @Override protected void onCreate(Bundle b){super.onCreate(b);ui();consent();}
 private void ui(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(245,247,251));
  root.setOnApplyWindowInsetsListener((v,insets)->{int top,bottom;if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}v.setPadding(0,top,0,bottom);return insets;});
  w=new WebView(this);WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(false);s.setLoadWithOverviewMode(false);s.setUseWideViewPort(false);
  w.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){applyAdaptiveLayout();w.setVisibility(View.VISIBLE);}});w.setWebChromeClient(new WebChromeClient());w.addJavascriptInterface(new Bridge(),"Android");root.addView(w,new LinearLayout.LayoutParams(-1,0,1));
  banner=new AdView(this);banner.setAdUnitId(B);banner.setAdListener(new AdListener(){@Override public void onAdLoaded(){bannerLoading=false;banner.setVisibility(View.VISIBLE);}@Override public void onAdFailedToLoad(LoadAdError e){bannerLoading=false;banner.setVisibility(View.GONE);handler.postDelayed(()->loadBanner(),30000);}});banner.setAdSize(getAdaptiveBannerSize());banner.setVisibility(View.GONE);root.addView(banner,new LinearLayout.LayoutParams(-1,-2));
  setContentView(root);root.requestApplyInsets();w.loadUrl("file:///android_asset/index.html");
 }
 private AdSize getAdaptiveBannerSize(){int widthPx=getResources().getDisplayMetrics().widthPixels;float density=getResources().getDisplayMetrics().density;int widthDp=Math.max(320,Math.round(widthPx/density));return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,widthDp);}
 private void applyAdaptiveLayout(){if(w==null)return;String js="(function(){var s=document.getElementById('ecAdaptive');if(!s){s=document.createElement('style');s.id='ecAdaptive';document.head.appendChild(s);}s.textContent=\"#app{width:100%;max-width:460px;margin:0 auto;min-height:100%;height:100%;padding:14px 12px 8px;display:flex;flex-direction:column;overflow:hidden}.top{display:grid!important;grid-template-columns:minmax(0,1fr) auto auto!important;align-items:center!important;gap:6px!important;flex:0 0 auto}.brand{grid-column:1!important;grid-row:1!important;min-width:0!important}.headActions{grid-column:2!important;grid-row:1!important;min-width:0!important;display:flex!important;align-items:center!important;gap:4px!important}.hamb{grid-column:3!important;grid-row:1!important;justify-self:end!important;flex:0 0 38px!important}.brand h1{font-size:clamp(17px,4.8vw,19px)!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}.other{max-width:92px!important;overflow:hidden!important;text-overflow:ellipsis!important}.display{flex:0 0 auto!important}.quick{flex:0 0 auto!important}#app .keys{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:repeat(5,minmax(0,1fr))!important;grid-auto-rows:minmax(0,1fr)!important;gap:clamp(5px,1.7vw,9px)!important;flex:1 1 auto!important;min-height:0!important}#app .key{height:auto!important;min-height:0!important;min-width:0!important;font-size:clamp(16px,4.8vw,20px)!important}.note{flex:0 0 auto!important}\";document.documentElement.style.overflow='hidden';document.body.style.overflow='hidden';})();";w.evaluateJavascript(js,null);}
 private void consent(){consent=UserMessagingPlatform.getConsentInformation(this);consent.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),()->UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,e->{privacy();ads();}),e->{privacy();ads();});}
 private void privacy(){if(w==null||consent==null)return;boolean r=consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;w.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(r?"'inline-block'":"'none")+"})()",null);}
 private void ads(){if(ads||consent==null||!consent.canRequestAds())return;ads=true;MobileAds.initialize(this,s->{loadBanner();loadInter();loadReward();});}
 private void loadBanner(){if(!ads||banner==null||bannerLoading)return;bannerLoading=true;banner.setAdSize(getAdaptiveBannerSize());banner.loadAd(new AdRequest.Builder().build());}
 private void loadInter(){if(!ads||inter!=null||interLoading)return;interLoading=true;InterstitialAd.load(this,I,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd a){interLoading=false;inter=a;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){inter=null;loadInter();}@Override public void onAdFailedToShowFullScreenContent(AdError e){inter=null;loadInter();}});}@Override public void onAdFailedToLoad(LoadAdError e){interLoading=false;inter=null;handler.postDelayed(()->loadInter(),30000);}});}
 private void loadReward(){if(!ads||reward!=null||rewardLoading)return;rewardLoading=true;RewardedAd.load(this,R,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd a){rewardLoading=false;reward=a;}@Override public void onAdFailedToLoad(LoadAdError e){rewardLoading=false;reward=null;handler.postDelayed(()->loadReward(),30000);}});}
 private void showReward(){if(reward==null){Toast.makeText(this,"Rewarded sedang disediakan.",Toast.LENGTH_SHORT).show();loadReward();return;}RewardedAd a=reward;reward=null;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){loadReward();}@Override public void onAdFailedToShowFullScreenContent(AdError e){loadReward();}});a.show(this,x->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
 private void showInter(){if(inter==null||SystemClock.elapsedRealtime()-lastInter<300000)return;lastInter=SystemClock.elapsedRealtime();InterstitialAd a=inter;inter=null;a.show(this);}
 private void showPrivacy(){if(consent!=null&&consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)UserMessagingPlatform.showPrivacyOptionsForm(this,e->privacy());}
 @Override protected void onResume(){super.onResume();if(banner!=null)banner.resume();}
 @Override protected void onPause(){if(banner!=null)banner.pause();super.onPause();}
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(banner!=null){banner.destroy();banner=null;}if(w!=null){w.destroy();w=null;}super.onDestroy();}
 public class Bridge{@JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showReward());}@JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInter());}@JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacy());}}
}
