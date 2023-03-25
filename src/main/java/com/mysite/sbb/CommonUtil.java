package com.mysite.sbb;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

@Component		//@Component로 CommonUtil 클래스는 스프링부트에 의해 관리되는 빈(bean, 자바객체)으로 등록
				//이렇게 빈(bean)으로 등록된 컴포넌트는 템플릿에서 바로 사용할 수 있다.
public class CommonUtil {
    public String markdown(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}