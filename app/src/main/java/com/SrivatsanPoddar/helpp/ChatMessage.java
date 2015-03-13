package com.SrivatsanPoddar.helpp;

//Retrofit object for a chat message
public class ChatMessage
{
    public String message;
    public String set_target_company_id;
    public String pair;
    public String pairsIndex;
    public String target_role;
    public String request_format;
    public String request_type;
    public double amount;

    public ChatMessage(String mString) {
        message = mString;
    }

    public ChatMessage(String mString, String mPairsIndex) {
        message = mString;
        pairsIndex = mPairsIndex;
        target_role = "agent";
        request_type = "MESSAGE";
    }

    public void setRequestType(String myRequestType) {
        request_type = myRequestType;
    }
    
    public void setTargetCompany(String target){
        set_target_company_id = target;
    }
    
    public void setString(String mString) {
        message = mString;
    }
    
    public String toString() {
        return message;
    }
    
    public void setPairsIndex(String mPairsIndex) {
        pairsIndex = mPairsIndex;
        target_role = "agent";
    }
}
