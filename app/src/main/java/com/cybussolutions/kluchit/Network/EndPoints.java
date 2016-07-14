package com.cybussolutions.kluchit.Network;

/**
 * Created by Hamza Android on 5/16/2016.
 */
public class EndPoints
{
    public static final String BASE_URL = "http://demo.cybussolutions.com/kluchitrm/";
    public static final String INSERT_TOKEN = BASE_URL + "user/insertDeviceToken";
    public static final String LOGIN = BASE_URL + "user";
    public static final String GET_ALL_JOBS = BASE_URL + "common_controller/getAllJobs";
    public static final String GET_CATAGORY = BASE_URL + "user/getUserCategory";
    public static final String SEND_RESPONCE = BASE_URL+"user/getUserQuestions";
    public static final String SEND_ANSWERS = BASE_URL+"user/getUserQuestionAnswers";
    public static final String GET_JOB_DETAILS = BASE_URL+"common_controller/getUserJobDataById";
    public static final String CLOSE_JOB = BASE_URL+"common_controller/closeJob";
    public static final String GET_POST_QUESTIONS = BASE_URL+"user/getUserPostQuestions";
    public static final String FB_PROFILE_PIC_PATH= "http://demo.cybussolutions.com/kluchitrm/uploads/";
}
