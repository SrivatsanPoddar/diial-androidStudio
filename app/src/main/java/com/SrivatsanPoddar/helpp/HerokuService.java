package com.SrivatsanPoddar.helpp;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface HerokuService
{
	@GET("/nodes")
	void nodes(Callback<Node[]> cb);
	
	@GET("/{company_id}/questions")
    void getQuestions(@Path("company_id") String company_id, Callback<ArrayList<SurveyQuestion>> cb);
	
    @POST("/responses")
    void addResponse(@Body SurveyQuestion question, Callback<String> cb);
    
    @GET("/requestCallToken")
    void getCallToken(Callback<CallToken> cb);
    
    @POST("/call")
    void addCall(@Body Call call, Callback<Call> cb);
    
}