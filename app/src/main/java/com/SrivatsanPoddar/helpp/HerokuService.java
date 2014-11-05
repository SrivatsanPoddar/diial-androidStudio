package com.SrivatsanPoddar.helpp;

import java.util.ArrayList;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

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

    @GET("/calls")
    void getCallLog(@QueryMap Map<String, String> options, Callback<Calls> cb);

    @GET("/competitorsAd")
    void getAdImageURL(@Query("company_id") String company_id, Callback<StringResponse> cb);

    @GET("/parentNodes")
    void getParentNodes(Callback<ParentNode[]> cb);

    @GET("/instructionTree")
    void getInstructionTree(@QueryMap Map<String, Integer> options, Callback<InstructionTree> cb);

    @POST("/addJSONtree")  //One-time use for converting java trees to JSON
    void addJSONtree(@Body Node node, Callback<StringResponse> cb);
}