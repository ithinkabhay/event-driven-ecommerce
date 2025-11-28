package com.ecommerce.service;

import com.ecommerce.dto.UserResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;

public interface UserService {

    public void register(RegisterRequest request);

    public UserResponse login(LoginRequest request);

}
