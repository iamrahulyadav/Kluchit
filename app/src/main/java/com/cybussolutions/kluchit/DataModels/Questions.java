package com.cybussolutions.kluchit.DataModels;

/**
 * Created by Hamza Android on 5/19/2016.
 */
public class Questions
{
    String q_Id,q_Txt,cat_type;

    public Questions()
    {

    }
    public Questions(String cat_type, String q_Txt, String q_Id) {
        this.cat_type = cat_type;
        this.q_Txt = q_Txt;
        this.q_Id = q_Id;
    }

    public String getQ_Id() {
        return q_Id;
    }

    public void setQ_Id(String q_Id) {
        this.q_Id = q_Id;
    }

    public String getQ_Txt() {
        return q_Txt;
    }

    public void setQ_Txt(String q_Txt) {
        this.q_Txt = q_Txt;
    }

    public String getCat_type() {
        return cat_type;
    }

    public void setCat_type(String cat_type) {
        this.cat_type = cat_type;
    }
}
