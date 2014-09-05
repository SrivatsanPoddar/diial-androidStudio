package com.SrivatsanPoddar.helpp;

public class ChatMessage
{
    public String message;
    public String set_target_company_id;
    public String pair;
    public String pairsIndex;
    public String target_role;
    public String request_format;
    public String request_type;
    
    public ChatMessage(String mString) {
        message = mString;
    }

    public ChatMessage(String mString, String mPairsIndex) {
        message = mString;
        pairsIndex = mPairsIndex;
        target_role = "agent";
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
