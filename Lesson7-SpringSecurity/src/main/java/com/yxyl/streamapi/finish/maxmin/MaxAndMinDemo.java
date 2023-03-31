package com.yxyl.streamapi.finish.maxmin;

import com.yxyl.springboot.streamapi.Author;

import java.util.List;
import java.util.Optional;

import static com.yxyl.springboot.streamapi.StreamDemo.getAuthors;

public class MaxAndMinDemo {
    public static void main(String[] args) {
        //分别获取这些作家的所出书籍的最高分和最低分并打印。
        //Stream<Author>  -> Stream<Book> ->Stream<Integer>  ->求值

        List<Author> authors = getAuthors();
        Optional<Integer> max = authors.stream()
                .flatMap(author -> author.getBooks().stream())
                .map(book -> book.getScore())
                .max((score1, score2) -> score1 - score2);

        Optional<Integer> min = authors.stream()
                .flatMap(author -> author.getBooks().stream())
                .map(book -> book.getScore())
                .min((score1, score2) -> score1 - score2);
        System.out.println("max.get() = " + max.get());
        System.out.println("min.get() = " + min.get());
    }
}
