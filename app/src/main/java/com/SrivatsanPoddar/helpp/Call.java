package com.SrivatsanPoddar.helpp;

import java.io.Serializable;
import java.util.ArrayList;

public class Call implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = -7804709187605780091L;
    
    
    public String device_id;
    public String start_time; 
    public String end_time;
    public String company_id;
    public ArrayList<String> response_ids = new ArrayList<String>();
    public Node[] call_path;
    public ArrayList<String> stored_information;
    public String call_path_string;
}
