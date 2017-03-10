package com.example.vmac.WatBot;

import org.json.JSONObject;

/**
 * Created by norton on 3/7/17.
 */

public class Jobs {

    private String policyNumber;
    private String startDate;
    private String endDate;
    private String typeOfPolicy;

    public Jobs() {
       super();
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTypeOfPolicy() {
        return typeOfPolicy;
    }

    public void setTypeOfPolicy(String typeOfPolicy) {
        this.typeOfPolicy = typeOfPolicy;
    }

    public JSONObject getJson(){
        StringBuilder jsonData = new StringBuilder();
        jsonData.append("{");
        jsonData.append("\"printername\" : \"").append(policyNumber).append("\"").append(",");
        jsonData.append("\"repairtype\" : \"").append(startDate).append("\"").append(",");
        jsonData.append("\"errordescription\" : \"").append(endDate).append("\"");
        jsonData.append("\"address\" : \"").append(typeOfPolicy).append("\"");
        jsonData.append("}");
        JSONObject jsonObject = new JSONObject();
        return  jsonObject;
    }
}
