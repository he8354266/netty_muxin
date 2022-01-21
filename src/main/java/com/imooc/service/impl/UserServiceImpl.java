package com.imooc.service.impl;/**
 * @Title: project
 * @Package * @Description:     * @author CodingSir
 * @date 2022/1/1817:41
 */

import com.imooc.enums.MsgSignFlagEnum;
import com.imooc.mapper.*;

import com.imooc.netty.ChatMsg;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.FriendRequestVO;
import com.imooc.pojo.vo.MyFriendsVO;
import com.imooc.service.UserService;
import com.imooc.utils.FastDFSClient;
import com.imooc.utils.FileUtils;
import com.imooc.utils.QRCodeUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zkjy
 * @version 1.0
 * @description zkjy
 * @updateUser
 * @createDate 2022/1/18 17:41
 * @updateDate 2022/1/18 17:41
 **/
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UsersMapper usersMapper;
    @Resource
    private UsersMapperCustom usersMapperCustom;

    @Resource
    private MyFriendsMapper myFriendsMapper;

    @Resource
    private FriendsRequestMapper friendsRequestMapper;

    @Resource
    private ChatMsgMapper chatMsgMapper;
    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);
        Users users = usersMapper.selectOne(user);
        return Objects.isNull(users) ? false : true;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example userExanmple = new Example(Users.class);
        Example.Criteria criteria = userExanmple.createCriteria();

        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);
        Users users = usersMapper.selectOneByExample(userExanmple);
        return users;
    }

    @Override
    public Users saveUser(Users user) {
        String userId = sid.nextShort();
        String qrCodePath = "E://user" + userId + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "muxin_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);

        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setQrcode(qrCodeUrl);
        user.setId(userId);
        return user;
    }

    @Override
    public Users updateUserInfo(Users user) {
        return null;
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        return null;
    }

    @Override
    public Users queryUserInfoByUsername(String username) {
        return null;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

    }

    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return null;
    }

    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {

    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {

    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return null;
    }


    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.imooc.pojo.ChatMsg msgDB = new com.imooc.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);

        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {

    }

    @Override
    public List<com.imooc.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        return null;
    }
}
