package com.earnplayapps.easycalculator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
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
 private WebView w; private AdView banner; private InterstitialAd inter; private RewardedAd reward; private ConsentInformation consent; private long lastInter; private boolean ads;
 private final Handler handler=new Handler(Looper.getMainLooper());
 @Override protected void onCreate(Bundle b){super.onCreate(b);ui();consent();}
 private void ui(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(245,247,251));
  w=new WebView(this);WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(false);s.setLoadWithOverviewMode(false);s.setUseWideViewPort(false);
  w.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){fixMobileCss();w.setVisibility(View.VISIBLE);}});w.setWebChromeClient(new WebChromeClient());w.addJavascriptInterface(new Bridge(),"Android");
  root.addView(w,new LinearLayout.LayoutParams(-1,0,1));
  banner=new AdView(this);banner.setAdUnitId(B);banner.setAdListener(new AdListener(){@Override public void onAdLoaded(){banner.setVisibility(View.VISIBLE);}@Override public void onAdFailedToLoad(LoadAdError e){handler.postDelayed(()->loadBanner(),30000);}});banner.setAdSize(getAdaptiveBannerSize());banner.setVisibility(View.GONE);root.addView(banner,new LinearLayout.LayoutParams(-1,-2));
  setContentView(root);w.loadUrl("file:///android_asset/index.html");
 }
 private AdSize getAdaptiveBannerSize(){int widthPx=getResources().getDisplayMetrics().widthPixels;float density=getResources().getDisplayMetrics().density;int widthDp=Math.max(320,Math.round(widthPx/density));return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,widthDp);}
 private void fixMobileCss(){String js="javascript:(function(){try{if(document.getElementById('__ec_mobile'))return;var s=document.createElement('style');s.id='__ec_mobile';s.textContent='html,body{width:100%!important;max-width:100%!important;overflow-x:hidden!important}body{margin:0!important}.app{width:100%!important;max-width:460px!important;margin:0 auto!important;padding:14px 12px 10px!important}.top{grid-template-columns:36px minmax(0,1fr) auto!important;gap:7px!important}.brand{min-width:0!important}.brand h1{font-size:18px!important;white-space:nowrap!important}.brand small{font-size:10px!important;white-space:nowrap!important}.headActions{min-width:0!important}.historyBtn{width:34px!important;flex:none!important}.other{padding:7px 9px!important;font-size:10px!important}.display{width:100%!important}.quick{width:100%!important;gap:5px!important}.quick button{min-width:0!important;font-size:10px!important;padding:8px 2px!important}.basicMode .keys{width:100%!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;gap:7px!important}.basicMode .key{width:100%!important;height:clamp(48px,9.5vh,70px)!important;min-width:0!important}.basicMode .result{font-size:clamp(34px,9vw,52px)!important}.catgrid{grid-template-columns:1fr 1fr!important}.modal{width:min(100%,430px)!important;max-height:88vh!important}.two{grid-template-columns:1fr 1fr!important}@media(max-width:360px){.top{grid-template-columns:34px minmax(0,1fr) auto!important}.other{font-size:9px!important;padding:6px 7px!important}.brand h1{font-size:16px!important}.historyBtn{width:31px!important}.basicMode .key{height:46px!important}.basicMode .result{font-size:32px!important}}';document.head.appendChild(s);}catch(e){console.log('Easy Calculator mobile CSS',e)}})();";w.evaluateJavascript(js,null);}
 private void consent(){consent=UserMessagingPlatform.getConsentInformation(this);consent.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),()->UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,e->{privacy();ads();}),e->{privacy();ads();});}
 private void privacy(){if(w==null||consent==null)return;boolean r=consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;w.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(r?"'inline-block'":"'none")+"})()",null);}
 private void ads(){if(ads||consent==null||!consent.canRequestAds())return;ads=true;MobileAds.initialize(this,s->{loadBanner();loadInter();loadReward();});}
 private void loadBanner(){if(!ads||banner==null)return;banner.setAdSize(getAdaptiveBannerSize());banner.loadAd(new AdRequest.Builder().build());}
 private void loadInter(){if(!ads)return;InterstitialAd.load(this,I,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd a){inter=a;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){inter=null;loadInter();}@Override public void onAdFailedToShowFullScreenContent(AdError e){inter=null;loadInter();}});}@Override public void onAdFailedToLoad(LoadAdError e){inter=null;handler.postDelayed(()->loadInter(),30000);}});}
 private void loadReward(){if(!ads)return;RewardedAd.load(this,R,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd a){reward=a;}@Override public void onAdFailedToLoad(LoadAdError e){reward=null;handler.postDelayed(()->loadReward(),30000);}});}
 private void showReward(){if(reward==null){Toast.makeText(this,"Rewarded sedang disediakan.",Toast.LENGTH_SHORT).show();loadReward();return;}RewardedAd a=reward;reward=null;a.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){loadReward();}@Override public void onAdFailedToShowFullScreenContent(AdError e){loadReward();}});a.show(this,x->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
 private void showInter(){if(inter==null||SystemClock.elapsedRealtime()-lastInter<300000)return;lastInter=SystemClock.elapsedRealtime();InterstitialAd a=inter;inter=null;a.show(this);}
 private void showPrivacy(){if(consent!=null&&consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)UserMessagingPlatform.showPrivacyOptionsForm(this,e->privacy());}
 public class Bridge{@JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showReward());}@JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInter());}@JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacy());}}
}