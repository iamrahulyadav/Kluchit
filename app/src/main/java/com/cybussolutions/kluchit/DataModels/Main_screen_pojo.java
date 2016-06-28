package com.cybussolutions.kluchit.DataModels;

/**
 * Created by Hamza Android on 5/11/2016.
 */
public class Main_screen_pojo {

    String Maintxt,Discription,Catagory;

    String job_id;

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public  Main_screen_pojo()
    {

    }

    public Main_screen_pojo(String catagory, String discription, String maintxt) {
        Catagory = catagory;
        Discription = discription;
        Maintxt = maintxt;
    }

    public String getMaintxt() {
        return Maintxt;
    }

    public void setMaintxt(String maintxt) {
        Maintxt = maintxt;
    }

    public String getCatagory() {
        return Catagory;
    }

    public void setCatagory(String catagory) {
        Catagory = catagory;
    }

    public String getDiscription() {
        return Discription;
    }

    public void setDiscription(String discription) {
        Discription = discription;
    }
}
