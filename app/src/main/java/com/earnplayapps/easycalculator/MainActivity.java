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
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(245,247,251));
        webView=new WebView(this); WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(false); s.setBuiltInZoomControls(false); s.setDisplayZoomControls(false);
        webView.setBackgroundColor(Color.rgb(245,247,251)); webView.setWebViewClient(new WebViewClient()); webView.setWebChromeClient(new WebChromeClient()); webView.addJavascriptInterface(new AdBridge(),"Android"); webView.loadUrl("file:///android_asset/index.html");
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1f));
        bannerView=new AdView(this); bannerView.setAdUnitId(bannerId()); bannerView.setAdSize(AdSize.BANNER); bannerView.setBackgroundColor(Color.WHITE); root.addView(bannerView,new LinearLayout.LayoutParams(-1,-2)); setContentView(root);
    }

    private void setupConsentAndAds(){
        consentInformation=UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters params=new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(this,params,()->{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->{ updatePrivacyButton(); startAdsIfAllowed(); });
            updatePrivacyButton(); startAdsIfAllowed();
        },error->{updatePrivacyButton(); startAdsIfAllowed();});
    }

    private void updatePrivacyButton(){if(webView==null||consentInformation==null)return; boolean req=consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED; webView.post(()->webView.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(req?"'inline-block'":"'none'")+";})();",null));}
    private void startAdsIfAllowed(){if(adsStarted||consentInformation==null||!consentInformation.canRequestAds())return; adsStarted=true; MobileAds.initialize(this,status->{loadBanner();loadInterstitial();loadRewarded();});}
    private void loadBanner(){if(bannerView!=null)bannerView.loadAd(new AdRequest.Builder().build());}
    private void loadInterstitial(){InterstitialAd.load(this,interstitialId(),new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd ad){interstitialAd=ad; ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){interstitialAd=null;loadInterstitial();}@Override public void onAdFailedToShowFullScreenContent(AdError e){interstitialAd=null;loadInterstitial();}});}@Override public void onAdFailedToLoad(LoadAdError e){interstitialAd=null;}});}
    private void loadRewarded(){RewardedAd.load(this,rewardedId(),new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd ad){rewardedAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){rewardedAd=null;loadRewarded();}@Override public void onAdFailedToShowFullScreenContent(AdError e){rewardedAd=null;loadRewarded();}});}@Override public void onAdFailedToLoad(LoadAdError e){rewardedAd=null;}});}
    private void showRewarded(){if(rewardedAd==null){Toast.makeText(this,"Rewarded sedang disediakan. Cuba lagi sebentar.",Toast.LENGTH_SHORT).show();loadRewarded();return;} RewardedAd ad=rewardedAd;rewardedAd=null;ad.show(this,item->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
    private void showInterstitial(){long now=SystemClock.elapsedRealtime();if(now-lastInterstitialShown<300000L||interstitialAd==null)return;lastInterstitialShown=now;InterstitialAd ad=interstitialAd;interstitialAd=null;ad.show(this);}
    private void showPrivacyOptions(){UserMessagingPlatform.showPrivacyOptionsForm(this,error->updatePrivacyButton());}
    public class AdBridge{@JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showRewarded());}@JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInterstitial());}@JavascriptInterface public boolean isPrivacyOptionsRequired(){return consentInformation!=null&&consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;}@JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacyOptions());}}
    @Override protected void onDestroy(){if(bannerView!=null)bannerView.destroy();if(webView!=null)webView.destroy();super.onDestroy();}
}
