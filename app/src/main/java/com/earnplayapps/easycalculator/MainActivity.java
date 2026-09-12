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

    private WebView webView;
    private AdView bannerView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private ConsentInformation consentInformation;
    private long lastInterstitialShown=0;
    private boolean adsStarted=false;

    private String bannerId(){return USE_TEST_ADS?TEST_BANNER:LIVE_BANNER;}
    private String interstitialId(){return USE_TEST_ADS?TEST_INTERSTITIAL:LIVE_INTERSTITIAL;}
    private String rewardedId(){return USE_TEST_ADS?TEST_REWARDED:LIVE_REWARDED;}

    @Override protected void onCreate(Bundle state){super.onCreate(state);buildUi();setupConsentAndAds();}

    private void buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,247,251));
        webView=new WebView(this);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);s.setDisplayZoomControls(false);
        webView.setBackgroundColor(Color.rgb(245,247,251));
        webView.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);patchCalculatorUi();}});
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AdBridge(),"Android");
        webView.loadUrl("file:///android_asset/index.html");
        root.addView(webView,new LinearLayout.LayoutParams(-1,0,1f));
        bannerView=new AdView(this);bannerView.setAdUnitId(bannerId());bannerView.setAdSize(AdSize.BANNER);bannerView.setBackgroundColor(Color.WHITE);
        root.addView(bannerView,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
    }

    private void patchCalculatorUi(){
        if(webView==null)return;
        String js="javascript:(function(){try{"+
        "var st=document.getElementById('__easy_calc_fix');if(!st){st=document.createElement('style');st.id='__easy_calc_fix';st.textContent="+
        "'html,body{overflow-x:hidden!important}body.basicMode{min-height:100dvh!important;overflow:hidden!important}body.basicMode #app{min-height:calc(100dvh - 1px)!important;height:calc(100dvh - 1px)!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}body.basicMode .keys{flex:1 1 auto!important;min-height:0!important;grid-template-rows:repeat(5,minmax(0,1fr))!important}body.basicMode .key{height:auto!important;min-height:0!important}.basicMode .zero{grid-column:span 2!important}.appLogo{width:38px;height:38px;border:1px solid #e5e9f1;border-radius:12px;background:linear-gradient(145deg,#fff,#edf2f7);display:grid;place-items:center;box-shadow:0 7px 18px rgba(30,42,58,.10),inset 0 1px 0 #fff}.appLogo svg{width:22px;height:22px}.headActions{justify-content:flex-end;gap:6px}.headActions .other{display:flex;align-items:center;gap:5px;padding:7px 9px;font-size:10px;font-weight:750}.headActions .other svg{width:17px;height:17px}.headActions .hamb{width:34px;height:34px;border-radius:10px}.headActions .hamb i{width:16px;height:2px}.cat .ico,.toolico{border:1px solid rgba(255,255,255,.96)!important;box-shadow:inset 0 1px 0 rgba(255,255,255,.98),0 7px 16px rgba(30,42,58,.10)!important;background:linear-gradient(145deg,#fff,rgba(245,247,250,.88))!important}.cat .ico svg,.toolico svg,.menuIcon svg{width:20px;height:20px}.cat b{font-size:12.5px}.cat span{font-size:9.5px}.historyClear{border:1px solid #f0d7bb;background:#fff8f0;color:#b96500;border-radius:9px;padding:7px 10px;font-size:10px;font-weight:800}.historyItem .historyBtns{display:flex;gap:6px;justify-content:flex-end}.menuGrid{display:grid;gap:9px}.menuItem{width:100%;display:grid;grid-template-columns:38px 1fr 16px;gap:10px;align-items:center;border:1px solid #e5e9ef;background:linear-gradient(145deg,#fff,#f7f9fb);border-radius:14px;padding:10px;text-align:left;box-shadow:0 7px 18px rgba(30,42,58,.06)}.menuIcon{width:38px;height:38px;border-radius:11px;display:grid;place-items:center;background:linear-gradient(145deg,#fff4e9,#fff);color:#d8750a;box-shadow:inset 0 1px 0 #fff,0 6px 14px rgba(30,42,58,.08)}.menuItem b{font-size:13px}.menuItem span{display:block;color:#6f7a8e;font-size:10px;margin-top:2px}.menuArrow{color:#a1a9b8;font-size:18px}';document.head.appendChild(st);} "+
        "document.body.classList.add('basicMode');"+
        "document.querySelectorAll('.merdeka,[class*=merdeka],[id*=merdeka]').forEach(function(e){e.remove()});"+
        "var ks=document.querySelector('.keys');if(ks){ks.querySelectorAll('.key').forEach(function(k){if(k.textContent.trim()==='(')k.remove()});var z=Array.from(ks.querySelectorAll('.key')).find(function(k){return k.textContent.trim()==='0'});if(z)z.classList.add('zero');}"+
        "var top=document.querySelector('.top'),brand=document.querySelector('.brand'),ha=document.querySelector('.headActions'),hamb=document.getElementById('hamb'),other=document.getElementById('other');"+
        "if(top&&brand&&ha&&hamb&&other){var logo=document.getElementById('appLogo');if(!logo){logo=document.createElement('button');logo.id='appLogo';logo.className='appLogo';logo.type='button';logo.setAttribute('aria-label','Easy App');logo.innerHTML='<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#d8750a\" stroke-width=\"1.9\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M4 5.5h16v13H4z\"/><path d=\"M8 9h8M8 12h5M8 15h7\"/></svg>';top.insertBefore(logo,brand);}if(hamb.parentElement!==ha)ha.appendChild(hamb);other.innerHTML='<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><rect x=\"4\" y=\"5\" width=\"16\" height=\"14\" rx=\"3\"/><path d=\"M8 9h8M8 12h5M8 15h7\"/></svg><span>Calculator Lain</span>';logo.onclick=function(){if(typeof openCats==='function')openCats()};}"+
        "var cats=document.getElementById('catGrid');if(cats){var practical=["+
        "['Basic','blue','Pengiraan asas',[['Percentage','Peratus','pct'],['Fraction','Pecahan','fraction'],['Ratio','Nisbah','ratio'],['Average','Purata','average'],['Percentage Change','Perubahan peratus','pctchange']]],"+
        "['Money','green','Duit & harga',[['Discount','Diskaun','discount'],['Profit & Loss','Untung / rugi','profit'],['Tax / SST','Cukai','tax'],['Tip','Tip','tip'],['Split Bill','Bahagi bil','split'],['Loan / EMI','Ansuran','emi'],['ROI','Pulangan','roi']]],"+
        "['Converter','cyan','Tukar unit',[['Length','Panjang','length'],['Weight','Berat','weight'],['Temperature','Suhu','temperature'],['Area','Luas','area'],['Volume','Isipadu','volume'],['Speed','Kelajuan','speed'],['Time','Masa','time']]],"+
        "['Date & Time','pink','Tarikh & umur',[['Age','Umur','age'],['Date Difference','Beza tarikh','datediff'],['Add Days','Tambah hari','adddays'],['Working Days','Hari bekerja','workdays']]],"+
        "['Health','pink','Kecergasan',[['BMI','BMI','bmi'],['BMR','Kalori asas','bmr'],['TDEE','Kalori harian','tdee'],['Water Intake','Keperluan air','water'],['Pace','Pace larian','pace']]],"+
        "['Home & Daily','yellow','Rumah & harian',[['Fuel Cost','Kos minyak','fuel'],['Electricity Cost','Kos elektrik','electric'],['Room Area','Luas bilik','room'],['Tile Estimate','Anggaran jubin','tile'],['Grocery Unit Price','Harga seunit','unitprice']]],"+
        "['Study','violet','Markah & belajar',[['GPA','GPA','gpa'],['Grade Percentage','Peratus markah','grade'],['Weighted Average','Purata berwajaran','weighted'],['Required Marks','Markah diperlukan','required']]],"+
        "['Work & Business','orange','Kerja & bisnes',[['Hourly Pay','Gaji ikut jam','hourly'],['Overtime','Bayaran OT','overtime'],['Commission','Komisen','commission'],['Margin','Margin keuntungan','margin'],['Break-even','Pulang modal','breakeven'],['Unit Cost','Kos seunit','unitcost']]]"+
        "];window.__easyPractical=practical;var icon=function(n){var p={Basic:'<path d=\"M5 12h14M12 5v14\"/>',Money:'<path d=\"M12 4v16M16 7.5c0-1.7-1.7-2.5-4-2.5s-4 1-4 2.8c0 4.2 8 1.7 8 6 0 1.8-1.7 2.7-4 2.7s-4-.9-4-2.6\"/>',Converter:'<path d=\"M7 7h11l-3-3M17 17H6l3 3M18 7v4M6 13v4\"/>', 'Date & Time':'<circle cx=\"12\" cy=\"12\" r=\"8\"/><path d=\"M12 7v5l3 2\"/>',Health:'<path d=\"M20 9c0 5-8 10-8 10S4 14 4 9a4 4 0 0 1 7-2 4 4 0 0 1 7 2Z\"/>', 'Home & Daily':'<path d=\"M4 11 12 4l8 7v8H4zM9 19v-5h6v5\"/>',Study:'<path d=\"M4 5h16v14H4zM8 9h8M8 13h6\"/>', 'Work & Business':'<path d=\"M5 7h14v12H5zM9 7V5h6v2M8 12h8\"/>'};return '<svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" stroke-linejoin=\"round\">'+(p[n]||p.Basic)+'</svg>';};cats.innerHTML=practical.map(function(c,i){return '<button class=\"cat\" data-i=\"'+i+'\"><div class=\"ico '+c[1]+'\">'+icon(c[0])+'</div><div><b>'+c[0]+'</b><span>'+c[2]+'</span></div><div class=\"chev\">›</div></button>'}).join('');cats.querySelectorAll('.cat').forEach(function(b){b.onclick=function(){var c=practical[Number(b.dataset.i)];closeCats();clearAll();document.getElementById('toolTitle').textContent=c[0];document.getElementById('toolDesc').textContent=c[2];document.getElementById('toolBody').innerHTML='<div class=\"toolgrid\">'+c[3].map(function(x){return '<button class=\"tool\" onclick=\"openTool(\\\''+x[2]+'\\\',\\\''+x[0]+'\\\')\"><div class=\"toolico\">'+icon(c[0])+'</div><div><b>'+x[0]+'</b><span>'+x[1]+'</span></div><div class=\"toolchev\">›</div></button>'}).join('')+'</div>';document.getElementById('toolOverlay').classList.add('show');}});}"+
        "var hb=document.getElementById('historyBtn'),ho=document.getElementById('historyOverlay'),hl=document.getElementById('historyList');"+
        "function renderEasyHistory(){if(!hl)return;var arr=Array.isArray(historyItems)?historyItems:[];hl.innerHTML='<div style=\"display:flex;justify-content:flex-end;margin-bottom:8px\"><button class=\"historyClear\" id=\"easyClearHistory\">Padam semua</button></div>'+(arr.length?arr.map(function(h,i){var a=(h&&h.expr)||h&&h.expression||'',r=(h&&h.result)||h&&h.value||'';return '<div class=\"historyItem\"><b>'+String(a)+'</b><span>= '+String(r)+'</span><div class=\"historyBtns\"><button data-use=\"'+i+'\">Guna semula</button><button data-del=\"'+i+'\">Padam</button></div></div>'}).join(''):'<div class=\"historyEmpty\">Belum ada sejarah pengiraan.</div>');var cl=document.getElementById('easyClearHistory');if(cl)cl.onclick=function(){if(Array.isArray(historyItems))historyItems.length=0;try{localStorage.removeItem('easyCalculatorHistory')}catch(e){}renderEasyHistory()};hl.querySelectorAll('[data-del]').forEach(function(b){b.onclick=function(){historyItems.splice(Number(b.dataset.del),1);renderEasyHistory()}});hl.querySelectorAll('[data-use]').forEach(function(b){b.onclick=function(){var h=historyItems[Number(b.dataset.use)];if(h){var v=h.expr||h.expression||'';if(typeof setExpr==='function')setExpr(String(v));else{expr=String(v);if(document.getElementById('expr'))document.getElementById('expr').textContent=expr;}if(ho)ho.classList.remove('show')}}});}"
        "if(hb){hb.onclick=function(){renderEasyHistory();ho.classList.add('show')}}"+
        "var mo=document.getElementById('menuOverlay');if(!mo){mo=document.createElement('div');mo.id='menuOverlay';mo.className='overlay';mo.innerHTML='<div class=\"modal\"><div class=\"mhead\"><div><h2>Menu</h2><p>Fungsi tambahan Easy Calculator</p></div><button class=\"close\" id=\"ecMenuClose\">×</button></div><div class=\"menuGrid\"><button class=\"menuItem\" id=\"ecSettings\"><div class=\"menuIcon\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\"><circle cx=\"12\" cy=\"12\" r=\"3\"/><path d=\"M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-1.8 1.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6V20h-2.6v-.1a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1-1.8-1.8.1-.1A1.7 1.7 0 0 0 8 15a1.7 1.7 0 0 0-1.6-1H6v-2.6h.1A1.7 1.7 0 0 0 8 10a1.7 1.7 0 0 0-.3-1.9l-.1-.1 1.8-1.8.1.1a1.7 1.7 0 0 0 1.9.3 1.7 1.7 0 0 0 1-1.6V5H15v.1a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1 1.8 1.8-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.1V14h-.1a1.7 1.7 0 0 0-1.6 1Z\"/></svg></div><div><b>Tetapan</b><span>Pilihan aplikasi</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"ecPrivacy\"><div class=\"menuIcon\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\"><path d=\"M12 4 19 7v5c0 4.2-2.9 7.3-7 8-4.1-.7-7-3.8-7-8V7z\"/><path d=\"m9 12 2 2 4-4\"/></svg></div><div><b>Privasi</b><span>Pilihan privasi iklan</span></div><div class=\"menuArrow\">›</div></button><button class=\"menuItem\" id=\"ecAbout\"><div class=\"menuIcon\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\"><circle cx=\"12\" cy=\"12\" r=\"8\"/><path d=\"M12 11v5M12 8h.01\"/></svg></div><div><b>Tentang Easy Calculator</b><span>Calculator mudah untuk kegunaan harian</span></div><div class=\"menuArrow\">›</div></button></div></div>';document.body.appendChild(mo);document.getElementById('ecMenuClose').onclick=function(){mo.classList.remove('show')};document.getElementById('ecSettings').onclick=function(){mo.classList.remove('show');alert('Tetapan Easy Calculator akan tersedia di sini tanpa mengubah calculator utama.')};document.getElementById('ecPrivacy').onclick=function(){mo.classList.remove('show');if(window.Android&&Android.showPrivacyOptions)Android.showPrivacyOptions()};document.getElementById('ecAbout').onclick=function(){mo.classList.remove('show');alert('Easy Calculator ialah calculator ringkas untuk kegunaan harian.')};}"+
        "hamb.onclick=function(){mo.classList.add('show')};"+
        "}catch(e){console.log('Easy Calculator UI patch',e);}})();";
        webView.evaluateJavascript(js,null);
    }

    private void setupConsentAndAds(){
        consentInformation=UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters params=new ConsentRequestParameters.Builder().build();
        consentInformation.requestConsentInfoUpdate(this,params,()->{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->{updatePrivacyButton();startAdsIfAllowed();});
            updatePrivacyButton();startAdsIfAllowed();
        },error->{updatePrivacyButton();startAdsIfAllowed();});
    }

    private void updatePrivacyButton(){
        if(webView==null||consentInformation==null)return;
        boolean req=consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        webView.post(()->webView.evaluateJavascript("(function(){var b=document.getElementById('privacyBtn');if(b)b.style.display="+(req?"'inline-block'":"'none'")+";})();",null));
    }
    private void startAdsIfAllowed(){if(adsStarted||consentInformation==null||!consentInformation.canRequestAds())return;adsStarted=true;MobileAds.initialize(this,status->{loadBanner();loadInterstitial();loadRewarded();});}
    private void loadBanner(){if(bannerView!=null)bannerView.loadAd(new AdRequest.Builder().build());}
    private void loadInterstitial(){InterstitialAd.load(this,interstitialId(),new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){@Override public void onAdLoaded(InterstitialAd ad){interstitialAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){interstitialAd=null;loadInterstitial();}@Override public void onAdFailedToShowFullScreenContent(AdError e){interstitialAd=null;loadInterstitial();}});}@Override public void onAdFailedToLoad(LoadAdError e){interstitialAd=null;}});}
    private void loadRewarded(){RewardedAd.load(this,rewardedId(),new AdRequest.Builder().build(),new RewardedAdLoadCallback(){@Override public void onAdLoaded(RewardedAd ad){rewardedAd=ad;ad.setFullScreenContentCallback(new FullScreenContentCallback(){@Override public void onAdDismissedFullScreenContent(){rewardedAd=null;loadRewarded();}@Override public void onAdFailedToShowFullScreenContent(AdError e){rewardedAd=null;loadRewarded();}});}@Override public void onAdFailedToLoad(LoadAdError e){rewardedAd=null;}});}
    private void showRewarded(){if(rewardedAd==null){Toast.makeText(this,"Rewarded sedang disediakan. Cuba lagi sebentar.",Toast.LENGTH_SHORT).show();loadRewarded();return;}RewardedAd ad=rewardedAd;rewardedAd=null;ad.show(this,item->Toast.makeText(this,"Reward diterima: 1",Toast.LENGTH_SHORT).show());}
    private void showInterstitial(){long now=SystemClock.elapsedRealtime();if(now-lastInterstitialShown<300000L||interstitialAd==null)return;lastInterstitialShown=now;InterstitialAd ad=interstitialAd;interstitialAd=null;ad.show(this);}
    private void showPrivacyOptions(){
        if(consentInformation==null)return;
        if(consentInformation.getPrivacyOptionsRequirementStatus()!=ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED){Toast.makeText(this,"Pilihan privasi tidak diperlukan pada masa ini.",Toast.LENGTH_SHORT).show();return;}
        try{UserMessagingPlatform.showPrivacyOptionsForm(this,error->{if(error!=null)Toast.makeText(this,"Privacy Options tidak dapat dibuka sekarang.",Toast.LENGTH_SHORT).show();updatePrivacyButton();});}catch(Exception e){Toast.makeText(this,"Privacy Options tidak dapat dibuka sekarang.",Toast.LENGTH_SHORT).show();}
    }

    public class AdBridge{
        @JavascriptInterface public void showRewardedAd(){runOnUiThread(()->showRewarded());}
        @JavascriptInterface public void onNaturalTransition(){runOnUiThread(()->showInterstitial());}
        @JavascriptInterface public void showPrivacyOptions(){runOnUiThread(()->showPrivacyOptions());}
    }
}
