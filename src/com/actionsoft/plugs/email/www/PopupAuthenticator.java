package com.actionsoft.plugs.email.www;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

class PopupAuthenticator extends Authenticator {
    private String username;
    private String pwd;

    public PopupAuthenticator(String username, String pwd) {
	this.username = username;
	this.pwd = pwd;
    }

    public PasswordAuthentication getPasswordAuthentication() {
	return new PasswordAuthentication(this.username, this.pwd);
    }
}