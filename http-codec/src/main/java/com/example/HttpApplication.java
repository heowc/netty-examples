package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpApplication {

    private final int port;

    public HttpApplication(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        public void initChannel(Channel ch) {
                            final ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                            pipeline.addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {
                                    System.out.println(request);

                                    final String message = "Hi, First Http Application By Netty!";
                                    final ByteBuf content = Unpooled.copiedBuffer(message, Charset.defaultCharset());

                                    DefaultFullHttpResponse response =
                                            new DefaultFullHttpResponse(HTTP_1_1, OK, content);

                                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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

        new HttpApplication(port).run();
    }
}
