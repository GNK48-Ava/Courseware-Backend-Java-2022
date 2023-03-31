package com.yxyl.streamapi.common.map;

import com.yxyl.springboot.streamapi.Author;

import java.util.List;

import static com.yxyl.springboot.streamapi.StreamDemo.getAuthors;

public class MapDemo {
    public static void main(String[] args) {
        List<Author> authors = getAuthors();

        authors
                .stream()
                .map(author -> author.getName())
                .forEach(name->System.out.println(name));
    }

    
}
