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
            @Override public void onPageFinished(WebView view,String url){super.onPageFinished(view,url);patchCalculatorUi();}
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
        "'html,body{overflow-x:hidden!important}body.basicMode{min-height:100dvh!important;overflow:hidden!important}body.basicMode #app{min-height:calc(100dvh - 1px)!important;height:calc(100dvh - 1px)!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}body.basicMode .keys{flex:1 1 auto!important;min-height:0!important;grid-template-rows:repeat(5,minmax(0,1fr))!important}body.basicMode .key{height:auto!important;min-height:0!important}.basicMode .zero{grid-column:span 2!important}.appLogo{width:38px;height:38px;border:0;border-radius:12px;background:linear-gradient(145deg,#fff,#eef2f7);display:grid;place-items:center;box-shadow:0 7px 18px rgba(30,42,58,.10),inset 0 1px 0 #fff}.appLogo span{font-size:11px;font-weight:950;color:#d8750a}.headActions{justify-content:flex-end}.headActions .hamb{width:34px;height:34px;border-radius:10px}.headActions .hamb i{width:17px;height:2px}.headActions .other{padding:8px 10px;font-size:10.5px}.cat .ico,.toolico{border:1px solid rgba(255,255,255,.95)!important;box-shadow:inset 0 1px 0 rgba(255,255,255,.95),0 7px 16px rgba(30,42,58,.10)!important;background:linear-gradient(145deg,#fff,rgba(245,247,250,.85))!important}.menuGrid{display:grid;gap:9px}.menuItem{width:100%;display:grid;grid-template-columns:38px 1fr 16px;gap:10px;align-items:center;border:1px solid #e5e9ef;background:linear-gradient(145deg,#fff,#f7f9fb);border-radius:14px;padding:10px;text-align:left;box-shadow:0 7px 18px rgba(30,42,58,.06)}.menuIcon{width:38px;height:38px;border-radius:11px;display:grid;place-items:center;background:linear-gradient(145deg,#fff4e9,#fff);color:#d8750a;font-weight:900;box-shadow:inset 0 1px 0 #fff,0 6px 14px rgba(30,42,58,.08)}.menuItem b{font-size:13px}.menuItem span{display:block;color:#6f7a8e;font-size:10px;margin-top:2px}.menuArrow{color:#a1a9b8;font-size:18px}';document.head.appendChild(s);} "+
        "document.body.classList.add('basicMode');"+
        "document.querySelectorAll('.merdeka,[class*=merdeka],[id*=merdeka]').forEach(function(e){e.remove()});"+
        "var ks=document.querySelector('.keys');if(ks){ks.querySelectorAll('.key').forEach(function(k){if(k.textContent.trim()==='(')k.remove()});var z=Array.from(ks.querySelectorAll('.key')).find(function(k){return k.textContent.trim()==='0'});if(z)z.classList.add('zero');}"+
        "var top=document.querySelector('.top'),brand=document.querySelector('.brand'),ha=document.querySelector('.headActions'),hamb=document.getElementById('hamb');"+
        "if(top&&brand&&ha&&hamb){var logo=document.getElementById('appLogo');if(!logo){logo=document.createElement('button');logo.id='appLogo';logo.className='appLogo';logo.setAttribute('aria-label','Easy App');logo.innerHTML='<span>EA</span>';top.insertBefore(logo,brand);}if(hamb.parentElement!==ha)ha.appendChild(hamb);logo.onclick=function(){openCats()};}"+
        "var cats=document.getElementById('catGrid');if(cats){var practical=["+
        "['Basic','+','blue','Pengiraan asas',[['Percentage','Peratus','pct'],['Fraction','Pecahan','fraction'],['Ratio','Nisbah','ratio'],['Average','Purata','average'],['Percentage Change','Perubahan peratus','pctchange']]],"+
        "['Money','RM','green','Duit & harga',[['Discount','Diskaun','discount'],['Profit & Loss','Untung / rugi','profit'],['Tax / SST','Cukai','tax'],['Tip','Tip','tip'],['Split Bill','Bahagi bil','split'],['Loan / EMI','Ansuran','emi'],['ROI','Pulangan','roi']]],"+
        "['Converter','↔','cyan','Tukar unit',[['Length','Panjang','length'],['Weight','Berat','weight'],['Temperature','Suhu','temperature'],['Area','Luas','area'],['Volume','Isipadu','volume'],['Speed','Kelajuan','speed'],['Time','Masa','time']]],"+
        "['Date & Time','◷','pink','Tarikh & umur',[['Age','Umur','age'],['Date Difference','Beza tarikh','datediff'],['Add Days','Tambah hari','adddays'],['Working Days','Hari bekerja','workdays']]],"+
        "['Health','♥','pink','Kecergasan',[['BMI','BMI','bmi'],['BMR','Kalori asas','bmr'],['TDEE','Kalori harian','tdee'],['Water Intake','Keperluan air','water'],['Pace','Pace larian','pace']]],"+
        "['Home & Daily','⌂','yellow','Rumah & harian',[['Fuel Cost','Kos minyak','fuel'],['Electricity Cost','Kos elektrik','electric'],['Room Area','Luas bilik','room'],['Tile Estimate','Anggaran jubin','tile'],['Grocery Unit Price','Harga seunit','unitprice']]],"+
        "['Study','A+','violet','Markah & belajar',[['GPA','GPA','gpa'],['Grade Percentage','Peratus markah','grade'],['Weighted Average','Purata berwajaran','weighted'],['Required Marks','Markah diperlukan','required']]],"+
        "['Work & Business','▣','orange','Kerja & bisnes',[['Hourly Pay','Gaji ikut jam','hourly'],['Overtime','Bayaran OT','overtime'],['Commission','Komisen','commission'],['Margin','Margin keuntungan','margin'],['Break-even','Pulang modal','breakeven'],['Unit Cost','Kos seunit','unitcost']]]"+
        "];window.__ecPractical=practical;cats.innerHTML=practical.map(function(c,i){return '<button class=\"cat\" data-i=\"'+i+'\"><div class=\"ico '+c[2]+'\">'+c[1]+'</div><div><b>'+c[0]+'</b><span>'+c[3]+'</span></div><div class=\"chev\">›</div></button>'}).join('');cats.querySelectorAll('.cat').forEach(function(b){b.onclick=function(){var c=practical[Number(b.dataset.i)];closeCats();clearAll();document.getElementById('toolTitle').textContent=c[0];document.getElementById('toolDesc').textContent=c[3];document.getElementById('toolBody').innerHTML='<div class=\"toolgrid\">'+c[4].map(function(x){return '<button class=\"tool\" onclick=\"openTool(\\\''+x[2]+'\\\',\\\''+x[0]+'\\\')\"><div class=\"toolico\">'+x[0].slice(0,3)+'</div><div><b>'+x[0]+'</b><span>'+x[1]+'</span></div><div class=\"toolchev\">›</div></button>'}).join('')+'</div>';document.getElementById('toolOverlay').classList.add('show');}});}"+
        "var mo=document.getElementById('menuOverlay');if(!mo){mo=document.createElement('div');mo.id='menuOverlay';mo.className='overlay';mo.innerHTML='<div class=\"modal\"><div class=\"mhead\"><div><h2>Menu</h2><p>Fungsi tambahan Easy Calculator</p></div><button class=\"close\" id=\"ecMenuClose\">×</button></div><div class=\"menuGrid\"><button class=\"menuItem\" id=\"ecSettings\"><div class=\"menuIcon\">S</div><div><b>Tetapan</b><span>Pilihan aplikasi</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"ecPrivacy\"><div class=\"menuIcon\">P</div><div><b>Privasi</b><span>Pilihan privasi iklan</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"ecAbout\"><div class=\"menuIcon\">i</div><div><b>Tentang Easy Calculator</b><span>Calculator mudah untuk kegunaan harian</span></div><div class=\"menuArrow\">›</div></button></div></div>';document.body.appendChild(mo);document.getElementById('ecMenuClose').onclick=function(){mo.classList.remove('show')};document.getElementById('ecSettings').onclick=function(){mo.classList.remove('show');alert('Tetapan Easy Calculator akan tersedia di sini tanpa mengubah calculator utama.')};document.getElementById('ecPrivacy').onclick=function(){mo.classList.remove('show');if(window.Android&&Android.showPrivacyOptions)Android.showPrivacyOptions()};document.getElementById('ecAbout').onclick=function(){mo.classList.remove('show');alert('Easy Calculator ialah calculator ringkas untuk kegunaan harian.')};}"+
        "hamb.onclick=function(){mo.classList.add('show')};"+
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

    private void updatePrivacyButton(){if(webView==null||consentInformation==null)return;boolean req=consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;webView.post(()->webView.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(req?"'inline-block'":"'none'")+";})();",null));}
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
