package com.mysite.sbb;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorHandler implements ErrorController {

    // @Override
    // public String getErrorPath() {
    //     return null;
    // }
    //SpringBoot 2.3.x부터 이 방법을 사용하지 않음
	
    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        // error로 들어온 에러의 status를 불러온다 (ex:404,500,403...)
        //기본적으로 제일 많이 사용되는 404에러는 404에러페이지로, 이외 나머지 에러는 기본 에러페이지로 이동하도록 했다.
        
        if(status != null){
            int statusCode = Integer.valueOf(status.toString());	
            
            //ValueOf() 메서드는 파라미터로 숫자로 변환할 문자열을 입력받고, 참조형인 new Integer(정수)로 변환
            //toString메서드 객체가 가지고 있는 정보나 값들을 문자열로 만들어 리턴하는 메소드
            
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error_404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error_500";
            }
        }
        return "error";
    }
}