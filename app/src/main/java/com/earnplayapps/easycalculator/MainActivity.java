package com.earnplayapps.easycalculator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends Activity {
    private static final String LIVE_BANNER="ca-app-pub-9940728659432865/2663696435";
    private static final String LIVE_INTERSTITIAL="ca-app-pub-9940728659432865/4188531092";
    private static final String LIVE_REWARDED="ca-app-pub-9940728659432865/2875449427";
    private static final boolean USE_TEST_ADS=true;
    private static final String TEST_BANNER="ca-app-pub-3940256099942544/9214589741";
    private static final String TEST_INTERSTITIAL="ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED="ca-app-pub-3940256099942544/5224354917";
    private WebView webView; private AdView bannerView; private InterstitialAd interstitialAd; private RewardedAd rewardedAd;
    private ConsentInformation consentInformation; private long lastInterstitialShown=0; private boolean adsStarted=false;
    private String bannerId(){return USE_TEST_ADS?TEST_BANNER:LIVE_BANNER;}
    private String interstitialId(){return USE_TEST_ADS?TEST_INTERSTITIAL:LIVE_INTERSTITIAL;}
    private String rewardedId(){return USE_TEST_ADS?TEST_REWARDED:LIVE_REWARDED;}
    @Override protected void onCreate(Bundle state){super.onCreate(state);buildUi();setupConsentAndAds();}
    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(245,247,251));
        webView=new WebView(this); WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(false); s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false); webView.setBackgroundColor(Color.rgb(245,247,251));
        webView.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);patchCalculatorUi();}}); webView.setWebChromeClient(new WebChromeClient()); webView.addJavascriptInterface(new AdBridge(),"Android"); webView.loadUrl("file:///android_asset/index.html");
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1f)); bannerView=new AdView(this); bannerView.setAdUnitId(bannerId()); bannerView.setAdSize(AdSize.BANNER); bannerView.setBackgroundColor(Color.WHITE); root.addView(bannerView,new LinearLayout.LayoutParams(-1,-2)); setContentView(root);
    }
    private void patchCalculatorUi(){
        if(webView==null)return;
        String js="javascript:(function(){try{"+
        "var s=document.createElement('style');s.id='easyFinalStyle';s.textContent="+
        "'html,body{overflow-x:hidden!important}.app{max-width:460px!important}.basicMode .keys{grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:repeat(5,minmax(0,1fr))!important;flex:1!important;min-height:0!important}.basicMode .key{height:auto!important;min-height:0!important}.basicMode .key.zero{grid-column:span 2!important}.easyLogo{width:38px;height:38px;border:1px solid #e5e9f1;border-radius:12px;background:linear-gradient(145deg,#fff,#edf2f7);display:grid;place-items:center;box-shadow:0 7px 18px rgba(30,42,58,.10),inset 0 1px #fff}.easyLogo svg{width:22px;height:22px}.headActions{display:flex!important;align-items:center!important;justify-content:flex-end!important;gap:6px!important}.headActions .other{padding:7px 9px!important;font-size:10px!important}.headActions .hamb{width:34px!important;height:34px!important;border-radius:10px!important}.headActions .hamb i{width:16px!important}.cat .ico,.toolico{border:1px solid rgba(255,255,255,.95)!important;background:linear-gradient(145deg,#fff,#f4f7fb)!important;box-shadow:inset 0 1px #fff,0 7px 16px rgba(30,42,58,.10)!important}.cat .ico svg,.toolico svg{width:20px;height:20px}.menuGrid{display:grid;gap:9px}.menuItem{width:100%;display:grid;grid-template-columns:38px 1fr 16px;gap:10px;align-items:center;border:1px solid #e5e9ef;background:linear-gradient(145deg,#fff,#f7f9fb);border-radius:14px;padding:10px;text-align:left;box-shadow:0 7px 18px rgba(30,42,58,.06)}.menuIcon{width:38px;height:38px;border-radius:11px;display:grid;place-items:center;background:linear-gradient(145deg,#fff4e9,#fff);color:#d8750a;box-shadow:inset 0 1px #fff,0 6px 14px rgba(30,42,58,.08)}.menuItem b{font-size:13px}.menuItem span{display:block;color:#6f7a8e;font-size:10px;margin-top:2px}.menuArrow{color:#a1a9b8;font-size:18px}.historyClear{border:1px solid #f0d7bb;background:#fff8f0;color:#b96500;border-radius:9px;padding:7px 10px;font-size:10px;font-weight:800}.historyBtns{display:flex;gap:6px;justify-content:flex-end}';document.head.appendChild(s);"+
        "document.body.classList.add('basicMode');"+
        "document.querySelectorAll('.merdeka,[class*=merdeka],[id*=merdeka]').forEach(function(e){e.remove()});"+
        "var keys=document.querySelector('.keys');if(keys){keys.querySelectorAll('.key').forEach(function(k){if(k.textContent.trim()==='(')k.remove()});var z=Array.from(keys.querySelectorAll('.key')).find(function(k){return k.textContent.trim()==='0'});if(z)z.classList.add('zero');}"+
        "var top=document.querySelector('.top'),brand=document.querySelector('.brand'),ha=document.querySelector('.headActions'),hamb=document.getElementById('hamb'),other=document.getElementById('other');"+
        "if(top&&brand&&ha&&hamb&&other){var logo=document.getElementById('easyLogo');if(!logo){logo=document.createElement('button');logo.id='easyLogo';logo.className='easyLogo';logo.type='button';logo.setAttribute('aria-label','Easy App');logo.innerHTML='<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#d8750a\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><rect x=\"4\" y=\"5\" width=\"16\" height=\"14\" rx=\"3\"/><path d=\"M8 9h8M8 12h5M8 15h7\"/></svg>';top.insertBefore(logo,brand)}if(hamb.parentElement!==ha)ha.appendChild(hamb);other.textContent='Calculator Lain';logo.onclick=function(){if(typeof openCats==='function')openCats()};}"+
        "var cats=document.getElementById('catGrid');if(cats){var C=[['Basic','+','blue','Pengiraan asas'],['Money','RM','green','Duit & harga'],['Converter','↔','cyan','Tukar unit'],['Date & Time','◷','pink','Tarikh & umur'],['Health','♥','pink','Kecergasan'],['Home & Daily','⌂','yellow','Rumah & harian'],['Study','A+','violet','Markah & belajar'],['Work & Business','▣','orange','Kerja & bisnes']];cats.innerHTML=C.map(function(c,i){return '<button class=\"cat\" data-c=\"'+i+'\"><div class=\"ico '+c[2]+'\"><b>'+c[1]+'</b></div><div><b>'+c[0]+'</b><span>'+c[3]+'</span></div><div class=\"chev\">›</div></button>'}).join('');cats.querySelectorAll('.cat').forEach(function(b){b.onclick=function(){var c=C[+b.dataset.c];var list=(typeof tools!=='undefined'&&tools[c[0]])?tools[c[0]]:[];closeCats();document.getElementById('toolTitle').textContent=c[0];document.getElementById('toolDesc').textContent='Pilih calculator yang anda perlukan. Setiap calculator akan terangkan apa yang perlu diisi.';document.getElementById('toolBody').innerHTML='<div class=\"toolgrid\">'+list.map(function(x){return '<button class=\"tool\" onclick=\"openTool(\\\''+x[1]+'\\\',\\\''+x[0]+'\\\')\"><div class=\"toolico\">'+c[1]+'</div><div><b>'+x[0]+'</b><span>'+x[2]+'</span></div><div class=\"toolchev\">›</div></button>'}).join('')+'</div>';document.getElementById('toolOverlay').classList.add('show')}})}}"+
        "var menu=document.getElementById('easyMenu');if(!menu){menu=document.createElement('div');menu.id='easyMenu';menu.className='overlay';menu.innerHTML='<div class=\"modal\"><div class=\"mhead\"><div><h2>Menu</h2><p>Fungsi tambahan Easy Calculator</p></div><button class=\"close\" id=\"easyMenuClose\">×</button></div><div class=\"menuGrid\"><button class=\"menuItem\" id=\"easySettings\"><div class=\"menuIcon\">⚙</div><div><b>Settings</b><span>Pilihan aplikasi</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"easyPrivacy\"><div class=\"menuIcon\">✓</div><div><b>Privacy</b><span>Pilihan privasi iklan</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"easyAbout\"><div class=\"menuIcon\">i</div><div><b>Tentang Easy Calculator</b><span>Calculator untuk kegunaan harian</span></div><div class=\"menuArrow\">›</div></button></div></div>';document.body.appendChild(menu);document.getElementById('easyMenuClose').onclick=function(){menu.classList.remove('show')};document.getElementById('easySettings').onclick=function(){alert('Tetapan Easy Calculator akan tersedia di sini.')};document.getElementById('easyPrivacy').onclick=function(){menu.classList.remove('show');if(window.Android&&Android.showPrivacyOptions)Android.showPrivacyOptions()};document.getElementById('easyAbout').onclick=function(){alert('Easy Calculator — calculator mudah untuk kegunaan harian.')}}"+
        "hamb.onclick=function(){menu.classList.add('show')};"+
        "}catch(e){console.log('Easy Calculator patch error',e);}})();";
        webView.evaluateJavascript(js,null);
    }
    private void setupConsentAndAds(){
        consentInformation=UserMessagingPlatform.getConsentInformation(this); ConsentRequestParameters params=new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(this,params,()->{UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->{updatePrivacyButton();startAdsIfAllowed();});updatePrivacyButton();startAdsIfAllowed();},error->{updatePrivacyButton();startAdsIfAllowed();});
    }
    private void updatePrivacyButton(){if(webView==null||consentInformation==null)return;boolean req=consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;webView.post(()->webView.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(req?"'inline-block'":"'none'")+";})();",null));}
    private void startAdsIfAllowed(){if(adsStarted||consentInformation==null||!consentInformation.canRequestAds())return;adsStarted=true;MobileAds.initialize(this,status->{loadBanner();loadInterstitial();loadRewarded();});}
    private void loadBanner(){if(bannerView!=null)bannerView.loadAd(new AdRequest.Builder().build());}
    private void loadInterstitial(){InterstitialAd.load(this,interstitialId(),new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd ad){interstitialAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){interstitialAd=null;loadInterstitial();}@Override public void onAdFailedToShowFullScreenContent(AdError e){interstitialAd=null;loadInterstitial();}});}@Override public void onAdFailedToLoad(LoadAdError e){interstitialAd=null;}});}
    private void loadRewarded(){RewardedAd.load(this,rewardedId(),new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd ad){rewardedAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){rewardedAd=null;loadRewarded();}@Override public void onAdFailedToShowFullScreenContent(AdError e){rewardedAd=null;loadRewarded();}});}@Override public void onAdFailedToLoad(LoadAdError e){rewardedAd=null;}});}
    private void showRewarded(){if(rewardedAd==null){Toast.makeText(this,"Rewarded sedang disediakan. Cuba lagi sebentar.",Toast.LENGTH_SHORT).show();loadRewarded();return;}RewardedAd ad=rewardedAd;rewardedAd=null;ad.show(this,item->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
    private void showInterstitial(){long now=SystemClock.elapsedRealtime();if(now-lastInterstitialShown<300000L||interstitialAd==null)return;lastInterstitialShown=now;InterstitialAd ad=interstitialAd;interstitialAd=null;ad.show(this);}
    private void showPrivacyOptions(){if(consentInformation==null)return;if(consentInformation.getPrivacyOptionsRequirementStatus()!=ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED){Toast.makeText(this,"Pilihan privasi tidak diperlukan pada masa ini.",Toast.LENGTH_SHORT).show();return;}try{UserMessagingPlatform.showPrivacyOptionsForm(this,error->{if(error!=null)Toast.makeText(this,"Privacy Options tidak dapat dibuka sekarang.",Toast.LENGTH_SHORT).show();updatePrivacyButton();});}catch(Exception e){Toast.makeText(this,"Privacy Options tidak dapat dibuka sekarang.",Toast.LENGTH_SHORT).show();}}
    public class AdBridge{ @JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showRewarded());} @JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInterstitial());} @JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacyOptions());} }
}
