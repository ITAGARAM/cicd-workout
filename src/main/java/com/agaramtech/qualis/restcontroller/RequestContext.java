package com.agaramtech.qualis.restcontroller;

import org.springframework.web.context.annotation.RequestScope;

import com.agaramtech.qualis.global.UserInfo;

import jakarta.inject.Named;

/**
 * This class is used to hold the specific details of the user request that is received for every request
 * at the rest controller.
 */
@Named
@RequestScope
public class RequestContext {
	

	/**
	 * Default Constructor
	 */
	public RequestContext() {
		super();
		// TODO Auto-generated constructor stub
	}

	private UserInfo userInfo;

	/**
	 * This returns the logged in user details.
	 * @return UserInfo 
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}

	/**
	 * This is used to assign the logged in user details.
	 * @param userInfo UserInfo
	 */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}
