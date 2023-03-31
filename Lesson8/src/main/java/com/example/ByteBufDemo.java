package com.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;


public class ByteBufDemo {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes("hello".getBytes());

        // 切片
        ByteBuf sliceBuf = buf.slice(1, 3);//从索引1开始，长度为3的切片

        // 输出切片内容
        byte[] bytes = new byte[sliceBuf.readableBytes()];
        sliceBuf.readBytes(bytes);
        System.out.println(new String(bytes)); // 输出 "ell"

        // 修改原始ByteBuf的内容
        buf.setByte(1, 'E');

        // 输出切片内容
        bytes = new byte[sliceBuf.readableBytes()];
        sliceBuf.readBytes(bytes);
        System.out.println(new String(bytes)); // 输出 "Ell"


    }
}
