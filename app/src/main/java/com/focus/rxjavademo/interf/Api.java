package com.focus.rxjavademo.interf;

import com.focus.rxjavademo.entity.LoginRequest;
import com.focus.rxjavademo.entity.LoginResponse;
import com.focus.rxjavademo.entity.RegisterRequest;
import com.focus.rxjavademo.entity.RegisterResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;

/**
 * Created by focus on 2017/10/15.
 */

public interface Api {
    @GET
    Observable<LoginResponse> login(@Body LoginRequest request);

    @GET
    Observable<RegisterResponse> register(@Body RegisterRequest request);

}
