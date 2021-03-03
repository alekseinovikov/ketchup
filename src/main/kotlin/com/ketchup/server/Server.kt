package com.ketchup.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress
import java.nio.charset.Charset

fun main(args: Array<String>) {
    Server.run(8080)
}

object Server {
    fun run(port: Int) {
        val echoHandler = EchoServerHandler()
        val group = NioEventLoopGroup()

        try {
            val future = ServerBootstrap().group(group)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(InetSocketAddress(port))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(echoHandler)
                    }
                })
                .bind()
                .sync()

            future.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }
}

@Sharable
class EchoServerHandler : ChannelInboundHandlerAdapter() {

    companion object {
        val charset: Charset = CharsetUtil.UTF_8;
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        (msg as ByteBuf).let {
            println("Server received: ${it.toString(charset)}")
            ctx.write(it)
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx
            .writeAndFlush(Unpooled.EMPTY_BUFFER)
            .addListener(ChannelFutureListener.CLOSE)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}