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

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
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
    private static final String APP_ID = "ca-app-pub-9940728659432865~9855790743";
    private static final String LIVE_BANNER = "ca-app-pub-9940728659432865/2663696435";
    private static final String LIVE_INTERSTITIAL = "ca-app-pub-9940728659432865/4188531092";
    private static final String LIVE_REWARDED = "ca-app-pub-9940728659432865/2875449427";
    private static final boolean USE_TEST_ADS = true;
    private static final String TEST_BANNER = "ca-app-pub-3940256099942544/9214589741";
    private static final String TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";
    private static final String TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917";

    private ConsentInformation consentInformation;
    private WebView webView;
    private AdView bannerView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private long lastInterstitialShown;
    private boolean adsStarted;

    private String bannerId(){return USE_TEST_ADS?TEST_BANNER:LIVE_BANNER;}
    private String interstitialId(){return USE_TEST_ADS?TEST_INTERSTITIAL:LIVE_INTERSTITIAL;}
    private String rewardedId(){return USE_TEST_ADS?TEST_REWARDED:LIVE_REWARDED;}

    @Override protected void onCreate(Bundle b){super.onCreate(b); buildUi(); setupConsentAndAds();}

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,247,251));
        webView=new WebView(this);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false);
        webView.setBackgroundColor(Color.rgb(245,247,251));
        webView.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view,String url){
                super.onPageFinished(view,url);
                patchCalculatorUi();
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AdBridge(),"Android");
        webView.loadUrl("file:///android_asset/index.html");
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1f));
        bannerView=new AdView(this); bannerView.setAdUnitId(bannerId()); bannerView.setAdSize(AdSize.BANNER); bannerView.setBackgroundColor(Color.WHITE);
        root.addView(bannerView,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
    }

    private void patchCalculatorUi(){
        if(webView==null)return;
        String js="javascript:(function(){try{"+
            "var s=document.getElementById('__ec_targeted_fix');"+
            "if(!s){s=document.createElement('style');s.id='__ec_targeted_fix';s.textContent="+
            "'html,body{overflow-x:hidden!important}body.basicMode{min-height:100dvh!important;overflow:hidden!important}body.basicMode #app{min-height:calc(100dvh - 1px)!important;height:calc(100dvh - 1px)!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}body.basicMode .keys{flex:1 1 auto!important;min-height:0!important;grid-template-rows:repeat(5,minmax(0,1fr))!important}body.basicMode .key{height:auto!important;min-height:0!important}';document.head.appendChild(s);} "+
            "document.body.classList.add('basicMode');"+
            "document.querySelectorAll('.merdeka,[class*=merdeka],[id*=merdeka]').forEach(function(e){e.remove()});"+
            "if(!window.__ecBasicWatcher){window.__ecBasicWatcher=true;var o=document.getElementById('toolOverlay');if(o)new MutationObserver(function(){if(!document.body.classList.contains('basicMode'))document.body.classList.add('basicMode');document.querySelectorAll('.merdeka,[class*=merdeka],[id*=merdeka]').forEach(function(e){e.remove()});}).observe(o,{attributes:true,attributeFilter:['class']});}"+
            "var hb=document.getElementById('historyBtn');"+
            "if(hb){hb.onclick=function(){window.__ecRenderHistory();var o=document.getElementById('historyOverlay');if(o)o.classList.add('show')}}"+
            "window.__ecRenderHistory=function(){var el=document.getElementById('historyList');if(!el)return;var d=Array.isArray(window.historyData)?window.historyData:(typeof historyItems!=='undefined'&&Array.isArray(historyItems)?historyItems:[]);if(!d.length){el.innerHTML='<div class=\\\"historyEmpty\\\">Belum ada pengiraan.</div>';return;}el.innerHTML='<div style=\\\"display:flex;justify-content:flex-end;margin-bottom:8px\\\"><button onclick=\\\"window.__ecClearHistory()\\\" style=\\\"border:1px solid #f0d7bb;background:#fff8f0;color:#b96500;border-radius:9px;padding:7px 10px;font-size:10px;font-weight:800\\\">Padam semua</button></div>'+d.map(function(h,i){var r=h.result||'';var t=h.title||'Basic';return '<div class=\\\"historyItem\\\"><b>'+String(r).replace(/[&<>]/g,function(m){return {'&':'&amp;','<':'&lt;','>':'&gt;'}[m]})+'</b><span>'+String(t)+'</span><div style=\\\"display:flex;justify-content:flex-end;gap:6px;margin-top:8px\\\"><button onclick=\\\"window.__ecUseHistory('+i+')\\\" style=\\\"margin:0\\\">Guna semula</button><button onclick=\\\"window.__ecDeleteHistory('+i+')\\\" style=\\\"margin:0;color:#c84b4b\\\">Padam</button></div></div>'}).join('')};"+
            "window.__ecDeleteHistory=function(i){var d=Array.isArray(window.historyData)?window.historyData:(typeof historyItems!=='undefined'&&Array.isArray(historyItems)?historyItems:[]);if(i<0||i>=d.length)return;d.splice(i,1);try{localStorage.setItem('easyCalculatorHistory',JSON.stringify(d))}catch(e){}window.__ecRenderHistory()};"+
            "window.__ecClearHistory=function(){var d=Array.isArray(window.historyData)?window.historyData:(typeof historyItems!=='undefined'&&Array.isArray(historyItems)?historyItems:[]);if(!d.length)return;d.length=0;try{localStorage.removeItem('easyCalculatorHistory')}catch(e){}window.__ecRenderHistory()};"+
            "window.__ecUseHistory=function(i){var d=Array.isArray(window.historyData)?window.historyData:(typeof historyItems!=='undefined'&&Array.isArray(historyItems)?historyItems:[]);if(!d[i])return;var v=String(d[i].result||'');if(typeof expr!=='undefined'){expr=v;if(typeof show==='function')show();}var o=document.getElementById('historyOverlay');if(o)o.classList.remove('show');};"+
            "}catch(e){}})();";
        webView.evaluateJavascript(js,null);
    }

    private void setupConsentAndAds(){
        consentInformation=UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters params=new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(this,params,()->{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->{updatePrivacyButton();startAdsIfAllowed();});
            updatePrivacyButton(); startAdsIfAllowed();
        },error->{updatePrivacyButton();startAdsIfAllowed();});
    }

    private void updatePrivacyButton(){if(webView==null||consentInformation==null)return; boolean req=consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED; webView.post(()->webView.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(req?"'inline-block'":"'none'")+";})();",null));}
    private void startAdsIfAllowed(){if(adsStarted||consentInformation==null||!consentInformation.canRequestAds())return;adsStarted=true;MobileAds.initialize(this,status->{loadBanner();loadInterstitial();loadRewarded();});}
    private void loadBanner(){if(bannerView!=null)bannerView.loadAd(new AdRequest.Builder().build());}
    private void loadInterstitial(){InterstitialAd.load(this,interstitialId(),new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd ad){interstitialAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){interstitialAd=null;loadInterstitial();}@Override public void onAdFailedToShowFullScreenContent(AdError e){interstitialAd=null;loadInterstitial();}});}@Override public void onAdFailedToLoad(LoadAdError e){interstitialAd=null;}});}
    private void loadRewarded(){RewardedAd.load(this,rewardedId(),new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd ad){rewardedAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){rewardedAd=null;loadRewarded();}@Override public void onAdFailedToShowFullScreenContent(AdError e){rewardedAd=null;loadRewarded();}});}@Override public void onAdFailedToLoad(LoadAdError e){rewardedAd=null;}});}
    private void showRewarded(){if(rewardedAd==null){Toast.makeText(this,"Rewarded sedang disediakan. Cuba lagi sebentar.",Toast.LENGTH_SHORT).show();loadRewarded();return;}RewardedAd ad=rewardedAd;rewardedAd=null;ad.show(this,item->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
    private void showInterstitial(){long now=SystemClock.elapsedRealtime();if(now-lastInterstitialShown<300000L||interstitialAd==null)return;lastInterstitialShown=now;InterstitialAd ad=interstitialAd;interstitialAd=null;ad.show(this);}
    private void showPrivacyOptions(){UserMessagingPlatform.showPrivacyOptionsForm(this,error->updatePrivacyButton());}
    public class AdBridge{@JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showRewarded());}@JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInterstitial());}@JavascriptInterface public boolean isPrivacyOptionsRequired(){return consentInformation!=null&&consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;}@JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacyOptions());}}
    @Override protected void onDestroy(){if(bannerView!=null)bannerView.destroy();if(webView!=null)webView.destroy();super.onDestroy();}
}
