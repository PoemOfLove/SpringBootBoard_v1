package com.mysite.sbb.question;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table

public class QuestionPage {	//이전페이지와 다음페이지의 데이터를 담을 엔티티
    @Id
    private Integer id;
    private String PREVID;
    private String PREV_SUB;
    private String NEXTID;
    private String NEXT_SUB;
}