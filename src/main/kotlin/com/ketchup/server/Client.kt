package com.ketchup.server

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.CharsetUtil
import java.net.InetSocketAddress
import java.nio.charset.Charset

fun main(args: Array<String>) {
    Client.run("localhost", 8080)
}

object Client {
    fun run(host: String, port: Int) {
        val group = NioEventLoopGroup()
        try {
            val future = Bootstrap()
                .group(group)
                .channel(NioSocketChannel::class.java)
                .remoteAddress(InetSocketAddress(host, port))
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline().addLast(EchoClientHandler())
                    }
                })
                .connect()
                .sync()

            future.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully().sync()
        }
    }
}

@Sharable
class EchoClientHandler : SimpleChannelInboundHandler<ByteBuf>() {

    companion object {
        val charset: Charset = CharsetUtil.UTF_8;
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello! World!", charset))
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        println("Client received: ${msg.toString(charset)}")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}