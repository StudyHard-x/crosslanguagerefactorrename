package com.mcs.mcsproject.config;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
public class SwaggerConfiguration {
    @Bean
    public Docket docket(){
        Docket docket = new Docket(DocumentationType.OAS_30 );
        ApiInfo apiInfo = new ApiInfoBuilder().
                contact(new Contact("12","13","14")).
                build();
        docket.apiInfo(apiInfo);

        docket.select()
                .apis(RequestHandlerSelectors.basePackage("com.mcs.mcsproject.controller"));

        return docket;
    }


//    private ApiInfo apiInfo(){
//        //作者信息
//        Contact contact =new Contact("xch","http://test.com","2750443x@student.gla.ac.uk");
//
//        return new ApiInfo("cross rename api doc ",
//                "desc",
//                "1.0",
//                "urn:tos",
//                contact, "Apache 2.0",
//                "http://www.apache.org/licenses/LICENSE-2.0",
//                new ArrayList());
//    }

}
