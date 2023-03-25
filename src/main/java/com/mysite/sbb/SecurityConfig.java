package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mysite.sbb.user.UserSecurityService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity	//모든 요청들이 필터를 걸친다
@EnableMethodSecurity(prePostEnabled = true) //@PreAuthorize 애너테이션이 동작할수 있게 한다.
@RequiredArgsConstructor
public class SecurityConfig {
//    private final UserDetailsService loginService;
	private final UserSecurityService userSecurityService;
    


	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	
//    	http.csrf().disable();
        http.authorizeHttpRequests()
        		
                .requestMatchers("/siteuser/**").authenticated()
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                
                .and()
                .formLogin()
                .loginPage("/user/login")
                .defaultSuccessUrl("/")


                
    			.and()
    			.logout()
    			.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
    			.logoutSuccessUrl("/")
    			.invalidateHttpSession(true)
    			.and()
                .exceptionHandling()
                .accessDeniedPage("/accessDenied");

        
    	
    	
        
        
        return http.build();
        
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    


    
}