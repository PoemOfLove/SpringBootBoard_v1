package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity //JPA가 엔티티로 인식한다
@Table
public class Question {
    @Id //기본 키로 지정한다
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //1씩 자동으로 증가하여 저장된다.
    //strategy는 고유번호를 생성하는 옵션으로
    //GenerationType.IDENTITY는 해당 컬럼만의 독립적인 시퀀스를 생성하여 번호를 증가시킬 때 사용한다.
    private Integer id;

    @Column(length = 200) //컬럼의 길이를 설정할때 사용
    private String subject;

    @Column(columnDefinition = "TEXT") // 컬럼 속성
    private String content;

    private LocalDateTime createDate;
    
    //@Column 애너테이션을 사용하지 않더라도 테이블 컬럼으로 인식한다. 세부설정을 하기위해 @Column을 하는것이다.
    //테이블 컬럼으로 인식하고 싶지 않은 경우에만 @Transient 애너테이션을 사용한다.
    
    
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) // cascade = CascadeType.REMOVE 질문을 삭제하면 그에 달린 답변들도 모두 함께 삭제하기 위해서
    private List<Answer> answerList;
    
    @ManyToOne
    private SiteUser author;
    
    private LocalDateTime modifyDate;	//SiteUser 엔티티를 @ManyToOne으로 적용(여러개의 질문이 한 명의 사용자에게 작성될 수 있으므로)
    
    @ManyToMany		//질문과 추천인은 부모와 자식의 관계가 아니고 대등한 관계이기 때문에 @ManyToMany를 사용
    Set<SiteUser> voter;	//List가 아닌 Set으로 한 이유는 추천인은 중복되면 안되기 때문
    
    @Column(columnDefinition = "integer default 0", nullable = false)
    private int countview; /*조회수*/
    
    private String category; /*카테고리값 저장컬럼*/
    
    private String filepath;/*파일저장경로*/
    private String filename;/*파일이름*/

}