package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.nio.charset.Charset;

public class ExecutorGroupApplication {

    private final int port;

    public ExecutorGroupApplication(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        final ByteBuf buf = Unpooled.copiedBuffer("Hi!\r\n", Charset.defaultCharset());

        final DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(10);
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(executorGroup, new SimpleChannelInboundHandler<ByteBuf>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                            // #eventLoopGroup
                                            // Thread[nioEventLoopGroup-3-2,10,main]
                                            // io.netty.channel.nio.NioEventLoop@f0f2775

                                            // #executorGroup
                                            // Thread[defaultEventExecutorGroup-2-1,5,main]
                                            // io.netty.util.concurrent.DefaultEventExecutor@1e4a7dd4
                                            System.out.println(Thread.currentThread());
                                            System.out.println(ctx.executor());
                                            ctx.writeAndFlush(buf.duplicate());
                                        }
                                    });
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new ExecutorGroupApplication(port).run();
    }
}
