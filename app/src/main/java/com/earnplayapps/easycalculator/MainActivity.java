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

public class MainActivity extends Activity {
 private static final String B=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/9214589741":"ca-app-pub-9940728659432865/2663696435";
 private static final String I=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/1033173712":"ca-app-pub-9940728659432865/4188531092";
 private static final String R=BuildConfig.DEBUG?"ca-app-pub-3940256099942544/5224354917":"ca-app-pub-9940728659432865/2875449427";
 private WebView w; private AdView banner; private InterstitialAd inter; private RewardedAd reward; private ConsentInformation consent;
 private long lastInter; private boolean ads; private boolean bannerLoading; private boolean interLoading; private boolean rewardLoading;
 private final Handler handler=new Handler(Looper.getMainLooper());

 @Override protected void onCreate(Bundle b){super.onCreate(b);ui();consent();}

 private void ui(){
  LinearLayout root=new LinearLayout(this);
  root.setOrientation(LinearLayout.VERTICAL);
  root.setBackgroundColor(Color.rgb(245,247,251));
  root.setOnApplyWindowInsetsListener((v,insets)->{
   int top,bottom;
   if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}
   else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}
   v.setPadding(0,top,0,bottom);
   return insets;
  });

  w=new WebView(this);
  WebSettings s=w.getSettings();
  s.setJavaScriptEnabled(true);
  s.setDomStorageEnabled(true);
  s.setAllowFileAccess(true);
  s.setAllowContentAccess(false);
  s.setLoadWithOverviewMode(false);
  s.setUseWideViewPort(false);
  s.setBuiltInZoomControls(false);
  s.setDisplayZoomControls(false);
  s.setSupportZoom(false);
  w.setVerticalScrollBarEnabled(false);
  w.setHorizontalScrollBarEnabled(false);
  w.setWebViewClient(new WebViewClient(){
   @Override public void onPageFinished(WebView v,String u){applyAdaptiveLayout();w.setVisibility(View.VISIBLE);}
  });
  w.setWebChromeClient(new WebChromeClient());
  w.addJavascriptInterface(new Bridge(),"Android");
  root.addView(w,new LinearLayout.LayoutParams(-1,0,1));

  banner=new AdView(this);
  banner.setAdUnitId(B);
  banner.setAdListener(new AdListener(){
   @Override public void onAdLoaded(){bannerLoading=false;banner.setVisibility(View.VISIBLE);}
   @Override public void onAdFailedToLoad(LoadAdError e){bannerLoading=false;banner.setVisibility(View.GONE);handler.postDelayed(()->loadBanner(),30000);}
  });
  banner.setAdSize(getAdaptiveBannerSize());
  banner.setVisibility(View.GONE);
  root.addView(banner,new LinearLayout.LayoutParams(-1,-2));

  setContentView(root);
  root.requestApplyInsets();
  w.loadUrl("file:///android_asset/index.html");
 }

 private AdSize getAdaptiveBannerSize(){
  int widthPx=getResources().getDisplayMetrics().widthPixels;
  float density=getResources().getDisplayMetrics().density;
  int widthDp=Math.max(320,Math.round(widthPx/density));
  return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,widthDp);
 }

 private void applyAdaptiveLayout(){
  if(w==null)return;
  String js="(function(){"+
   "var app=document.getElementById('app');var top=document.querySelector('.top');if(!app||!top)return;"+
   "var brand=top.querySelector('.brand'),actions=top.querySelector('.headActions'),hamb=top.querySelector('.hamb');"+
   "if(brand&&actions&&hamb){top.appendChild(brand);top.appendChild(actions);top.appendChild(hamb);}"+
   "if(brand&&!brand.querySelector('.appLogo')){var logo=document.createElement('button');logo.className='appLogo';logo.id='appLogo';logo.type='button';logo.setAttribute('aria-label','Easy Calculator');logo.innerHTML='<span>EA</span>';brand.insertBefore(logo,brand.firstChild);}"+
   "var s=document.getElementById('ecAdaptive');if(!s){s=document.createElement('style');s.id='ecAdaptive';document.head.appendChild(s);}"
   +"s.textContent=\""+
   "*{box-sizing:border-box}html,body{width:100%;height:100%;overflow:hidden!important;}"+
   "#app{width:100%;max-width:460px;height:100%;min-height:0;margin:0 auto;padding:12px 12px 6px;display:flex;flex-direction:column;overflow:hidden;}"+
   ".top{display:grid!important;grid-template-columns:minmax(0,1fr) auto 38px!important;align-items:center!important;gap:6px!important;flex:0 0 auto!important;width:100%;margin-bottom:8px;}"+
   ".brand{grid-column:1!important;grid-row:1!important;min-width:0!important;display:flex!important;align-items:center!important;gap:8px!important;overflow:hidden!important;}"+
   ".appLogo{width:34px!important;height:34px!important;min-width:34px!important;border:1px solid #e5e9ef!important;border-radius:10px!important;background:linear-gradient(145deg,#fff,#eef2f7)!important;color:#d8750a!important;display:grid!important;place-items:center!important;padding:0!important;font-weight:900!important;box-shadow:0 5px 14px rgba(30,42,58,.08)!important;}"+
   ".appLogo span{font-size:10px!important;font-weight:950!important;}"+
   ".brand h1{min-width:0!important;font-size:clamp(16px,4.8vw,19px)!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;}"+
   ".brand small{white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;}"+
   ".headActions{grid-column:2!important;grid-row:1!important;min-width:0!important;display:flex!important;align-items:center!important;justify-content:flex-end!important;gap:4px!important;overflow:hidden!important;}"+
   ".historyBtn{flex:0 0 auto!important;}"+
   ".other{max-width:min(30vw,110px)!important;overflow:hidden!important;text-overflow:ellipsis!important;}"+
   ".hamb{grid-column:3!important;grid-row:1!important;justify-self:end!important;width:38px!important;height:38px!important;min-width:38px!important;flex:0 0 38px!important;}"+
   ".display{flex:0 0 auto!important;min-width:0!important;margin-bottom:7px!important;}"+
   ".quick{display:grid!important;grid-template-columns:repeat(5,minmax(0,1fr))!important;flex:0 0 auto!important;gap:5px!important;margin-bottom:7px!important;}"+
   ".quick button{min-width:0!important;padding:7px 2px!important;font-size:10px!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;}"+
   "#app .keys{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:repeat(5,minmax(0,1fr))!important;grid-auto-rows:minmax(0,1fr)!important;gap:clamp(5px,1.7vw,8px)!important;flex:1 1 auto!important;min-height:0!important;width:100%!important;align-content:stretch!important;}"+
   "#app .key{width:100%!important;height:auto!important;min-width:0!important;min-height:0!important;font-size:clamp(16px,4.8vw,20px)!important;}"+
   ".note{flex:0 0 auto!important;min-height:20px!important;padding:4px 0 0!important;}"+
   ".overlay{z-index:100!important;}"+
   ".modal{width:min(100%,430px)!important;max-height:calc(100vh - 32px)!important;}"+
   "@media(max-width:360px){#app{padding-left:8px;padding-right:8px}.top{gap:4px!important}.brand{gap:5px!important}.appLogo{width:31px!important;height:31px!important;min-width:31px!important}.hamb{width:34px!important;height:34px!important;min-width:34px!important}.headActions{gap:3px!important}.historyBtn{width:31px!important;height:31px!important}.other{max-width:82px!important;padding-left:7px!important;padding-right:7px!important}.result{font-size:clamp(32px,9vw,44px)!important}.quick{gap:3px!important}.quick button{font-size:9px!important;padding:6px 1px!important}}"+
   "\";"+
   "document.documentElement.style.overflow='hidden';document.body.style.overflow='hidden';"+
   "var p=document.getElementById('ecPrivacyLink');if(p&&!p.dataset.bound){p.dataset.bound='1';p.onclick=function(){if(window.Android&&Android.showPrivacyOptions)Android.showPrivacyOptions();}}"+
   "if(typeof window.runTool==='function'&&!window.__ecRunToolGuard){var originalRunTool=window.runTool;window.runTool=function(t){try{return originalRunTool(t);}catch(e){var out=document.getElementById('out'),msg=document.getElementById('msg');if(out)out.textContent='Input tidak sah';if(msg)msg.textContent='Sila semak semua nilai yang dimasukkan.';return null;}};window.__ecRunToolGuard=true;}"+
   "if(typeof window.filterCats==='function'&&!window.__ecFilterGuard){window.__ecFilterGuard=true;}"+
   "})();";
  w.evaluateJavascript(js,null);
 }

 private void consent(){
  consent=UserMessagingPlatform.getConsentInformation(this);
  consent.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),
   ()->UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,e->{privacy();ads();}),
   e->{privacy();ads();}
  );
 }

 private void privacy(){
  if(w==null||consent==null)return;
  boolean r=consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
  w.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(r?"'inline-block'":"'none")+"})()",null);
 }

 private void ads(){
  if(ads||consent==null||!consent.canRequestAds())return;
  ads=true;
  MobileAds.initialize(this,s->{loadBanner();loadInter();loadReward();});
 }

 private void loadBanner(){
  if(!ads||banner==null||bannerLoading)return;
  bannerLoading=true;
  banner.setAdSize(getAdaptiveBannerSize());
  banner.loadAd(new AdRequest.Builder().build());
 }

 private void loadInter(){
  if(!ads||inter!=null||interLoading)return;
  interLoading=true;
  InterstitialAd.load(this,I,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){
   @Override public void onAdLoaded(InterstitialAd a){
    interLoading=false;inter=a;
    a.setFullScreenContentCallback(new FullScreenContentCallback(){
     @Override public void onAdDismissedFullScreenContent(){inter=null;loadInter();}
     @Override public void onAdFailedToShowFullScreenContent(AdError e){inter=null;loadInter();}
    });
   }
   @Override public void onAdFailedToLoad(LoadAdError e){interLoading=false;inter=null;handler.postDelayed(()->loadInter(),30000);}
  });
 }

 private void loadReward(){
  if(!ads||reward!=null||rewardLoading)return;
  rewardLoading=true;
  RewardedAd.load(this,R,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){
   @Override public void onAdLoaded(RewardedAd a){rewardLoading=false;reward=a;}
   @Override public void onAdFailedToLoad(LoadAdError e){rewardLoading=false;reward=null;handler.postDelayed(()->loadReward(),30000);}
  });
 }

 private void showReward(){
  if(reward==null){Toast.makeText(this,"Rewarded sedang disediakan.",Toast.LENGTH_SHORT).show();loadReward();return;}
  RewardedAd a=reward;reward=null;
  a.setFullScreenContentCallback(new FullScreenContentCallback(){
   @Override public void onAdDismissedFullScreenContent(){loadReward();}
   @Override public void onAdFailedToShowFullScreenContent(AdError e){loadReward();}
  });
  a.show(this,x->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());
 }

 private void showInter(){
  if(inter==null||SystemClock.elapsedRealtime()-lastInter<300000)return;
  lastInter=SystemClock.elapsedRealtime();
  InterstitialAd a=inter;inter=null;a.show(this);
 }

 private void showPrivacy(){
  if(consent!=null&&consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)
   UserMessagingPlatform.showPrivacyOptionsForm(this,e->privacy());
 }

 @Override protected void onResume(){super.onResume();if(banner!=null)banner.resume();}
 @Override protected void onPause(){if(banner!=null)banner.pause();super.onPause();}
 @Override protected void onDestroy(){
  handler.removeCallbacksAndMessages(null);
  if(banner!=null){banner.destroy();banner=null;}
  if(w!=null){w.destroy();w=null;}
  super.onDestroy();
 }

 public class Bridge{
  @JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showReward());}
  @JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInter());}
  @JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacy());}
 }
}
