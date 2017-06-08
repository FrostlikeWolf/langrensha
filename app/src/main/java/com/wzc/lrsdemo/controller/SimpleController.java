package com.wzc.lrsdemo.controller;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import com.wzc.lrsdemo.config.Constants;
import com.wzc.lrsdemo.event.GameStateMessage;
import com.wzc.lrsdemo.role.Huntsman;
import com.wzc.lrsdemo.role.Prophet;
import com.wzc.lrsdemo.role.Role;
import com.wzc.lrsdemo.role.Villager;
import com.wzc.lrsdemo.role.Witch;
import com.wzc.lrsdemo.role.Wolf;
import com.wzc.lrsdemo.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 简单模式游戏流程控制器
 * Created by Administrator on 2017/3/13.
 */

public class SimpleController implements Role.OnRoleStateChangeListener {
    private static final String TIANHEI = "天黑";
    private static final String YUYAN = "预言家行动";
    private static final String LANGREN = "狼人行动";
    private static final String NVWU = "女巫行动";
    private static final String LIEREN = "猎人行动";
    private static final String TIANLIANG = "天亮";
    public static final String TAOLUN = "讨论";
    public static final String TOUPIAO = "投票";
    private static final String TOUPIAOJIEGUO = "投票结果";

    private int winer = Role.WIN_NO;//阵营不要放在role里
    private static boolean isGameing;

    private int bout = 1;//回合数默认为1，默认第一天晚上
    private String curStage;
    private Map<String, Integer> elementMap;//角色组成情况，key=角色名，value=人数
    private Map<Integer, Role> deployMap;//角色部署情况，key=玩家编号，value=角色名

    private List<Integer> aliveList;//幸存者集合

    private Handler handler;

    /**
     * 流程控制器一开始就拥有handler通知界面变化
     */
    public SimpleController(Handler handler) {
        this.handler = handler;
    }

    public static boolean isGameing() {
        return isGameing;
    }

    public boolean isAlive(int number) {
        return deployMap.get(number).isAlive();
    }

    public String getCurStage() {
        return curStage;
    }

    public Role getRole(Integer number) {
        if (deployMap.containsKey(number)) {
            return deployMap.get(number);
        }
        return null;
    }

    public void startGame(int players) {
        winer = Role.WIN_NO;
        isGameing = true;
        bout = 1;
        //生成角色组成情况
        elementMap = GameAlloter.getElementMap(players);
        //生成角色部署情况
        deployMap = GameAlloter.getDeployMap(elementMap, players);

        //将角色部署情况显示到界面
        Message msg_role_deploy = new Message();
        msg_role_deploy.what = GameStateMessage.ROLE_DEPLOY;
        msg_role_deploy.obj = deployMap;
        handler.sendMessage(msg_role_deploy);

        //生成幸存者集合
        aliveList = GameAlloter.updateAliver(aliveList, deployMap);
        for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
            entry.getValue().setOnRoleStateChangeListener(this);
        }

        //进入天黑阶段
        doStage(TIANHEI);
    }

    public void stopGame() {
        isGameing = false;
    }

    //被决定的人
    private static List<Integer> nightDieList = new ArrayList<Integer>();
    private static int who;
    private static Prophet prophet;
    private static Wolf wolf;
    private static Witch witch;

    /**
     * 进入流程中的某阶段
     */

    private void doStage(String stage) {
        who = -1;
        prophet = null;
        wolf = null;
        witch = null;
        Runnable runnable = null;
        if (isGameing) {
            curStage = stage;


            switch (stage) {
                case TIANHEI:
//第一回合天黑时法官发言，游戏角色组成情况
                    if (bout == 1) {
                        String str_element = String.format(Constants.TEXT_START_ALLOT, elementMap.get(Wolf.NAME), elementMap.get(Prophet.NAME), elementMap.get(Witch.NAME), elementMap.get(Huntsman.NAME), elementMap.get(Villager.NAME));
                        Message msg_system_chat = new Message();
                        msg_system_chat.what = GameStateMessage.GAME_START;
                        msg_system_chat.obj = new GameStateMessage(null, str_element);
                        handler.sendMessage(msg_system_chat);
                    }
//修改房间标题
                    Message msg_room_title = new Message();
                    msg_room_title.what = GameStateMessage.ZHOUYE;
                    msg_room_title.obj = new GameStateMessage(null, "(第" + bout + "夜)");
                    handler.sendMessage(msg_room_title);
//法官发言
                    Message msg_system_chat = new Message();
                    msg_system_chat.what = GameStateMessage.CHAT;
                    msg_system_chat.obj = new GameStateMessage(null, String.format(Constants.TEXT_DAY_DARK, bout));
                    handler.sendMessage(msg_system_chat);
                    doStage(YUYAN);
                    break;

                case YUYAN:
                    for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
                        if (entry.getValue().getName().equals(Prophet.NAME) && entry.getValue().isAlive()) {
                            prophet = (Prophet) entry.getValue();
                            who = doRoleActionToWho(entry.getValue());
                        }
                    }
                    if (prophet != null) {
                        //修改倒计时内容
                        Message msg_system_timer = new Message();
                        msg_system_timer.what = GameStateMessage.COUNTDOWNTIMER;
                        msg_system_timer.obj = new GameStateMessage(prophet, String.format(Constants.TEXT_WAIT_ACTION, prophet.getName()), prophet.ACTION_TIME);
                        handler.sendMessage(msg_system_timer);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (who >= 1) {
                                    Message msg_system_chat = new Message();
                                    msg_system_chat.what = GameStateMessage.CHAT;
                                    msg_system_chat.obj = new GameStateMessage(null, "预言家调查了[" + who + "]号的身份,他的身份是" + deployMap.get(who).getName());
                                    handler.sendMessage(msg_system_chat);
                                }
                                doStage(LANGREN);
                            }
                        };
                        new GameTask(prophet.getRoleActionTime(), runnable).start();
                    } else {
                        doStage(LANGREN);
                    }
                    break;

                case LANGREN:
                    for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
                        if (entry.getValue().getName().equals(Wolf.NAME) && entry.getValue().isAlive()) {
                            wolf = (Wolf) entry.getValue();
                            who = doRoleActionToWho(entry.getValue());
                        }
                    }
                    if (wolf != null) {
                        //修改倒计时内容
                        Message msg_system_timer = new Message();
                        msg_system_timer.what = GameStateMessage.COUNTDOWNTIMER;
                        msg_system_timer.obj = new GameStateMessage(wolf, String.format(Constants.TEXT_WAIT_ACTION, wolf.getName()), Wolf.ACTION_TIME);
                        handler.sendMessage(msg_system_timer);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (who >= 1) {
                                    //女巫救人行动
                                    if (!witchSave(who)) {
                                        Role role = deployMap.get(who);
                                        Message msg_system_die = new Message();
                                        msg_system_die.what = GameStateMessage.CHAT;
                                        msg_system_die.obj = new GameStateMessage(role, String.format("[%d]号被狼杀", who));
                                        handler.sendMessage(msg_system_die);
                                        role.setState(Role.STATE_DIE_WOLF_KILL);

                                        nightDieList.add(who);
                                    }
                                }
                                doStage(NVWU);
                            }
                        };
                        new GameTask(wolf.getRoleActionTime(), runnable).start();
                    } else {
                        doStage(NVWU);
                    }
                    break;

                case NVWU:
                    for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
                        if (entry.getValue().getName().equals(Witch.NAME) && entry.getValue().isAlive()) {
                            witch = (Witch) entry.getValue();
                            who = doRoleActionToWho(entry.getValue());
                            break;
                        }
                    }
                    if (witch != null && witch.hasPanacea()) {
                        //修改倒计时内容
                        Message msg_system_timer = new Message();
                        msg_system_timer.what = GameStateMessage.COUNTDOWNTIMER;
                        msg_system_timer.obj = new GameStateMessage(witch, String.format(Constants.TEXT_WAIT_ACTION, witch.getName()), witch.ACTION_TIME);
                        handler.sendMessage(msg_system_timer);
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (who >= 1 && witch.hasPoison()) {
                                    witch.usePoison();
                                    Role role = deployMap.get(who);
                                    Message msg_system_chat = new Message();
                                    msg_system_chat.what = GameStateMessage.CHAT;
                                    msg_system_chat.obj = new GameStateMessage(role, String.format("[%d]号被毒杀", who));
                                    handler.sendMessage(msg_system_chat);
                                    role.setState(Role.STATE_DIE_POISON);
                                    nightDieList.add(who);
                                    LogUtil.e(who + "被毒杀");
                                } else {
                                }
                                doStage(LIEREN);
                            }
                        };
                        new GameTask(witch.getRoleActionTime(), runnable).start();
                    } else {
                        doStage(LIEREN);
                    }
                    break;

                case LIEREN:
                    Huntsman huntsman = null;
                    for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
                        if (entry.getValue().getName().equals(Huntsman.NAME) && entry.getValue().isAlive()) {
                            huntsman = (Huntsman) entry.getValue();
                            who = doRoleActionToWho(entry.getValue());
                            break;
                        }
                    }
                    doStage(TIANLIANG);
                    break;

                case TIANLIANG:
                    bout++;
                    //修改房间标题
                    Message msg_room_title_1 = new Message();
                    msg_room_title_1.what = GameStateMessage.ZHOUYE;
                    msg_room_title_1.obj = new GameStateMessage(null, "(第" + bout + "天)");
                    handler.sendMessage(msg_room_title_1);

                    //法官发言
                    Message msg_system_chat_1 = new Message();
                    msg_system_chat_1.what = GameStateMessage.CHAT;
                    //判断是否夜晚死过人
                    if (nightDieList.size() > 0) {
                        msg_system_chat_1.obj = new GameStateMessage(null, String.format(Constants.TEXT_DAY_DAWN, GameAlloter.textNumerFormat(nightDieList)));
                    } else {
                        msg_system_chat_1.obj = new GameStateMessage(null, Constants.TEXT_DAY_SAFE);
                    }
                    handler.sendMessage(msg_system_chat_1);
                    doStage(TAOLUN);
                    break;

                case TAOLUN:
                    int curSpeaker = 0;
                    for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
                        if (entry.getValue().getState().equals(Role.STATE_TALK)) {
                            curSpeaker = entry.getKey();
                            entry.getValue().setState(Role.STATE_HOLD_ON);
                            break;
                        }
                    }
                    int firstSpeaker = GameAlloter.pickSpeak(aliveList, nightDieList);
                    //curSpeaker=0时表示第一次轮询
                    if (curSpeaker == 0) {
                        curSpeaker = GameAlloter.pickSpeak(aliveList, curSpeaker);
                    } else {
                        curSpeaker = GameAlloter.pickSpeak(aliveList, curSpeaker);
                        //轮询结束
                        if (firstSpeaker == curSpeaker) {
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    doStage(TOUPIAO);
                                }
                            };
                            new GameTask(1000, runnable).start();
                            break;
                        }
                    }

                    //修改倒计时内容
                    Message msg_system_timer = new Message();
                    msg_system_timer.what = GameStateMessage.COUNTDOWNTIMER;
                    //是否有人语音中
                    if (curSpeaker > 0) {
                        if (nightDieList.size() > 0) {
                            msg_system_timer.obj = new GameStateMessage(witch, String.format(Constants.TEXT_DAY_DAWN_TIMER, GameAlloter.textNumerFormat(nightDieList), curSpeaker), Role.getCommonActionTime());
                        } else {
                            msg_system_timer.obj = new GameStateMessage(witch, String.format(Constants.TEXT_DAY_SAFE_TIMER, curSpeaker), Role.getCommonActionTime());
                        }
                    } else {
                        if (nightDieList.size() > 0) {
                            msg_system_timer.obj = new GameStateMessage(witch, String.format(Constants.TEXT_DAY_DAWN_TIMER, GameAlloter.textNumerFormat(nightDieList), firstSpeaker), Role.getCommonActionTime());
                        } else {
                            msg_system_timer.obj = new GameStateMessage(witch, String.format(Constants.TEXT_DAY_SAFE_TIMER, firstSpeaker), Role.getCommonActionTime());
                        }
                    }


                    handler.sendMessage(msg_system_timer);

                    Role role = deployMap.get(curSpeaker);
                    role.setState(Role.STATE_TALK);

                    Message msg_player_speak = new Message();
                    msg_player_speak.what = GameStateMessage.GAME_SPEAK;
                    msg_player_speak.obj = new GameStateMessage(role, null);
                    handler.sendMessage(msg_player_speak);

                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            doStage(TAOLUN);
                        }
                    };
                    new GameTask(Role.getCommonActionTime(), runnable).start();
                    break;

                case TOUPIAO:
                    nightDieList.clear();
                    //修改倒计时内容
                    Message msg_system_timer_vote = new Message();
                    msg_system_timer_vote.what = GameStateMessage.COUNTDOWNTIMER;
                    msg_system_timer_vote.obj = new GameStateMessage(null, "等待其他玩家投票", Role.getRoleActionTime());
                    handler.sendMessage(msg_system_timer_vote);

                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (Role.doCommonAction() == Role.ACTION_KILL) {
                                who = aliveList.get(new Random().nextInt(aliveList.size()));
                            }

                            if (who >= 1) {
                                Role role = deployMap.get(who);
                                Message msg_system_chat_vote = new Message();
                                msg_system_chat_vote.what = GameStateMessage.CHAT;
//                                msg_system_chat_vote.obj = new GameStateMessage(role, String.format("[%d]号被票杀", who));
                                msg_system_chat_vote.obj = new GameStateMessage(role, String.format(Constants.TEXT_VOTE_KILL, who));
                                handler.sendMessage(msg_system_chat_vote);
                                role.setState(Role.STATE_DIE_VOTE);
                            }
                            doStage(TOUPIAOJIEGUO);
                        }
                    };
                    new GameTask(Role.getRoleActionTime(), runnable, false).start();
                    break;

                case TOUPIAOJIEGUO:
                    doStage(TIANHEI);
                    break;
            }
        }
    }

    private boolean witchSave(int number) {
        witch = null;
        for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
            if (entry.getValue().getName().equals(Witch.NAME) && entry.getValue().isAlive()) {
                witch = (Witch) entry.getValue();
                break;
            }
        }
        if (witch != null && witch.hasPanacea()) {
            if (Role.ACTION_GIVE_UP == witch.doRoleAction()) {
                witch.usePanacea();
                LogUtil.e("女巫拯救了" + number);
                return true;
            }
        }
        return false;
    }

    /**
     * 角色对谁做出独有行动
     */
    private int doRoleActionToWho(Role role) {
        int who = -1;
        int outlier = -1;
        if (role.doRoleAction() == Role.ACTION_GIVE_UP) {
            return who;
        }

        for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
            if (role.equals(entry.getValue())) {
                outlier = entry.getKey();
            }
        }

        return GameAlloter.chooseAliver(aliveList, outlier);
    }

    public List<Integer> getAliveList() {
        return aliveList;
    }

    @Override
    public void onStateChange() {
        handler.sendEmptyMessage(GameStateMessage.STAGE_CHANGE);

        aliveList = GameAlloter.updateAliver(aliveList, deployMap);
        winer = GameAlloter.checkWinner(deployMap);
        String str_winer = null;
        if (winer == Role.WIN_NO) {
            return;
        }
        if (winer == Role.WIN_WOLF) {
            str_winer = "狼人";
            isGameing = false;
        }
        if (winer == Role.WIN_PERSON) {
            str_winer = "好人";
            isGameing = false;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Role> entry : deployMap.entrySet()) {
            sb.append("\n" + entry.getKey() + "号" + entry.getValue().getName() + "(" + entry.getValue().getState() + ")");
        }
        Message msg_system_chat_result = new Message();
        msg_system_chat_result.what = GameStateMessage.GAME_OVER;
        msg_system_chat_result.obj = new GameStateMessage(null, String.format(Constants.TEXT_OVER_RESULT, str_winer, sb.toString()));
        handler.sendMessage(msg_system_chat_result);
        LogUtil.e(sb.toString());

        Message msg_room_title = new Message();
        msg_room_title.what = GameStateMessage.ZHOUYE;
        msg_room_title.obj = new GameStateMessage(null, "");
    }

    private class GameTask {
        private static final int COUNT_DOWN_INTERVAL = 1000;
        private long millisInFuture;
        private Runnable runnable;
        private boolean isCanRandom;

        public GameTask(long millisInFuture, Runnable runnable) {
            this.millisInFuture = millisInFuture;
            this.runnable = runnable;
        }

        public GameTask(long millisInFuture, Runnable runnable, boolean isCanRandom) {
            this.millisInFuture = millisInFuture;
            this.runnable = runnable;
            this.isCanRandom = isCanRandom;
        }

        public boolean skip() {
            return false;
        }

        public void start() {
            if (skip()) {
                return;
            }
            //判断该类角色是否有幸存者
            long time = new Random().nextInt((int) millisInFuture);
            if (isCanRandom) {
                if (time < 3000) {
                    time = 3000;
                }
            } else {
                time = millisInFuture;
            }

            new CountDownTimer(time, COUNT_DOWN_INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    runnable.run();
                }
            }.start();
        }
    }
}
