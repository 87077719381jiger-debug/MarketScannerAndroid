package com.jiger.marketscanner;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    TextView result;
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        result=findViewById(R.id.result);
        Button scan=findViewById(R.id.scan);
        scan.setOnClickListener(v -> new Thread(this::scan).start());
    }
    void scan(){
        runOnUiThread(() -> result.setText("SCAN…\nНарық дерегі алынуда"));
        try{
            URL u=new URL("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=5m&limit=100");
            HttpURLConnection c=(HttpURLConnection)u.openConnection();
            c.setConnectTimeout(10000); c.setReadTimeout(10000);
            BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder s=new StringBuilder(); String line;
            while((line=r.readLine())!=null)s.append(line);
            JSONArray a=new JSONArray(s.toString());
            double[] close=new double[a.length()];
            double[] vol=new double[a.length()];
            for(int i=0;i<a.length();i++){ JSONArray k=a.getJSONArray(i); close[i]=k.getDouble(4); vol[i]=k.getDouble(5); }
            double e9=ema(close,9), e21=ema(close,21);
            double rsi=rsi(close,14);
            double avg=0; for(int i=a.length()-21;i<a.length()-1;i++) avg+=vol[i]; avg/=20;
            double vr=vol[a.length()-1]/avg;
            String sig = e9>e21 && rsi>=50 && rsi<70 ? "BUY ↑" :
                         e9<e21 && rsi<=50 && rsi>30 ? "SELL ↓" : "NEUTRAL";
            String msg=String.format(Locale.US,"BTCUSDT • 5m\n\n%s\n\nEMA9: %.2f\nEMA21: %.2f\nRSI14: %.1f\nVolume: %.2fx\n\nСигнал — тек техникалық скан.",sig,e9,e21,rsi,vr);
            runOnUiThread(() -> result.setText(msg));
        }catch(Exception ex){ runOnUiThread(() -> result.setText("Дерек алу қатесі.\nИнтернетті тексеріңіз."));}
    }
    double ema(double[] x,int n){ double e=x[0], k=2.0/(n+1); for(int i=1;i<x.length;i++) e=x[i]*k+e*(1-k); return e; }
    double rsi(double[] x,int n){ double g=0,l=0; for(int i=x.length-n;i<x.length;i++){double d=x[i]-x[i-1]; if(d>0)g+=d;else l-=d;} if(l==0)return 100; return 100-100/(1+g/l); }
}