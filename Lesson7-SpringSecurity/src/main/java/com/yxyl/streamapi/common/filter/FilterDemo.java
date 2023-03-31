package com.yxyl.streamapi.common.filter;

import com.yxyl.springboot.streamapi.Author;

import java.util.List;

import static com.yxyl.springboot.streamapi.StreamDemo.getAuthors;

public class FilterDemo {
    public static void main(String[] args) {
        List<Author> authors = getAuthors();
        authors.stream()
                .filter(author -> author.getName().length()>1)
                .forEach(author -> System.out.println(author.getName()));
    }
    
    
    
}
