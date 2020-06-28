package com.oppo.test.jacocoreport.utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
    @Async("AsyncTaskConfig")
    public String dosomething(String message){
//      try{
//
//      }catch (InterruptedException e){
//
//      }
      return message;
    }
}
