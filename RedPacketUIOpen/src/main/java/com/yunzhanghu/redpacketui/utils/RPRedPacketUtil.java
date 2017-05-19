package com.yunzhanghu.redpacketui.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.yunzhanghu.redpacketsdk.RPSendPacketCallback;
import com.yunzhanghu.redpacketsdk.RPTokenCallback;
import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;
import com.yunzhanghu.redpacketsdk.contract.CheckPacketContract;
import com.yunzhanghu.redpacketsdk.presenter.impl.CheckPacketStatusPresenter;
import com.yunzhanghu.redpacketui.callback.RandomDetailCallback;
import com.yunzhanghu.redpacketui.ui.activity.RPDetailActivity;
import com.yunzhanghu.redpacketui.ui.activity.RPRecordActivity;
import com.yunzhanghu.redpacketui.ui.activity.RPRedPacketActivity;
import com.yunzhanghu.redpacketui.ui.fragment.PayTipsDialogFragment;
import com.yunzhanghu.redpacketui.ui.fragment.RandomDetailDialogFragment;
import com.yunzhanghu.redpacketui.ui.fragment.RandomPacketDialogFragment;
import com.yunzhanghu.redpacketui.ui.fragment.RedPacketDialogFragment;
import com.yunzhanghu.redpacketui.ui.fragment.SRedPacketDialogFragment;

import java.io.IOException;

/**
 * Created by max on 16/3/20
 */
public class RPRedPacketUtil {

    private static RPRedPacketUtil instance;

    private RedPacketDialogFragment mRedPacketDialogFragment;

    private SRedPacketDialogFragment mSRedPacketDialogFragment;

    private RandomDetailDialogFragment mRandomDetailDialogFragment;

    private CheckPacketStatusPresenter mCheckPacketStatusPresenter;

    private RandomDetailCallback mRandomDetailCallback;

    public static RPRedPacketUtil getInstance() {
        if (null == instance) {
            synchronized (RPRedPacketUtil.class) {
                if (null == instance) {
                    instance = new RPRedPacketUtil();
                }
            }
        }
        return instance;
    }

    private RPRedPacketUtil() {
    }


    public interface RPOpenPacketCallback {
        void onSuccess(RedPacketInfo redPacketInfo);

        void showLoading();

        void hideLoading();

        void onError(String code, String message);
    }

    /**
     * 打开红包的方法
     *
     * @param redPacketId   红包id
     * @param redPacketType 红包类型
     * @param activity      FragmentActivity
     * @param callBack      RPOpenPacketCallback
     */
    public void openRedPacket(final String redPacketId, final String redPacketType, final FragmentActivity activity, final RPOpenPacketCallback callBack) {
        if (callBack == null) {
            throw new IllegalArgumentException("RPOpenPacketCallback is null!");
        }
        //for bug:Fragment already added
        if (ClickUtil.isFastClick()) return;
        callBack.showLoading();
        final RedPacketInfo currentUserInfo = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
        RedPacket.getInstance().initRPToken(currentUserInfo.currentUserId, new RPTokenCallback() {
            @Override
            public void onTokenSuccess() {
                CheckPacketStatusPresenter presenter;
                if (redPacketType.equals(RPConstant.RED_PACKET_TYPE_ADVERTISEMENT)) {
                    presenter = checkADRedPacketStatus(activity, callBack);
                } else {
                    presenter = checkRedPacketStatus(activity, currentUserInfo, callBack);
                }
                presenter.checkRedPacketStatus(redPacketId);
            }

            @Override
            public void onSettingSuccess() {

            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                callBack.onError(errorCode, errorMsg);
                callBack.hideLoading();
            }
        });
    }

    /**
     * 查询广告红包状态
     *
     * @param activity FragmentActivity
     * @param callBack RPOpenPacketCallback
     * @return CheckPacketStatusPresenter
     */
    private CheckPacketStatusPresenter checkADRedPacketStatus(final FragmentActivity activity, final RPOpenPacketCallback callBack) {
        mCheckPacketStatusPresenter = new CheckPacketStatusPresenter(CheckPacketStatusPresenter.STATUS_EVENT_TAG_AD);
        mCheckPacketStatusPresenter.attach(new CheckPacketContract.View() {

            @Override
            public void onCheckStatusSuccess(RedPacketInfo redPacketInfo, boolean isShowDialog) {
                if (!isShowDialog) {
                    callBack.hideLoading();
                    Intent intent = new Intent(activity, RPDetailActivity.class);
                    intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
                    activity.startActivity(intent);
                }
            }

            @Override
            public void onPacketExpired(String message) {
                PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(RPConstant.CLIENT_CODE_OTHER_ERROR, message);
                showAllowingStateLost(dialog, activity);
            }

            @Override
            public void onError(String code, String message) {
                callBack.hideLoading();
                callBack.onError(code, message);
            }
        });
        return mCheckPacketStatusPresenter;
    }

    /**
     * 查询非广告红包状态
     *
     * @param activity        FragmentActivity
     * @param currentUserInfo 当前用户信息(即拆红包者信息)
     * @param callBack        RPOpenPacketCallback
     * @return CheckPacketStatusPresenter
     */
    private CheckPacketStatusPresenter checkRedPacketStatus(final FragmentActivity activity, final RedPacketInfo currentUserInfo, final RPOpenPacketCallback callBack) {
        mCheckPacketStatusPresenter = new CheckPacketStatusPresenter(CheckPacketStatusPresenter.STATUS_EVENT_TAG_NOT_AD);
        mCheckPacketStatusPresenter.attach(new CheckPacketContract.View() {
            @Override
            public void onCheckStatusSuccess(final RedPacketInfo redPacketInfo, boolean isShowDialog) {
                if (isShowDialog) {
                    showRedPacketDialog(redPacketInfo, activity, currentUserInfo, callBack);
                } else {
                    showRedPacketDetail(redPacketInfo, callBack, activity);
                }
            }


            @Override
            public void onError(String code, String message) {
                callBack.hideLoading();
                callBack.onError(code, message);
            }

            @Override
            public void onPacketExpired(String message) {
                callBack.hideLoading();
                PayTipsDialogFragment dialog = PayTipsDialogFragment.newInstance(RPConstant.CLIENT_CODE_OTHER_ERROR, message);
                showAllowingStateLost(dialog, activity);
            }

        });
        return mCheckPacketStatusPresenter;
    }


    /**
     * 显示红包详情的方法
     *
     * @param redPacketInfo 红包详情数据
     * @param callBack      RPOpenPacketCallback
     * @param activity      FragmentActivity
     */
    private void showRedPacketDetail(RedPacketInfo redPacketInfo, RPOpenPacketCallback callBack, FragmentActivity activity) {
        if (redPacketInfo.redPacketType.equals(RPConstant.RED_PACKET_TYPE_SINGLE_RANDOM)) {
            if (redPacketInfo.messageDirect.equals(RPConstant.MESSAGE_DIRECT_RECEIVE)) {
                if (redPacketInfo.status == RPConstant.RED_PACKET_STATUS_RECEIVABLE) {
                    mRandomDetailCallback = new RandomDetailCallback(redPacketInfo, callBack, activity);
                    mRandomDetailCallback.receiveRedPacket();
                    return;
                }
            }
            callBack.hideLoading();
            if (mRandomDetailDialogFragment == null) {
                mRandomDetailDialogFragment = RandomDetailDialogFragment.newInstance(redPacketInfo);
            } else {
                //for bug:Fragment already active
                if (mRandomDetailDialogFragment.isAdded()) return;
                mRandomDetailDialogFragment.setArguments(redPacketInfo);
            }
            if (mRandomDetailDialogFragment != null && !mRandomDetailDialogFragment.isAdded()) {//防止重复弹出对话框
                showAllowingStateLost(mRandomDetailDialogFragment, activity);
            }
        } else {
            callBack.hideLoading();
            Intent intent = new Intent(activity, RPDetailActivity.class);
            intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
            activity.startActivity(intent);
        }
    }


    /**
     * 显示红包对话框的方法
     *
     * @param redPacketInfo   红包数据
     * @param activity        FragmentActivity
     * @param currentUserInfo 当前用户信息
     * @param callBack        RPOpenPacketCallback
     */
    private void showRedPacketDialog(final RedPacketInfo redPacketInfo, final FragmentActivity activity, final RedPacketInfo currentUserInfo, final RPOpenPacketCallback callBack) {
        callBack.hideLoading();
        if (!TextUtils.isEmpty(redPacketInfo.redPacketType) && TextUtils.equals(redPacketInfo.redPacketType, RPConstant.RED_PACKET_TYPE_GROUP_EXCLUSIVE)) {
            if (mSRedPacketDialogFragment == null) {
                mSRedPacketDialogFragment = SRedPacketDialogFragment.newInstance(redPacketInfo);
            } else {
                //for bug:Fragment already active
                if (mSRedPacketDialogFragment.isAdded()) return;
                mSRedPacketDialogFragment.setArguments(redPacketInfo);
            }
            mSRedPacketDialogFragment.setCallback(new RPValueCallback<String>() {
                @Override
                public void onSuccess(String myAmount) {
                    playSound(activity);
                    redPacketInfo.receiverId = currentUserInfo.currentUserId;
                    redPacketInfo.receiverNickname = currentUserInfo.currentNickname;
                    redPacketInfo.myAmount = myAmount;
                    callBack.onSuccess(redPacketInfo);
                    mSRedPacketDialogFragment = null;
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    callBack.onError(errorCode, errorMsg);
                }
            });
            if (mSRedPacketDialogFragment != null && !mSRedPacketDialogFragment.isAdded()) {
                showAllowingStateLost(mSRedPacketDialogFragment, activity);
            }
        } else {
            //传入拆红包人昵称和头像
            if (mRedPacketDialogFragment == null) {
                mRedPacketDialogFragment = RedPacketDialogFragment.newInstance(redPacketInfo);
            } else {
                //for bug:Fragment already active
                if (mRedPacketDialogFragment.isAdded()) return;
                mRedPacketDialogFragment.setArguments(redPacketInfo);
            }
            mRedPacketDialogFragment.setCallback(new RPValueCallback<String>() {
                @Override
                public void onSuccess(String myAmount) {
                    playSound(activity);
                    redPacketInfo.receiverId = currentUserInfo.currentUserId;
                    redPacketInfo.receiverNickname = currentUserInfo.currentNickname;
                    redPacketInfo.myAmount = myAmount;
                    callBack.onSuccess(redPacketInfo);
                    mRedPacketDialogFragment = null;
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    callBack.onError(errorCode, errorMsg);
                }
            });
            if (mRedPacketDialogFragment != null && !mRedPacketDialogFragment.isAdded()) {
                showAllowingStateLost(mRedPacketDialogFragment, activity);
            }
        }
    }

    private void showAllowingStateLost(DialogFragment fragment, FragmentActivity activity) {
        if (activity != null) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.add(fragment, RPConstant.RP_PACKET_DIALOG_TAG);
            ft.commitAllowingStateLoss();
        }
    }

    /**
     * 进入小额随机红包方法
     *
     * @param redPacketInfo RedPacketInfo
     * @param activity      FragmentActivity
     */
    private void enterRandomRedPacket(RedPacketInfo redPacketInfo, FragmentActivity activity) {
        RandomPacketDialogFragment randomPacketDialogFragment = RandomPacketDialogFragment.newInstance(redPacketInfo);
        if (randomPacketDialogFragment != null && !randomPacketDialogFragment.isAdded()) {//防止重复弹出对话框
            showAllowingStateLost(randomPacketDialogFragment, activity);
        }
    }

    /**
     * 进入红包页面方法（单聊、小额随机、群红包、专属红包）
     *
     * @param activity      FragmentActivity
     * @param itemType      菜单的类型
     * @param redPacketInfo RedPacketInfo
     * @param callback      RPSendPacketCallback
     */
    public void startRedPacket(FragmentActivity activity, int itemType, RedPacketInfo redPacketInfo, RPSendPacketCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("RPSendPacketCallback is null!");
        }
        RedPacket.getInstance().setRPSendPacketCallback(callback);
        RedPacketInfo currentUserInfo = RedPacket.getInstance().getRPInitRedPacketCallback().initCurrentUserSync();
        redPacketInfo.senderId = currentUserInfo.currentUserId;
        redPacketInfo.senderNickname = currentUserInfo.currentNickname;
        redPacketInfo.senderAvatarUrl = currentUserInfo.currentAvatarUrl;
        Intent intent = null;
        switch (itemType) {
            case RPConstant.RP_ITEM_TYPE_SINGLE:
                redPacketInfo.chatType = RPConstant.CHAT_TYPE_SINGLE;
                intent = new Intent(activity, RPRedPacketActivity.class);
                break;
            case RPConstant.RP_ITEM_TYPE_GROUP:
                redPacketInfo.chatType = RPConstant.CHAT_TYPE_GROUP;
                intent = new Intent(activity, RPRedPacketActivity.class);
                break;
            case RPConstant.RP_ITEM_TYPE_RANDOM:
                redPacketInfo.chatType = RPConstant.CHAT_TYPE_SINGLE;
                enterRandomRedPacket(redPacketInfo, activity);
                break;
        }
        if (intent != null) {
            intent.putExtra(RPConstant.EXTRA_RED_PACKET_INFO, redPacketInfo);
            activity.startActivity(intent);
        }

    }

    public void startRecordActivity(Context context) {
        Intent intent = new Intent(context, RPRecordActivity.class);
        context.startActivity(intent);
    }

    public void detachView() {
        if (mCheckPacketStatusPresenter != null) {
            mCheckPacketStatusPresenter.detach(true);
            mCheckPacketStatusPresenter = null;
        }
        if (mRandomDetailCallback != null) {
            mRandomDetailCallback.detach();
        }
        if (mRedPacketDialogFragment != null) {
            mRedPacketDialogFragment = null;
        }
        if (mSRedPacketDialogFragment != null) {
            mSRedPacketDialogFragment = null;
        }
        if (mRandomDetailDialogFragment != null) {
            mRandomDetailDialogFragment = null;
        }
        RedPacket.getInstance().detachCallback();
        RedPacket.getInstance().detachTokenPresenter();
    }

    /**
     * 播放音效
     * assets中的 open_packet_sound.mp3或者是open_packet_sound.wav
     */
    public void playSound(Context context) {
        AssetFileDescriptor assetFileDescriptor;
        if (isWAVSoundFileExist(context) == null) {
            if (isMP3SoundFileExist(context) == null) {
                return;
            } else {
                assetFileDescriptor = isMP3SoundFileExist(context);
            }
        } else {
            assetFileDescriptor = isWAVSoundFileExist(context);
        }
        SoundPool soundPool;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_SYSTEM)
                    .build();
            SoundPool.Builder spb = new SoundPool.Builder();
            spb.setMaxStreams(10);
            spb.setAudioAttributes(attributes);
            soundPool = spb.build();
        } else {
            /*
             * 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
             */
            soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        }
        /*
         * 此方法为异步操作,因此要在加载完成后播放音频  
         * 把声音素材放到assets里，第2个参数即为资源文件，第3个为音乐的优先级
         */
        soundPool.load(assetFileDescriptor, 1);
        /*
         * 第一个参数为当前装载完成的音频资源在音频池中的ID;  
         * 第二个参数为左声道音量;  
         * 第三个参数为右声道音量;  
         * 第四个参数为优先级； 
         * 第五个参数为循环次数，0不循环，-1循环;  
         * 第六个参数为速率，速率最低0.5最高为2，1代表正常速度  
         */
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId, 1, 1, 0, 0, 1);// 播放音效
            }
        });
    }

    private AssetFileDescriptor isMP3SoundFileExist(Context context) {
        try {
            return context.getResources().getAssets().openFd("open_packet_sound.mp3");
        } catch (IOException e) {
            return null;
        }
    }

    private AssetFileDescriptor isWAVSoundFileExist(Context context) {
        try {
            return context.getResources().getAssets().openFd("open_packet_sound.wav");
        } catch (IOException e) {
            return null;
        }
    }

}
