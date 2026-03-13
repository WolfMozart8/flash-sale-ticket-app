package com.wolfmozart.flash_sale.dto;

import java.time.LocalDateTime;

public class ApiError {

    private int code;
    private String codeName;
    private String message;
    private String route;
    private final LocalDateTime timeStamp;

    public ApiError(int code,String codeName, String message, String route) {
        this.code = code;
        this.codeName = codeName;
        this.message = message;
        this.route = route;
        this.timeStamp = LocalDateTime.now();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }
}
