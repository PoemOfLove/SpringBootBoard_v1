package com.mysite.sbb.kakao;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private KakaoAPI kakaoAPI;

//    @RequestMapping(value="/")
//    public String index() {
//
//        return "index";
//    }


    
    
    
    @RequestMapping(value="/kakao_login")
    public String login(@RequestParam(value="code", required = false) String code, HttpSession session) {
        String access_Token = kakaoAPI.getAccessToken(code);
        HashMap<String, Object> userInfo = kakaoAPI.getUserInfo(access_Token);
        System.out.println("login Controller : " + userInfo);

        //    클라이언트의 이메일이 존재할 때 세션에 해당 이메일과 토큰 등록
        if (userInfo.get("email") != null) {
            session.setAttribute("userId", userInfo.get("email"));
            session.setAttribute("access_Token", access_Token);
        }
        

        return "kakao_index";
    }
    
    
    @RequestMapping(value="/kakao_logout")
    public String logout(HttpSession session) {
        kakaoAPI.kakaoLogout((String)session.getAttribute("access_Token"));
        session.removeAttribute("access_Token");
        session.removeAttribute("userId");
        return "kakao_logout";
    }

    
//    @RequestMapping(value = "/kakao_login")
//    public String home(@RequestParam(value = "code", required = false) String code) throws Exception{
//        System.out.println("#########" + code);
//        String access_Token = kakaoAPI.getAccessToken(code);
//        HashMap<String, Object> userInfo = kakaoAPI.getUserInfo(access_Token);
//        System.out.println("###access_Token#### : " + access_Token);
//        System.out.println("###userInfo#### : " + userInfo.get("email"));
//        System.out.println("###nickname#### : " + userInfo.get("nickname"));
//        System.out.println("###profile_image#### : " + userInfo.get("profile_image"));
//        return "kakao_index";
//    }
//    
    
    
    
    
    
 
//    @RequestMapping(value="/kakao_logout")
//    public String logout(HttpSession session) {
//        String access_Token = (String)session.getAttribute("access_Token");
//
//        if(access_Token != null && !access_Token.equals("")){
//            kakaoAPI.kakaoLogout(access_Token);
//            session.removeAttribute("access_Token");
//            session.removeAttribute("userId");
//            session.invalidate();
//        }else{
//            System.out.println("access_Token is null");
//            //return "redirect:/";
//        }
//        //return "index";
//        System.out.println("로그아웃 되엇씁니다.");
//        return "redirect:question/list";
//    }

    
    
}