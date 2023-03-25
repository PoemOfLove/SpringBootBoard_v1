package com.mysite.sbb.question;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionPageRepository questionPageRepository;
    
    private Specification<Question> search(String kw) {	// 검색어(kw)를 입력받아 쿼리의 조인문과 where문을 생성하여 리턴하는 search메서드
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {	//q = Root, 즉 기준을 의미하는 Question 엔티티의 객체 (질문 제목과 내용을 검색하기 위해 필요)
                query.distinct(true);  // 중복을 제거 
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);	//Question 엔티티와 SiteUser 엔티티를 아우터 조인(JoinType.LEFT)하여 만든 SiteUser 엔티티의 객체
                																//Question 엔티티와 SiteUser 엔티티는 author 속성으로 연결되어 있기 때문에 q.join("author")와 같이 조인해야 한다.
                																//(질문 작성자를 검색하기 위해 필요)
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);	//Question 엔티티와 Answer 엔티티를 아우터 조인하여 만든 Answer 엔티티의 객체.
                																//Question 엔티티와 Answer 엔티티는 answerList 속성으로 연결되어 있기 때문에 q.join("answerList")와 같이 조인해야 한다.
                																//(답변 내용을 검색하기 위해 필요)
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);	//바로 위에서 작성한 a 객체와 다시 한번 SiteUser 엔티티와 아우터 조인하여 만든 SiteUser 엔티티의 객체 (답변 작성자를 검색하기 위해서 필요)
                
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목 
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용 
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자 
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용 
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자 
                
                //검색어(kw)가 포함되어 있는지를 like로 검색하기 위해 제목, 내용, 질문 작성자, 답변 내용, 답변 작성자 각각에 cb.like를 사용하고 최종적으로 cb.or로 OR 검색되게 하였다.
            }
        };
    }
    
    public Page<Question> getList(int page, String kw) {	//매개변수 page, kw
    	List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }
    
    public Question getQuestion(Integer id) {	//id 값으로 Question 데이터를 조회하는 getQuestion 메서드
        Optional<Question> question = this.questionRepository.findById(id);	//리포지터리로 얻은 Question 객체는 Optional 객체
        if (question.isPresent()) {	//때문에 isPresent 메서드로 해당 데이터가 존재하는지 검사
        	
        	//조회수
            Question question1 = question.get();
            question1.setCountview(question1.getCountview() + 1);	//setCountview로 조회수컬럼에 값을 변경
            this.questionRepository.save(question1);	//question1은 값을 저장하고
            return question1;	// 다시리턴하여 기존값에서 +1 하여 조회수를 늘려나감
            //조회수끝
            
            
        } else {
            throw new DataNotFoundException("question not found");
        }
    }
    
    
    //파일저장할위치
    @Value("${ImgLocation}")
    private String imgLocation;
     
    /*질문데이터를 저장하는 create메서드*/
    public void create(String subject, String content, SiteUser user, String category, MultipartFile file) throws Exception{ //매개 변수값
    	
    	
    	String projectPath = imgLocation; //파일저장위치 = projectPath

        UUID uuid = UUID.randomUUID(); //식별자.랜덤으로 이름만들어줌
        String fileName = uuid + "_" + file.getOriginalFilename(); //저장될파일이름지정=랜덤식별자_원래파일이름
        File saveFile = new File(projectPath, fileName); //빈껍데기생성 이름은 fileName, projectPath라는 경로에담김
        file.transferTo(saveFile); 
        
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        /*카테고리추가*/
        q.setCategory(category);
        
        q.setFilename(fileName); //파일이름
        q.setFilepath(projectPath + fileName); //저장경로,파일이름
        this.questionRepository.save(q);
    }
    
    /*질문데이터 수정*/
    public void modify(Question question, String subject, String content, String category)  {		//질문 데이터를 수정할수 있는 modify 메서드를 추가

    	
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        question.setCategory(category); //카테고리수정
        
        
        this.questionRepository.save(question);
        
    
    }
    
    public Question modify(Question question, String subject, String content, String category, MultipartFile file) throws Exception {
        String projectPath = imgLocation;

        if (file.getOriginalFilename().equals("")){
            //새 파일이 없을 때
            question.setFilename(question.getFilename());
            question.setFilepath(question.getFilepath());

        } else if (file.getOriginalFilename() != null){
            //새 파일이 있을 때
            File f = new File(question.getFilepath());

            if (f.exists()) { // 파일이 존재하면
                f.delete(); // 파일 삭제
            }

            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + file.getOriginalFilename();
            File saveFile = new File(projectPath, fileName);
            file.transferTo(saveFile);

            question.setFilename(fileName);
            question.setFilepath(projectPath + fileName);
        }

        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        question.setCategory(category);
        
        this.questionRepository.save(question);

        return question;
    }
    
    
    
    
    
    public void delete(Question question) {		//Question 객체를 입력으로 받아 Question 리포지터리를 사용하여 질문 데이터를 삭제
        this.questionRepository.delete(question);
    }
    
    public void vote(Question question, SiteUser siteUser) {	//사용자를 추천인으로 저장하는 vote 메서드
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }
    
    public QuestionPage getQuestionByPageId(Question question){
        //레파지토리에 작성해둔 findByPages 메서드에서 question엔티티의 id를 기준으로 쿼리문을 실행
        return questionPageRepository.findByPages(question.getId());
    }
    
    
}

