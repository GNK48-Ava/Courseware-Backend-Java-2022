package com.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;


public class FileChannelDemo {
    public static void main(String[] args) throws IOException {

        FileChannel from = new FileOutputStream("hello.txt").getChannel();
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode("hello");//获取一个ByteBuffer
        from.write(byteBuffer);


        FileChannel to = new FileInputStream("hello.txt").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(16);
        to.read(buffer);
        buffer.flip();//切换至读模式

        while (buffer.hasRemaining()) {//当还有未读的元素
            System.out.print((char) buffer.get());
        }

        from.close();
        to.close();
    }
}
