package com.wzc.lrsdemo.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wzc.lrsdemo.R;
import com.wzc.lrsdemo.base.BaseActivity;
import com.wzc.lrsdemo.config.Constants;
import com.wzc.lrsdemo.controller.SimpleController;
import com.wzc.lrsdemo.data.GameChatData;
import com.wzc.lrsdemo.data.RoleData;
import com.wzc.lrsdemo.event.MsgEvent;
import com.wzc.lrsdemo.utils.AppUtil;
import com.wzc.lrsdemo.utils.DateUtil;
import com.wzc.lrsdemo.utils.GlideUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;

/**
 * Created by Administrator on 2017/3/7.
 */

public class RoleView extends RelativeLayout implements View.OnClickListener {
    public static final int STATE_LOCK = 0xa0;//位置锁定；只显示锁定
    public static final int STATE_NOBODY = 0xa1;//位置解锁，无人；显示无人，号码
    public static final int STATE_DIE = 0xa2;//位置解锁，有人，已死亡；显示死亡，号码
    public static final int STATE_UNREADY = 0xa3;//位置解锁，有人，未准备；显示头像，号码，语音
    public static final int STATE_READY = 0xa4;//位置解锁，有人，已准备；显示头像，号码，语音，准备状态
    public static final int STATE_SPEAKING = 0xa5;//位置解锁，有人，正在说话；显示头像，号码，语音

    private BaseActivity activity;
    private View root;
    private RoleData mRoleData;

    private ImageView head_iv;
    private ImageView number_iv;
    private TextView number_tv;
    private ImageView die_iv;
    private ImageView speaking_iv;
    private ImageView ready_iv;
    private ImageView lock_iv;

    //模拟聊天内容
    private String[] arrChat = Constants.ARR_TEXT_PLAYER_CHAT;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!AppUtil.isSafe(activity)) {
//                mHandler = null;
                return;
            }
            if (mRoleData != null) {
                mRoleData.setState(msg.what);
            }
            switch (msg.what) {
                //锁定
                case STATE_LOCK:
                    lock_iv.setVisibility(View.VISIBLE);
                    break;
                //无人
                case STATE_NOBODY:
                    lock_iv.setVisibility(View.GONE);
                    ready_iv.setVisibility(View.GONE);
                    speaking_iv.setVisibility(View.GONE);
                    head_iv.setVisibility(View.VISIBLE);
                    number_iv.setVisibility(View.VISIBLE);
                    number_tv.setVisibility(View.VISIBLE);
                    head_iv.setImageResource(R.mipmap.icon_vacancy);
                    break;
                //游戏中，死亡
                case STATE_DIE:
                    lock_iv.setVisibility(View.GONE);
                    ready_iv.setVisibility(View.GONE);
                    speaking_iv.setVisibility(View.GONE);
                    head_iv.setVisibility(View.VISIBLE);
                    number_iv.setVisibility(View.VISIBLE);
                    number_tv.setVisibility(View.VISIBLE);
                    die_iv.setVisibility(View.VISIBLE);
//                    head_iv.setImageResource(arr_role[number % arr_role.length]);
                    break;
                //玩家未准备
                case STATE_UNREADY:
                    lock_iv.setVisibility(View.GONE);
                    die_iv.setVisibility(View.GONE);
                    speaking_iv.setVisibility(View.GONE);
                    head_iv.setVisibility(View.VISIBLE);
                    number_iv.setVisibility(View.VISIBLE);
                    number_tv.setVisibility(View.VISIBLE);
//                    head_iv.setImageResource(arr_role[number % arr_role.length]);
                    if (mRoleData == null) {
                        return;
                    }
                    GlideUtil.into(activity, mRoleData.getHeadImgUrl(), head_iv, GlideUtil.ROUND);
                    number_tv.setText("" + mRoleData.getNumber());
                    if (mRoleData.isOwner() && !SimpleController.isGameing()) {
                        ready_iv.setVisibility(View.VISIBLE);
                        ready_iv.setImageResource(R.mipmap.icon_room_homeowners);
                    } else {
                        ready_iv.setVisibility(View.GONE);
                    }
                    break;
                //玩家准备
                case STATE_READY:
                    lock_iv.setVisibility(View.GONE);
                    die_iv.setVisibility(View.GONE);
                    speaking_iv.setVisibility(View.GONE);
                    head_iv.setVisibility(View.VISIBLE);
                    number_iv.setVisibility(View.VISIBLE);
                    number_tv.setVisibility(View.VISIBLE);
                    ready_iv.setVisibility(View.VISIBLE);
//                    head_iv.setImageResource(arr_role[number % arr_role.length]);
                    //判断角色是否房主
                    if (mRoleData.isOwner()) {
                        ready_iv.setImageResource(R.mipmap.icon_room_homeowners);
                    } else {
                        ready_iv.setImageResource(R.mipmap.room_ready);
                    }
                    break;
                //游戏中语音
                case STATE_SPEAKING:
                    lock_iv.setVisibility(View.GONE);
                    die_iv.setVisibility(View.GONE);
                    ready_iv.setVisibility(View.GONE);
                    head_iv.setVisibility(View.VISIBLE);
                    number_iv.setVisibility(View.VISIBLE);
                    number_tv.setVisibility(View.VISIBLE);
                    speaking_iv.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    public RoleView(Context context) {
        super(context);
        activity = (BaseActivity) context;
        init();
    }

    private void init() {
        root = LayoutInflater.from(activity).inflate(R.layout.view_role, this);
        root.setOnClickListener(this);

        head_iv = (ImageView) root.findViewById(R.id.role_head_iv);
        number_iv = (ImageView) root.findViewById(R.id.role_number_iv);
        number_tv = (TextView) root.findViewById(R.id.role_number_tv);
        die_iv = (ImageView) root.findViewById(R.id.role_die_iv);
        speaking_iv = (ImageView) root.findViewById(R.id.role_speaking_iv);
        ready_iv = (ImageView) root.findViewById(R.id.role_ready_iv);
        lock_iv = (ImageView) root.findViewById(R.id.role_lock_iv);

    }

    /**
     * 设置玩家信息
     */
    public void setup(RoleData roleData) {
        mRoleData = roleData;
        if (mRoleData != null) {
            mHandler.sendEmptyMessage(STATE_UNREADY);
            //发送消息玩家进场
            GameChatData gameChatData = new GameChatData(GameChatData.ENTRY, DateUtil.nowLongStr(), mRoleData.getNickName(), "", "");
            MsgEvent msgEvent = new MsgEvent(MsgEvent.ROOM_CHAT, null, gameChatData);
            EventBus.getDefault().post(msgEvent);
        } else {
            clear(false);
        }
    }

    public RoleData getmRoleData() {
        return mRoleData;
    }

    /**
     * 玩家离开位置，或者锁定位置
     *
     * @param isLock
     */
    public void clear(boolean isLock) {
        mRoleData = null;
        if (isLock) {
            mHandler.sendEmptyMessage(STATE_LOCK);
        } else {
            mHandler.sendEmptyMessage(STATE_NOBODY);
        }
    }

    @Override
    public void onClick(View v) {
//        if (GameController.isGameing()) {
//            return;
//        }
        if (SimpleController.isGameing()) {
            return;
        }
        if (mRoleData != null) {
            //判断当前是否准备
            if (mRoleData.getState() == STATE_LOCK) {
                mHandler.sendEmptyMessage(STATE_NOBODY);
            } else if (mRoleData.getState() == STATE_NOBODY) {
                mHandler.sendEmptyMessage(STATE_UNREADY);
            } else if (mRoleData.getState() == STATE_UNREADY) {
                mHandler.sendEmptyMessage(STATE_READY);
            } else if (mRoleData.getState() == STATE_READY) {
                mHandler.sendEmptyMessage(STATE_LOCK);
            }
            if (mRoleData.isOwner()) {
//                MsgEvent msgEvent = new MsgEvent(MsgEvent.ROOM_START);
//                EventBus.getDefault().post(msgEvent);
            }
        } else {
            activity.showToast("位置不可开启");
        }
    }

    /**
     * 模拟发言，可以删除该方法
     */
    public void chat() {
        if (mRoleData != null) {
//            mHandler.sendEmptyMessage(STATE_UNREADY);

            GameChatData gameChatData = new GameChatData(GameChatData.CHAT, DateUtil.nowLongStr(), mRoleData.getNumber() + "号" + mRoleData.getNickName(), "", arrChat[new Random().nextInt(arrChat.length)]);
            MsgEvent msgEvent = new MsgEvent(MsgEvent.ROOM_CHAT, null, gameChatData);
            EventBus.getDefault().post(msgEvent);
        }
    }

    /**
     * 准备状态
     */
    public void ready() {
        mHandler.sendEmptyMessage(STATE_READY);
    }

    /**
     * 未准备状态
     */
    public void unReady() {
        mHandler.sendEmptyMessage(STATE_UNREADY);
    }

    /**
     * 离开状态
     */
    public void noBody() {
        mHandler.sendEmptyMessage(STATE_NOBODY);
    }

    /**
     * 锁定状态
     */
    public void lock() {
        mHandler.sendEmptyMessage(STATE_LOCK);
    }

    /**
     * 语音状态
     */
    public void speak() {
        mHandler.sendEmptyMessage(STATE_SPEAKING);
    }

    /**
     * 死亡状态
     */
    public void die() {
        mHandler.sendEmptyMessage(STATE_DIE);
    }

    /**
     * 位置是否存在玩家
     */
    public boolean hasRole() {
        return mRoleData != null;
    }

}
