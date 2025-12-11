//package com.nesterukia.blog.unit.config;
//
//import com.nesterukia.blog.repository.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import static org.mockito.Mockito.mock;
//
//@Configuration
//@ComponentScan(basePackages = {"com.nesterukia.blog"})
//public class UnitTestConfiguration {
//
//    @Bean
//    @Primary
//    public CommentRepository mockCommentRepository() {
//        return mock(CommentRepository.class);
//    }
//
//    @Bean
//    @Primary
//    public ImageRepository mockImageRepository() {
//        return mock(ImageRepository.class);
//    }
//
//    @Bean
//    @Primary
//    public PostRepository mockPostRepository() {
//        return mock(PostRepository.class);
//    }
//
//    @Bean
//    @Primary
//    public PostTagRepository mockPostTagRepository() {
//        return mock(PostTagRepository.class);
//    }
//
//    @Bean
//    @Primary
//    public TagRepository mockTagRepository() {
//        return mock(TagRepository.class);
//    }
//}
