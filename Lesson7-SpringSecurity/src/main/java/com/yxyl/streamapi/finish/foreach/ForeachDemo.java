package com.yxyl.streamapi.finish.foreach;

import com.yxyl.springboot.streamapi.Author;

import java.util.List;

import static com.yxyl.springboot.streamapi.StreamDemo.getAuthors;

public class ForeachDemo {
    public static void main(String[] args) {
        //输出所有作家的名字
        List<Author> authors = getAuthors();

        authors.stream()
                .map(author -> author.getName())
                .distinct()
                .forEach(name-> System.out.println(name));
    }
}
