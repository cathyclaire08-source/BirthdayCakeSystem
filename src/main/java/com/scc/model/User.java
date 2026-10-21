package com.scc.model;

public record User(int id, String username, String password, String roles, String account_status, String full_name, String contact_number) {

    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s", id, username, roles, account_status, full_name, contact_number);

    }
}