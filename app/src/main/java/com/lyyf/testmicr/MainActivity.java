package com.lyyf.testmicr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    String test0="";
    String test1=" O1023955880O  3524";
    String test2="O1122OT122177238T";
    String test3="T122177238T 3524";
    String test4="T122177238T O1023955880O";
    String test5="O1122OT122177238T O1023955880O";
    String test6="T122177238T O3524 1023955880O";
    String test7="T122177238T 3524 1023955880O";
    String test8="T122177238T 3524 10239558D80O";
    String test9="T122177238T 354D20 1239558D80O";
    String test10="O123456O 1T1234D5678T 1234O1234567890123456O 123 A1234567890A";
    String test11="O123456O 1T1234D5678T O1234567890123456O 123 A1234567890A";
    String test12="O123456O 1T1234D5678T O1234567890123456O 123";
    String test13="O123456O 1T1234D5678T 1234567890123456O 123 A1234567890A";
    String test14="O123456O 1T1234D5678T 1234567890123456O 123";
    String test15="O123456O 1T1234D5678T 1234567890123456O A1234567890A";
    String test16="O123456O 1T1234D5678T 1234567890123456O";
    String test17="          T1234D5678T 1234 O567890123456O";
    String test18="T111314575T  013 120 1O 0234";
    private final int MICR_NODATA    = 0x01;
    private final int MICR_NOTRANSIT = 0x02;
    private final int MICR_NOACCOUNT = 0x04;
    private final int MICR_NOCHECK   = 0x08;
    private final int MICR_CANADIAN  = 0x10;
    private final int MICR_BUSINESS  = 0x20;
    private int currentMICRStatus=0;
    private String micrCheck;
    private String micrTransit;
    private String micrAmount;
    private String micrAccount;
    private int    micrResult;

    private final int SZMICR_TOAD          = 128;
    private final int SZMICR_FORMAT7600    = 128;
    private final int SZMICR_AUXONUSMAX    = 15;    // maximum length of the auxliliary onus field
    private final int SZMICR_CHECKNUMMAX   = 15;        // maximum length of the check number
    private final int SZMICR_TRANSITNUMMAX = 9;    // maximum length of the transit number
    private final int SZMICR_ONUSNUMMAX    = 19;    // maximum length of the onus number
    private final int SZMICR_ACCOUNTNUMMAX = 19;    // maximum length of the account number
    private final int SZMICR_AMOUNTMAX     = 10;    // maximum length of the amount

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private String FindAuxONUS(String account){
        StringBuilder check=new StringBuilder();
        if (account.charAt(0)=='O'){
            //Set check type as business
            micrResult|=MICR_BUSINESS;
            //check.append(account.charAt(0));
            for (int i=1;i<=SZMICR_CHECKNUMMAX;i++){
                if (account.charAt(i)=='O')
                    break;
                if (account.charAt(i)!=' '&&account.charAt(i)!='D'){
                    check.append(account.charAt(i));
                }
            }
        }
        return check.toString();
    }

    private int getCount(String str, String key) {
        if (str == null || key == null || "".equals(str.trim()) || "".equals(key.trim())) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(key, index)) != -1) {
            index = index + key.length();
            count++;
        }
        return count;
    }
    private String FindTransit(String account) {
        if (getCount(account,String.valueOf('T'))<2) {
            micrResult |= MICR_NOTRANSIT;
            return "";
        }
        StringBuilder transit = new StringBuilder();

        for (int i = account.indexOf('T') + 1; i < account.length(); i++) {
            if (account.charAt(i)=='T')
                break;
            if (account.charAt(i) != 'T' && account.charAt(i) != 'D'
                    && account.charAt(i) != ' ' && transit.toString().length() < SZMICR_TRANSITNUMMAX) {
                transit.append(account.charAt(i));
                if (account.charAt(i) == 'D' && transit.toString().length() >= 4)
                    micrResult |= MICR_CANADIAN;
            }
        }
        return transit.toString();
    }
    private String FindAccount(String scannedData) {
       int indexFirstO=0;
       int indexLastUnknown=0;
       boolean bFlag=false;
        for (int i=scannedData.length()-1;i>=0;i--){
            if (scannedData.charAt(i) == 'O'&&indexFirstO==0) {
                indexFirstO=i;
            }else{
                if (!bFlag){
                    if ((scannedData.charAt(i)=='O'&&indexFirstO!=0)||scannedData.charAt(i)=='T'
                            ||scannedData.charAt(i)=='A'){
                        indexLastUnknown=i;
                        bFlag=true;
                    }
                }

                if (scannedData.charAt(i)!='O'&&scannedData.charAt(i)<='0'
                        &&scannedData.charAt(i)>='9'&&scannedData.charAt(i)!='D'&&scannedData.charAt(i)!=' '&&indexLastUnknown==0)
                    indexLastUnknown=i;
            }
       }
        if (indexLastUnknown+1<indexFirstO)
        return scannedData.substring(indexLastUnknown+1,indexFirstO);
        else
            micrResult|=MICR_NOACCOUNT;
         return "";
    }

    private String FindAmount(String scannedData){
        StringBuffer sb=new StringBuffer();
        int indexFirst=0;
        boolean bGetA=false;
        for (int i=0;i<scannedData.length();i++){
          if (scannedData.charAt(i)=='A'){
              indexFirst=i+1;
              bGetA=true;
          }
        }
        if (indexFirst<scannedData.length()&&bGetA){
            for (int j=indexFirst;j<scannedData.length();j++){
                if (scannedData.charAt(j)>='0'&&scannedData.charAt(j)<='9'){
                    sb.append(scannedData.charAt(j));
                }
            }
        }

      return sb.toString();
    }
    String strAccount="";
    private String FindCheck(String scannedData,String check,String account) {
       if (check.length()>0)
           return check;

       StringBuilder sb=new StringBuilder();
       for (int i=scannedData.length()-1;i>0;i--){
           if (scannedData.charAt(i)>'0'&&scannedData.charAt(i)<'9'){
               sb.append(scannedData.charAt(i));
           }
       }

        if (sb.toString().length() == 0) {
            for (int j = account.length() - 1; j > 0; j--) {
                if (account.charAt(j) == ' ') {
                    j--;
                    while (j != -1) {
                        if ((account.charAt(j) >= '0' && account.charAt(j) <= '9')||account.charAt(j)=='D'||account.charAt(j)==' ')
                            sb.append(account.charAt(j));
                        j--;
                    }
                }
            }

            if (sb.toString().length() > 0) {
                StringBuffer stringBuffer = new StringBuffer (sb.toString());
                stringBuffer.reverse();
                strAccount = account.substring(account.indexOf(stringBuffer.toString())+stringBuffer.toString().length()+1);
                strAccount= strAccount.replace("D","");
                strAccount=strAccount.replace(" ","");
                return stringBuffer.toString().replace(String.valueOf('D'),"").trim();
            }
        }
      if (sb.toString().length()==0)
          micrResult|=MICR_NOCHECK;

        return sb.toString().replace(String.valueOf('D'),"").trim();
    }
    private void validateData(String scannedData){
        Log.i(TAG,"ScannedData:"+scannedData);
        int i=0;
        if (scannedData=="T122177238T 354D20 1239558D80O")
            i=1;
        if (scannedData.length()==0)
            return;
        String strCheckInfo=FindAuxONUS(scannedData);
        if (strCheckInfo.length()>0){
            scannedData=scannedData.substring(scannedData.indexOf(strCheckInfo)+strCheckInfo.length());
        }
        Log.i(TAG,"FindAuxONUS result:"+strCheckInfo);
        Log.i(TAG,"scannedData:"+scannedData);

        String transit=FindTransit(scannedData);
        if (transit.length()>0){
            scannedData=scannedData.substring(scannedData.indexOf(transit)+transit.length());
        }
        Log.i(TAG,"FindTransit result:"+transit);
        Log.i(TAG,"scannedData:"+scannedData);

        String account=FindAccount(scannedData);
     //   account= account.replace(" ","");
        if (account.length()>0){
            scannedData=scannedData.substring(scannedData.indexOf(account)+account.length());
        }
        Log.i(TAG,"account result:"+account);
        Log.i(TAG,"scannedData:"+scannedData);
        String amount=FindAmount(scannedData);
        if (amount.length()>0){
            scannedData=scannedData.substring(scannedData.indexOf(amount)+amount.length());
        }

        Log.i(TAG,"amount result:"+amount);
        Log.i(TAG,"scannedData:"+scannedData);
        String check=FindCheck(scannedData,strCheckInfo,account);

        Log.i(TAG,"check result:"+check);
        Log.i(TAG,"Current Account:"+strAccount);
        Log.i(TAG,"scannedData:"+scannedData);
        Log.i(TAG,"MICR_RESULT:"+micrResult);
    }


    public void Test(View view) {

        validateData(test0);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test1);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test2);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test3);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test4);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test5);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test6);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test7);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test8);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test9);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test10);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test11);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test12);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test13);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test14);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test15);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test16);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test17);
        micrResult=0;
        Log.i(TAG,"---------------------------------");
        validateData(test18);



    }
}