package com.SrivatsanPoddar.helpp;

public class SurveyQuestion {
	
	private String question_type;  //Could be "multiple_choice" for Multiple-Choice, "true_false", or "short_response"
	private String display_text;
	private String[] options;
	private String question_id;
	private String response;
	private String device_id;
	
	public SurveyQuestion(String myQuestionType, String myDisplayText, String[] myOptions, String myQuestionID) {
		question_type = myQuestionType;
		display_text = myDisplayText;
		options = myOptions;
		question_id = myQuestionID;
	}
	
	public String getQuestionType() {
		return question_type;
	}
	
	public String getDisplayText() {
		return display_text;
	}
	
	public String[] getOptions() {
		return options;
	}
	
	public void setResponse(String myResponse) {
		response = myResponse;
	}
	
	public void setDeviceID(String myDeviceId) {
	    device_id = myDeviceId;
	}
}
