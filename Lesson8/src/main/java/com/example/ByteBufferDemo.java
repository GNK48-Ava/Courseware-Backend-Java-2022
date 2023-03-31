package com.example;

import java.nio.ByteBuffer;


public class ByteBufferDemo {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);

        buffer.flip();//切换至读模式

        System.out.println(buffer.get());
        System.out.println(buffer.get());
        buffer.compact();
        buffer.put((byte) 4);
        buffer.flip();
        buffer.mark();

        System.out.println(buffer.get());
        System.out.println(buffer.get());
        System.out.println(buffer.get());
    }
}
