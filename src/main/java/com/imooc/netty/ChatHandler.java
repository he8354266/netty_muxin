package com.imooc.netty;/**
 * @Title: project
 * @Package * @Description:     * @author CodingSir
 * @date 2022/1/1410:04
 */

import com.imooc.SpringUtil;
import com.imooc.enums.MsgActionEnum;
import com.imooc.service.UserService;
import com.imooc.service.impl.UserServiceImpl;
import com.imooc.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * @author zkjy
 * @version 1.0
 * @description zkjy
 * @updateUser
 * @createDate 2022/1/14 10:04
 * @updateDate 2022/1/14 10:04
 **/
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    //用于记录和管理所有的客户端channle
    public static ChannelGroup users =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //获取客户端传过来的消息
        String content = msg.text();
        System.out.println(content);
        Channel currentChannel = ctx.channel();
        //获取客户端发送的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        //判断消息类型，根据不同的类型处理不同的业务
        //senderId 1478266085121236992
        //receiverId 1478266085297397760
        //{"action":2,"chatMsg":{"senderId":"1478266085121236992","receiverId":"1478266085297397760","msg":"你好"}}
        if (action == MsgActionEnum.CONNECT.type) {
            // 	2.1  当websocket 第一次open的时候，初始化channel，把用的channel和userid关联起来
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);
            //测试
            for (Channel user : users) {
                user.writeAndFlush(
                        new TextWebSocketFrame("欢迎" + senderId + "进入聊天室")
                );
                System.out.println(user.id().asLongText());
            }
            UserChannelRel.output();
        } else if (action == MsgActionEnum.CHAT.type) {
            String senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, currentChannel);

            for (Channel user : users) {
                System.out.println("用户=========" + user.id().asLongText());
            }
            //  2.2  聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();

            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl", UserServiceImpl.class);
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);


            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            //发送消息
            //从全局用户channel关系中获取接收方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel == null) {
                //离线推送
            } else {
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    //用户在线
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame("用户" + senderId + ":" + dataContentMsg.getChatMsg().getMsg())
                    );
                }
            }
        } else if (action == MsgActionEnum.SIGNED.type) {
            //  2.3  签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl", UserServiceImpl.class);
            // 扩展字段在signed类型的消息中，代表需要去签收的消息id，逗号间隔
            String msgIdsStr = dataContent.getExtand();

            List<String> msgIdList = Arrays.asList(msgIdsStr.split(","));
            System.out.println(msgIdList);
            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                userService.updateMsgSigned(msgIdList);
            }
        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            //  2.4  心跳类型的消息
            System.out.println("收到来自channel为[" + currentChannel + "]的心跳包...");
        }

    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channle，并且放到ChannelGroup中去进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asShortText();
        System.out.println("客户端被移除，channelId为：" + channelId);
        // 当触发handlerRemoved，ChannelGroup会自动移除对应客户端的channel
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭连接（关闭channel），随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
