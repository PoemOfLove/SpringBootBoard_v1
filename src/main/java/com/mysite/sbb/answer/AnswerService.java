package com.mysite.sbb.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;


    public Answer create(Question question, String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(author);
        this.answerRepository.save(answer);
        return answer;
    }
    
    public Answer getAnswer(Integer id) {	//답변 아이디로 답변을 조회하는 getAnswer 메서드
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    public void modify(Answer answer, String content) {	//답변의 내용으로 답변을 수정하는 modify 메서드
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }
    
    public void delete(Answer answer) {		//입력으로 받은 Answer 객체를 사용하여 답변을 삭제하는 delete 메서드
        this.answerRepository.delete(answer);
    }
    
    public void vote(Answer answer, SiteUser siteUser) {	//Answer 엔티티에 사용자를 추천인으로 저장하는 vote 메서드
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }
    
    /*페이징*/
    public Page<Answer> getList(Question question, int page){
		/*
		 * List<Sort.Order> sorts = new ArrayList<>();
		 * sorts.add(Sort.Order.desc("createDate"));
		 */

        Pageable pageable = PageRequest.of(page, 5);
        return this.answerRepository.findAllByQuestion(question, pageable);
    }
}