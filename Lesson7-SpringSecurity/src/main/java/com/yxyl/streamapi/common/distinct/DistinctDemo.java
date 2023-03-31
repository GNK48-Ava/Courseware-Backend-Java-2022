package com.yxyl.streamapi.common.distinct;

import com.yxyl.springboot.streamapi.Author;

import java.util.List;

import static com.yxyl.springboot.streamapi.StreamDemo.getAuthors;

public class DistinctDemo {
    public static void main(String[] args) {
        List<Author> authors = getAuthors();
        authors.stream()
                .distinct()
                .forEach(author -> System.out.println(author.getName()));
        //注意：distinct方法是依赖Object的equals方法来判断是否是相同对象的。所以需要注意重写equals方法。
    }
}
