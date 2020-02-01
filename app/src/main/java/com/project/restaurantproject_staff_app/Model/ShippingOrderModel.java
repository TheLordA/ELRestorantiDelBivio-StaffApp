package com.project.restaurantproject_staff_app.Model;

import java.util.List;

public class ShippingOrderModel {
    private boolean success;
    private String message;
    private List<ShippingOrder> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ShippingOrder> getResult() {
        return result;
    }

    public void setResult(List<ShippingOrder> result) {
        this.result = result;
    }
}
