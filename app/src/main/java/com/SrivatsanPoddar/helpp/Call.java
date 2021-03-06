package com.SrivatsanPoddar.helpp;

import java.io.Serializable;
import java.util.ArrayList;

//Call object used for Retrofit
public class Call implements Serializable
{
    private static final long serialVersionUID = -7804709187605780091L;

    public String device_id;
    public String start_time; 
    public String end_time;
    public String company_id;
    public ArrayList<String> response_ids = new ArrayList<String>();
    public ArrayList<Node> call_path;
    public ArrayList<String> stored_information;
    public String call_path_string;
    public int[] call_path_id; //Call path represented by Node ID
    public String company_name;  //For retrieval when inner join on database call

    public String toString() {
        return call_path_string;
    }
}
