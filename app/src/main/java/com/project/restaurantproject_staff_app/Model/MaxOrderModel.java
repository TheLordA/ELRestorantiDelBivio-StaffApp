package com.project.restaurantproject_staff_app.Model;

import java.util.List;

public class MaxOrderModel {

    private boolean success ;
    private List<MaxOrder> result;
    private String message ;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MaxOrder> getResult() {
        return result;
    }

    public void setResult(List<MaxOrder> result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
