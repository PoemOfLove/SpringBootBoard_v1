package com.mysite.sbb.question;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/question")	//URL prefix
@RequiredArgsConstructor //@Getter, @Setter가 자동으로 Getter, Setter 메서드를 생성하는 것과 마찬가지로 @RequiredArgsConstructor는 final이 붙은 속성에 자동으로 생성자를 생성
@Controller
public class QuestionController {

	private final QuestionService questionService;
	private final UserService userService;
	private final QuestionPageRepository questionPageRepository;
	private final AnswerService answerService;
	
    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page, 
    		@RequestParam(value = "kw", defaultValue = "") String kw) {	//검색어에 해당하는 kw 파라미터를 추가했고 디폴트값으로 빈 문자열을 설정
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);	//화면에서 입력한 검색어를 화면에 유지하기 위해 model.addAttribute("kw", kw)로 kw 값을 저장
        return "question_list";
    }
    
    //질문상세
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm, @AuthenticationPrincipal SiteUser siteUser, Principal principal,
            @RequestParam(value="page", defaultValue="0") int page) {
    	
    
    	
    	/*댓글페이징*/
    	Question question = this.questionService.getQuestion(id);	//QuestionService의 getQuestion 메서드를 호출하여 Question 객체를 템플릿에 전달
        
        Page<Answer> paging = this.answerService.getList(question, page);

    	
    	model.addAttribute("question", question);
        model.addAttribute("paging", paging);
        
        /*이전글다음글번호와 제목을 html에서 불러올수있게 model.addAttribute() 작성*/
        QuestionPage questionPage = questionPageRepository.findByPages(id);
        model.addAttribute("prevID", questionPage.getPREVID());
        model.addAttribute("prevSub", questionPage.getPREV_SUB());
        model.addAttribute("nextID", questionPage.getNEXTID());
        model.addAttribute("nextSub", questionPage.getNEXT_SUB());
        
        return "question_detail";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal, MultipartFile file) throws Exception{
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        
        SiteUser siteUser = this.userService.getUser(principal.getName());
    	this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, questionForm.getCategory(), file);
    	
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, @AuthenticationPrincipal SiteUser siteUser) {
        Question question = this.questionService.getQuestion(id);
        
//        if(siteUser.getRole() == "ROLE_ADMIN") {
//        	return "question_form";
//        }
        
        if((!question.getAuthor().getUsername().equals(siteUser.getUsername())) && !(siteUser.getRole().equals("ROLE_ADMIN"))) {	//현재 로그인한 사용자와 질문의 작성자가 동일하지 않을 경우에는 "수정권한이 없습니다." 오류가 발생
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        /*수정할 질문의 제목과 내용을 보여줌*/
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        /*수정할 카테고리 보여줌*/
        questionForm.setCategory(question.getCategory());
        return "question_form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, 	//POST 형식의 /question/modify/{id} 요청을 처리하기 위해 questionModify 메서드를 추가
    		@AuthenticationPrincipal SiteUser siteUser, @PathVariable("id") Integer id, MultipartFile file) throws Exception {	// questionForm의 데이터를 검증
        if (bindingResult.hasErrors()) {	
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if ((!question.getAuthor().getUsername().equals(siteUser.getUsername()))  && !(siteUser.getRole().equals("ROLE_ADMIN"))) {	//로그인한 사용자와 수정하려는 질문의 작성자가 동일한지도 검증
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), questionForm.getCategory(), file);	// QuestionService에서 작성한 modify 메서드를 호출하여 질문 데이터를 수정
        return String.format("redirect:/question/detail/%s", id);	//수정이 완료되면 질문 상세 화면을 다시 호출한다.
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(
    		@AuthenticationPrincipal SiteUser siteUser, @PathVariable("id") Integer id)  { //URL로 전달받은 id값을 사용하여 Question 데이터를 조회한후

        //로그인한 사용자와 질문 작성자가 동일할 경우 위에서 작성한 서비스의 delete 메서드로 질문을 삭제한다
        Question question = this.questionService.getQuestion(id);
        if ((!question.getAuthor().getUsername().equals(siteUser.getUsername())) && !(siteUser.getRole().equals("ROLE_ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);						//QuestionService의 vote 메서드를 호출하여 추천인을 저장
        return String.format("redirect:/question/detail/%s", id);
    }
    

}