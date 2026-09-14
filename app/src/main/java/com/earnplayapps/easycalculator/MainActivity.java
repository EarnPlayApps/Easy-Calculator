package com.earnplayapps.easycalculator;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
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

public class MainActivity extends Activity {
 private static final String B=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/9214589741":"ca-app-pub-9940728659432865/2663696435";
 private static final String I=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/1033173712":"ca-app-pub-9940728659432865/4188531092";
 private static final String R=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/5224354917":"ca-app-pub-9940728659432865/2875449427";
 private WebView w; private AdView banner; private InterstitialAd inter; private RewardedAd reward; private ConsentInformation consent;
 private long lastInter; private boolean ads; private boolean bannerLoading; private boolean interLoading; private boolean rewardLoading;
 private final Handler handler=new Handler(Looper.getMainLooper());
 @Override protected void onCreate(Bundle b){super.onCreate(b);ui();handler.postDelayed(()->{if(!isFinishing()&&!isDestroyed())safeConsent();},800);}
 private void ui(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setBackgroundColor(Color.rgb(245,247,251));
  root.setOnApplyWindowInsetsListener((v,insets)->{int top,bottom;if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}v.setPadding(0,top,0,bottom);return insets;});
  w=new WebView(this);WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(false);s.setLoadWithOverviewMode(false);s.setUseWideViewPort(false);s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);s.setSupportZoom(false);w.setVerticalScrollBarEnabled(false);w.setHorizontalScrollBarEnabled(false);
  w.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){w.setVisibility(View.VISIBLE);}});w.setWebChromeClient(new WebChromeClient());w.addJavascriptInterface(new Bridge(),"Android");root.addView(w,new LinearLayout.LayoutParams(-1,0,1));
  banner=new AdView(this);banner.setAdUnitId(B);banner.setAdListener(new AdListener(){@Override public void onAdLoaded(){bannerLoading=false;banner.setVisibility(View.VISIBLE);}@Override public void onAdFailedToLoad(LoadAdError e){bannerLoading=false;banner.setVisibility(View.GONE);handler.postDelayed(()->loadBanner(),10000);}});banner.setAdSize(getAdaptiveBannerSize());banner.setVisibility(View.GONE);root.addView(banner,new LinearLayout.LayoutParams(-1,-2));
  setContentView(root);root.requestApplyInsets();w.loadUrl("file:///android_asset/index.html");
 }
 private AdSize getAdaptiveBannerSize(){try{DisplayMetrics m=getResources().getDisplayMetrics();int widthDp=Math.max(1,Math.round(m.widthPixels/m.density));return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,widthDp);}catch(Exception e){return AdSize.BANNER;}}
 private void safeConsent(){try{consent();}catch(Exception ignored){if(consent!=null&&consent.canRequestAds())ads();}}
 private void consent(){consent=UserMessagingPlatform.getConsentInformation(this);consent.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),()->{try{UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,e->{privacy();adsIfAllowed();});}catch(Exception e){privacy();adsIfAllowed();}},e->{privacy();adsIfAllowed();});}
 private void adsIfAllowed(){if(consent!=null&&consent.canRequestAds())ads();}
 private void privacy(){if(w==null||consent==null)return;try{boolean r=consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;w.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(r?"'inline-block'":"'none")+"})()",null);}catch(Exception ignored){}}
 private void ads(){if(ads||consent==null||!consent.canRequestAds()||isFinishing()||isDestroyed())return;ads=true;try{MobileAds.initialize(this,s->{if(isFinishing()||isDestroyed())return;loadBanner();loadInter();loadReward();});}catch(Exception ignored){ads=false;}}
 private void loadBanner(){if(!ads||banner==null||bannerLoading||isFinishing()||isDestroyed())return;try{bannerLoading=true;banner.setVisibility(View.GONE);banner.setAdSize(getAdaptiveBannerSize());banner.loadAd(new AdRequest.Builder().build());}catch(Exception e){bannerLoading=false;banner.setVisibility(View.GONE);handler.postDelayed(()->loadBanner(),10000);}}
 private void loadInter(){if(!ads||inter!=null||interLoading||isFinishing()||isDestroyed())return;interLoading=true;try{InterstitialAd.load(this,I,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd a){interLoading=false;inter=a;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){inter=null;loadInter();}@Override public void onAdFailedToShowFullScreenContent(AdError e){inter=null;loadInter();}});}@Override public void onAdFailedToLoad(LoadAdError e){interLoading=false;inter=null;handler.postDelayed(()->loadInter(),30000);}});}catch(Exception e){interLoading=false;inter=null;}}
 private void loadReward(){if(!ads||reward!=null||rewardLoading||isFinishing()||isDestroyed())return;rewardLoading=true;try{RewardedAd.load(this,R,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd a){rewardLoading=false;reward=a;}@Override public void onAdFailedToLoad(LoadAdError e){rewardLoading=false;reward=null;handler.postDelayed(()->loadReward(),30000);}});}catch(Exception e){rewardLoading=false;reward=null;}}
 private void showReward(){if(reward==null){Toast.makeText(this,"Rewarded sedang disediakan.",Toast.LENGTH_SHORT).show();loadReward();return;}RewardedAd a=reward;reward=null;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){loadReward();}@Override public void onAdFailedToShowFullScreenContent(AdError e){loadReward();}});try{a.show(this,x->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}catch(Exception e){loadReward();}}
 private void showInter(){if(inter==null||SystemClock.elapsedRealtime()-lastInter<300000)return;lastInter=SystemClock.elapsedRealtime();InterstitialAd a=inter;inter=null;try{a.show(this);}catch(Exception e){loadInter();}}
 private void showPrivacy(){if(consent!=null&&consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)try{UserMessagingPlatform.showPrivacyOptionsForm(this,e->{privacy();adsIfAllowed();});}catch(Exception ignored){}}
 @Override protected void onResume(){super.onResume();if(banner!=null)try{banner.resume();}catch(Exception ignored){}}
 @Override protected void onPause(){if(banner!=null)try{banner.pause();}catch(Exception ignored){}super.onPause();}
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(banner!=null){try{banner.destroy();}catch(Exception ignored){}banner=null;}if(w!=null){try{w.destroy();}catch(Exception ignored){}w=null;}super.onDestroy();}
 public class Bridge{
  @JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showReward());}
  @JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInter());}
  @JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacy());}
  @JavascriptInterface public boolean isPrivacyOptionsRequired(){return consent!=null&&consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;}
 }
}
